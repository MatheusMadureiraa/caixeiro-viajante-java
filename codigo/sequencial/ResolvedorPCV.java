import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResolvedorPCV {

    private List<Cidade> cidades;

    public ResolvedorPCV(List<Cidade> cidades) {
        if (cidades == null || cidades.isEmpty()) {
            throw new IllegalArgumentException("A lista de cidades não pode ser vazia.");
        }
        this.cidades = cidades;
    }

    //  Algoritmo de Força Bruta 
    public Resultado resolverForcaBruta() {
        System.out.println("\n-- Executando Algoritmo de Força Bruta --");
        
        Permutacao p = new Permutacao(cidades.get(0));
        p.permutar(new ArrayList<>(cidades.subList(1, cidades.size())), 0);

        return new Resultado(p.getMelhorRota(), p.getMenorDistancia());
    }

    //  Algoritmo do Vizinho Mais Próximo
    public Resultado resolverVizinhoMaisProximo() {
        System.out.println("\n-- Executando Algoritmo do Vizinho Mais Próximo --");
        List<Cidade> rota = new ArrayList<>();
        List<Cidade> naoVisitadas = new ArrayList<>(this.cidades);

        Cidade cidadeAtual = naoVisitadas.remove(0);
        rota.add(cidadeAtual);

        while (!naoVisitadas.isEmpty()) {
            Cidade vizinhoMaisProximo = null;
            double menorDistanciaLocal = Double.POSITIVE_INFINITY;

            for (Cidade proxima : naoVisitadas) {
                double distancia = cidadeAtual.distanciaPara(proxima);
                if (distancia < menorDistanciaLocal) {
                    menorDistanciaLocal = distancia;
                    vizinhoMaisProximo = proxima;
                }
            }
            cidadeAtual = vizinhoMaisProximo;
            rota.add(cidadeAtual);
            naoVisitadas.remove(cidadeAtual);
        }
        rota.add(rota.get(0)); // Fecha o ciclo

        double distanciaTotal = 0;
        for (int i = 0; i < rota.size() - 1; i++) {
            distanciaTotal += rota.get(i).distanciaPara(rota.get(i + 1));
        }
        
        return new Resultado(rota, distanciaTotal);
    }
    
    //Método de Geração e Carregamento de Dados
    public static List<Cidade> carregarCidadesDeArquivo(String filepath) throws IOException {
        List<Cidade> cidades = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.strip().split(",");
                if (partes.length == 3) {
                    cidades.add(new Cidade(partes[0], Integer.parseInt(partes[1]), Integer.parseInt(partes[2])));
                }
            }
        }
        System.out.println(cidades.size() + " cidades carregadas do arquivo '" + filepath + "'.");
        return cidades;
    }
    
    //  Ponto de Entrada do Programa 
    public static void main(String[] args) {
        List<Cidade> cidadesParaResolver;
        try {
            cidadesParaResolver = carregarCidadesDeArquivo("cidades.txt");
        } catch (IOException e) {
            System.err.println("Erro: Arquivo 'cidades.txt' não encontrado. Encerrando.");
            return;
        }

        if (cidadesParaResolver != null && !cidadesParaResolver.isEmpty()) {
            ResolvedorPCV resolvedor = new ResolvedorPCV(cidadesParaResolver);
            
            // Mede e executa o Vizinho Mais Próximo
            long inicioVMP = System.nanoTime();
            Resultado resVMP = resolvedor.resolverVizinhoMaisProximo();
            long tempoVMP = System.nanoTime() - inicioVMP;
            System.out.printf("Execução VMP concluída em %.4fs\n", tempoVMP / 1e9);
            System.out.printf("Em ms: %.3fms\n", tempoVMP / 1e6);
            System.out.println("Rota VMP: " + resVMP.getRota());
            System.out.printf("Distância VMP: %.2f\n", resVMP.getDistancia());
            
            // Mede e executa a Força Bruta
            long inicioFB = System.nanoTime();
            Resultado resFB = resolvedor.resolverForcaBruta();
            long tempoFB = System.nanoTime() - inicioFB;
            
            if (resFB.getRota() != null) {
                System.out.printf("Execução Força Bruta concluída em %.4fs\n", tempoFB / 1e9);
                System.out.printf("Em ms: %.3fms\n", tempoFB / 1e6);
                System.out.println("Rota Força Bruta: " + resFB.getRota());
                System.out.printf("Distância Força Bruta: %.2f\n", resFB.getDistancia());
            }
        }
    }
}


//  Classe Auxiliar para a lógica de permutação da Força Bruta 
class Permutacao {
    private List<Cidade> melhorRota;
    private double menorDistancia = Double.POSITIVE_INFINITY;
    private final Cidade cidadeInicial;

    public Permutacao(Cidade inicial) {
        this.cidadeInicial = inicial;
    }

    public void permutar(List<Cidade> cidades, int k) {
        if (k == cidades.size()) {
            double distanciaAtual = calcularDistanciaDaRota(cidades);
            if (distanciaAtual < this.menorDistancia) {
                this.menorDistancia = distanciaAtual;
                this.melhorRota = new ArrayList<>();
                this.melhorRota.add(cidadeInicial);
                this.melhorRota.addAll(cidades);
                this.melhorRota.add(cidadeInicial);
            }
        } else {
            for (int i = k; i < cidades.size(); i++) {
                Collections.swap(cidades, k, i);
                permutar(cidades, k + 1);
                Collections.swap(cidades, k, i); // backtrack
            }
        }
    }
    
    private double calcularDistanciaDaRota(List<Cidade> cidadesPermutadas) {
        double distancia = 0;
        Cidade anterior = cidadeInicial;
        for (Cidade atual : cidadesPermutadas) {
            distancia += anterior.distanciaPara(atual);
            anterior = atual;
        }
        distancia += anterior.distanciaPara(cidadeInicial);
        return distancia;
    }

    public List<Cidade> getMelhorRota() { return melhorRota; }
    public double getMenorDistancia() { return menorDistancia; }
}