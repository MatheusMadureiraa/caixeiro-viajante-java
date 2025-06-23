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
            //Carrega os dados das cidades a partir de um arquivo de texto.
            List<CidadePCV> cidades = carregarCidadesDeArquivo("cidades.txt");
            
            System.out.println("\n--- Executando Algoritmo de Forca Bruta Distribuido ---");
            
            // Medição de tempo: marca o início da execução.
            long inicio = System.nanoTime();
            
            // Chama o método principal que resolve o problema de forma distribuída.
            ResultadoPCV resultadoFinal = resolverDistribuido(cidades);
            
            // Medição de tempo: calcula a duração total em nanossegundos.
            long tempoTotal = (System.nanoTime() - inicio);

            // Se uma rota válida foi encontrada, imprime os resultados.
            if (resultadoFinal != null && resultadoFinal.getRota() != null) {
                System.out.printf("\nExecucao Distribuida concluida em %ds\n", tempoTotal / 1_000_000_000);
                System.out.printf("Em ms: %dms\n", tempoTotal / 1_000_000);
                
                System.out.println("Melhor Rota Global: " + resultadoFinal.getRota().stream().map(CidadePCV::getNome).collect(Collectors.joining(" -> ")));
                
                System.out.printf("Distancia Total: %.2f\n", resultadoFinal.getDistancia());
            } else {
                System.out.println("Nenhuma solucao foi encontrada.");
            }

        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo de cidades: " + e.getMessage());
        }
    }

    // Método que orquestra a resolução distribuída do problema.
    public static ResultadoPCV resolverDistribuido(List<CidadePCV> cidades) {
        // Checagem de segurança para evitar erros com listas vazias.
        if (cidades == null || cidades.size() < 2) return null;

        // Variável para armazenar o melhor resultado encontrado entre todos os workers.
        ResultadoPCV melhorGlobal = new ResultadoPCV(null, Double.POSITIVE_INFINITY);

        ExecutorService executorDoServidor = null;

        try {
            // Conecta-se ao serviço de registro RMI que está rodando.
            Registry registry = LocateRegistry.getRegistry("localhost");
            
            // Lista para armazenar as referências (stubs) dos workers remotos.
            List<TSPInterface> workers = new ArrayList<>();
            
            // Procura no registro por todos os serviços com nome "TSPWorker".
            for (String name : registry.list()) {
                if (name.startsWith("TSPWorker")) {
                    System.out.println("SERVIDOR: Encontrado worker: " + name);
                    workers.add((TSPInterface) registry.lookup(name));
                }
            }

            // Se nenhum worker for encontrado, o programa não pode continuar.
            if (workers.isEmpty()) {
                System.err.println("Nenhum worker RMI encontrado. Abortando.");
                return null;
            }
            System.out.println(cidades.size() + " cidades, usando " + workers.size() + " workers...");

            // Define a cidade de origem (a primeira da lista) como fixa para todas as rotas.
            final CidadePCV origem = cidades.get(0);
            
            // Cria uma CÓPIA da sublista de cidades. Isso é crucial para que seja 'Serializable'.
            final List<CidadePCV> cidadesParaPermutar = new ArrayList<>(cidades.subList(1, cidades.size()));

            // Cria um pool de threads no servidor do tamanho do número de workers.
            executorDoServidor = Executors.newFixedThreadPool(workers.size());
            List<Future<ResultadoPCV>> futurosResultados = new ArrayList<>();
            int workerIndex = 0;

            // Loop principal de distribuição: para cada "segunda cidade" possível, cria uma tarefa.
            for (final CidadePCV segundaCidade : cidadesParaPermutar) {
                final TSPInterface worker = workers.get(workerIndex);
                final int indexDaTarefa = workerIndex;
                
                // Submete a tarefa para o pool de threads do servidor.
                Future<ResultadoPCV> future = executorDoServidor.submit(() -> {
                    System.out.println("SERVIDOR: Enviando tarefa para " + segundaCidade.getNome() + " para o worker " + indexDaTarefa);
                    // A chamada de rede (RMI). O servidor chama o método no worker.
                    return worker.resolverSubRota(origem, segundaCidade, cidadesParaPermutar);
                });
                futurosResultados.add(future);
                
                workerIndex = (workerIndex + 1) % workers.size();
            }

            // Loop de agregação: agora, o servidor espera a resposta de todas as tarefas.
            for (Future<ResultadoPCV> f : futurosResultados) {
                // O método .get() bloqueia a execução até que o resultado daquela tarefa chegue.
                ResultadoPCV resultadoParcial = f.get();
                
                // Compara o resultado do worker com o melhor resultado global encontrado até agora.
                if (resultadoParcial != null && resultadoParcial.getDistancia() < melhorGlobal.getDistancia()) {
                    melhorGlobal = resultadoParcial;
                }
            }

        } catch (Exception e) {
            System.err.println("Excecao no Servidor: " + e.toString());
            e.printStackTrace();
        } finally {
            // garante que o pool de threads do servidor seja encerrado.
            if (executorDoServidor != null) {
                executorDoServidor.shutdown();
            }
        }
        return melhorGlobal;
    }
    
    // Método utilitário para ler as cidades de um arquivo.
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