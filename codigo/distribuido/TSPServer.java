import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class TSPServer {

    public static void main(String[] args) {
        try {
            List<CidadePCV> cidades = carregarCidadesDeArquivo("cidades.txt");
            
            System.out.println("\n--- Executando Algoritmo de Forca Bruta Distribuido ---");
            long inicio = System.nanoTime();
            ResultadoPCV resultadoFinal = resolverDistribuido(cidades);
            long tempoTotal = (System.nanoTime() - inicio) / 1_000_000;

            if (resultadoFinal != null && resultadoFinal.getRota() != null) {
                System.out.printf("\nExecucao Distribuida concluida em %dms\n", tempoTotal);
                System.out.println("Melhor Rota Global: " + resultadoFinal.getRota().stream().map(CidadePCV::getNome).collect(Collectors.joining(" -> ")));
                System.out.printf("Distancia Total: %.2f\n", resultadoFinal.getDistancia());
            } else {
                System.out.println("Nenhuma solucao foi encontrada.");
            }

        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo de cidades: " + e.getMessage());
        }
    }

    public static ResultadoPCV resolverDistribuido(List<CidadePCV> cidades) {
        if (cidades == null || cidades.size() < 2) return null;
        if (cidades.size() > 13) {
            System.out.println("AVISO: Forca Bruta e muito lenta para mais de 13 cidades. Execucao abortada.");
            return new ResultadoPCV(null, Double.POSITIVE_INFINITY);
        }

        ResultadoPCV melhorGlobal = new ResultadoPCV(null, Double.POSITIVE_INFINITY);
        ExecutorService executorDoServidor = null;

        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            List<TSPInterface> workers = new ArrayList<>();
            for (String name : registry.list()) {
                if (name.startsWith("TSPWorker")) {
                    System.out.println("SERVIDOR: Encontrado worker: " + name);
                    workers.add((TSPInterface) registry.lookup(name));
                }
            }

            if (workers.isEmpty()) {
                System.err.println("Nenhum worker RMI encontrado. Abortando.");
                return null;
            }
            System.out.println(cidades.size() + " cidades, usando " + workers.size() + " workers...");

            // Lógica de divisão do trabalho (idêntica à sua versão paralela)
            final CidadePCV origem = cidades.get(0);
            final List<CidadePCV> cidadesParaPermutar = cidades.subList(1, cidades.size());

            // O servidor usa threads para fazer as chamadas remotas em paralelo
            executorDoServidor = Executors.newFixedThreadPool(workers.size());
            List<Future<ResultadoPCV>> futurosResultados = new ArrayList<>();
            int workerIndex = 0;

            for (final CidadePCV segundaCidade : cidadesParaPermutar) {
                final TSPInterface worker = workers.get(workerIndex);
                
                // Submete uma tarefa que FAZ a chamada RMI
                Future<ResultadoPCV> future = executorDoServidor.submit(() -> {
                    System.out.println("SERVIDOR: Enviando tarefa para " + segundaCidade.getNome() + " para o worker " + workerIndex);
                    // A chamada de rede acontece aqui!
                    return worker.resolverSubRota(origem, segundaCidade, cidadesParaPermutar);
                });
                futurosResultados.add(future);
                
                // Delega para o próximo worker (Round-Robin)
                workerIndex = (workerIndex + 1) % workers.size();
            }

            // Agrega os resultados (igual ao seu código paralelo)
            for (Future<ResultadoPCV> f : futurosResultados) {
                ResultadoPCV resultadoParcial = f.get();
                if (resultadoParcial != null && resultadoParcial.getDistancia() < melhorGlobal.getDistancia()) {
                    melhorGlobal = resultadoParcial;
                }
            }

        } catch (Exception e) {
            System.err.println("Excecao no Servidor: " + e.toString());
            e.printStackTrace();
        } finally {
            if (executorDoServidor != null) {
                executorDoServidor.shutdown();
            }
        }
        return melhorGlobal;
    }
    
    // Seu método de carregar cidades, pode ser copiado para cá
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