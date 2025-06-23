import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


public class ResolvedorParalelo {

    public static void main(String[] args) {
        List<CidadePCV> cidades;
        try {
            //Carrega a lista de cidades do arquivo.
            cidades = carregarCidadesDeArquivo("cidades.txt");
        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo de cidades: " + e.getMessage());
            return;
        }

        //Execução do Vizinho Mais Próximo 
        System.out.println("\n--- Executando Algoritmo do Vizinho Mais Próximo ---");
        long inicioVMP = System.nanoTime();
        ResultadoPCV resVMP = resolverVizinhoMaisProximo(cidades);
        long tempoVMP = System.nanoTime() - inicioVMP;
        
        System.out.printf("Execucao VMP concluida em %.4fs\n", tempoVMP / 1e9);
        System.out.println("Rota VMP: " + resVMP.getRota().stream().map(CidadePCV::getNome).collect(Collectors.joining(", ")));
        System.out.printf("Distancia VMP: %.2f\n", resVMP.getDistancia());
        
        //Execução da Força Bruta Paralela 
        System.out.println("\n--- Executando Algoritmo de Forca Bruta ---");
        long inicioFB = System.nanoTime();
        ResultadoPCV resFB = resolverForcaBrutaParaleloFinal(cidades);
        long tempoFB = System.nanoTime() - inicioFB;
        
        if (resFB.getRota() != null) {
            System.out.printf("Execucao Forca Bruta concluida em %.4fs\n", tempoFB / 1e9);
            System.out.println("Rota Forca Bruta: " + resFB.getRota().stream().map(CidadePCV::getNome).collect(Collectors.joining(", ")));
            System.out.printf("Distancia Forca Bruta: %.2f\n", resFB.getDistancia());
        } else {
            System.out.println("Nenhuma solucao foi encontrada.");
        }
    }
    
    
    //Resolve o PCV usando uma abordagem de Força Bruta com paralelismo.
    public static ResultadoPCV resolverForcaBrutaParaleloFinal(List<CidadePCV> cidades) {
        // Valida se a lista de cidades é suficiente para o cálculo.
        if (cidades == null || cidades.size() < 2) {
            return null;
        }
        // Limita a execução para evitar tempos absurdamente longos.
        if (cidades.size() > 13) {
            System.out.println("AVISO: Forca Bruta e muito lenta para mais de 13 cidades. Execucao abortada.");
            return new ResultadoPCV(null, Double.POSITIVE_INFINITY);
        }

        // Define a quantidade de threads com base nos núcleos de CPU disponíveis
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        System.out.println(cidades.size() + " cidades, usando " + numThreads + " threads...");

        // Define a cidade de origem e a lista das demais cidades a serem visitadas
        CidadePCV origem = cidades.get(0);
        List<CidadePCV> cidadesParaPermutar = new ArrayList<>(cidades.subList(1, cidades.size()));
        
        List<Future<ResultadoPCV>> futurosResultados = new ArrayList<>();

        // Cria uma tarefa para cada cidade a ser visitada para uma thread
        // Cada thread será responsável por todas as rotas que começam com aquela cidade
        for (final CidadePCV cidadeInicialDaPermutacao : cidadesParaPermutar) {
            
            Callable<ResultadoPCV> tarefa = () -> {

                List<CidadePCV> restoDasCidades = new ArrayList<>(cidadesParaPermutar);
                restoDasCidades.remove(cidadeInicialDaPermutacao);

                BuscaDePermutacaoThread buscaDaThread = new BuscaDePermutacaoThread(origem, cidadeInicialDaPermutacao);
                
                // Dispara a busca recursiva para o subconjunto desta thread
                buscaDaThread.permutar(restoDasCidades, 0);

                return new ResultadoPCV(buscaDaThread.getMelhorRotaLocal(), buscaDaThread.getMenorDistanciaLocal());
            };
            // Envia a tarefa para o pool de threads para execução
            futurosResultados.add(executor.submit(tarefa));
        }

        // Compara o resultado de todas as threads para encontrar o melhor global
        ResultadoPCV melhorGlobal = new ResultadoPCV(null, Double.MAX_VALUE);
        try {
            for (Future<ResultadoPCV> f : futurosResultados) {

                ResultadoPCV resultadoParcial = f.get();
                // Se o resultado desta thread for melhor que o melhor global, atualiza
                if (resultadoParcial != null && resultadoParcial.getDistancia() < melhorGlobal.getDistancia()) {
                    melhorGlobal = resultadoParcial;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Encera o pool de threads
            executor.shutdown();
        }

        return melhorGlobal;
    }
    
    //Resolve o PCV usando o método do vizinho mais próximo
    public static ResultadoPCV resolverVizinhoMaisProximo(List<CidadePCV> cidades) {
        List<CidadePCV> rota = new ArrayList<>();
        List<CidadePCV> naoVisitadas = new ArrayList<>(cidades);

        CidadePCV cidadeAtual = naoVisitadas.remove(0);
        rota.add(cidadeAtual);

        //Loop para visitar todas as cidades
        while (!naoVisitadas.isEmpty()) {
            CidadePCV vizinhoMaisProximo = null;
            double menorDistanciaLocal = Double.POSITIVE_INFINITY;

            //Loop para encontra a cidade mais próxima da cidade atual
            for (CidadePCV proxima : naoVisitadas) {
                double distancia = cidadeAtual.distanciaPara(proxima);
                if (distancia < menorDistanciaLocal) {
                    menorDistanciaLocal = distancia;
                    vizinhoMaisProximo = proxima;
                }
            }
            //Move para a cidade encontrada
            cidadeAtual = vizinhoMaisProximo;
            rota.add(cidadeAtual);
            naoVisitadas.remove(cidadeAtual);
        }
        
        rota.add(rota.get(0));

        // Calcula a distância total da rota encontrada.
        double distanciaTotal = 0;
        for (int i = 0; i < rota.size() - 1; i++) {
            distanciaTotal += rota.get(i).distanciaPara(rota.get(i + 1));
        }
        
        return new ResultadoPCV(rota, distanciaTotal);
    }

    //Carrega uma lista de cidades a partir de um arquivo de texto
    public static List<CidadePCV> carregarCidadesDeArquivo(String filepath) throws IOException {
        List<CidadePCV> cidades = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.strip().split(",");
                if (partes.length == 3) {
                    cidades.add(new CidadePCV(partes[0].trim(), Integer.parseInt(partes[1].trim()), Integer.parseInt(partes[2].trim())));
                }
            }
        }
        System.out.println(cidades.size() + " cidades carregadas do arquivo '" + filepath + "'.");
        return cidades;
    }
}

// Classe helper que realiza a busca recursiva para uma única thread
class BuscaDePermutacaoThread {
    private List<CidadePCV> melhorRotaLocal;
    private double menorDistanciaLocal = Double.POSITIVE_INFINITY;
    private final CidadePCV cidadeOrigem;
    private final CidadePCV cidadeInicialDaPermutacao;

    public BuscaDePermutacaoThread(CidadePCV origem, CidadePCV inicialDaPermutacao) {
        this.cidadeOrigem = origem;
        this.cidadeInicialDaPermutacao = inicialDaPermutacao;
    }

    //Método recursivo que explora todas as permutações possíveis para um subconjunto de cidades.
    public void permutar(List<CidadePCV> cidades, int k) {

        if (k == cidades.size()) {

            double distanciaAtual = calcularDistanciaDaRota(cidades);

            // Se for a melhor rota encontrada por esta thread até agora, salva
            if (distanciaAtual < this.menorDistanciaLocal) {
                this.menorDistanciaLocal = distanciaAtual;
                // Monta a rota completa apenas para o melhor resultado.
                this.melhorRotaLocal = new ArrayList<>();
                this.melhorRotaLocal.add(cidadeOrigem);
                this.melhorRotaLocal.add(cidadeInicialDaPermutacao);
                this.melhorRotaLocal.addAll(cidades);
                this.melhorRotaLocal.add(cidadeOrigem);
            }
        } else {
            for (int i = k; i < cidades.size(); i++) {
                Collections.swap(cidades, k, i);
                permutar(cidades, k + 1);
                Collections.swap(cidades, k, i); // Desfaz a troca para verificar outros caminhos
            }
        }
    }
    
    //Calcula a distância total de uma rota específica, partindo da origem global
    
    private double calcularDistanciaDaRota(List<CidadePCV> cidadesPermutadas) {
        double distancia = 0;
        // Começa da cidade fixa desta thread.
        CidadePCV anterior = cidadeInicialDaPermutacao;
        distancia += cidadeOrigem.distanciaPara(anterior);

        // Soma as distâncias entre as cidades da permutação.
        for (CidadePCV atual : cidadesPermutadas) {
            distancia += anterior.distanciaPara(atual);
            anterior = atual;
        }

        distancia += anterior.distanciaPara(cidadeOrigem);
        return distancia;
    }

    // Métodos para a thread principal recuperar o melhor resultado encontrado aqui
    public List<CidadePCV> getMelhorRotaLocal() { return melhorRotaLocal; }
    public double getMenorDistanciaLocal() { return menorDistanciaLocal; }
}