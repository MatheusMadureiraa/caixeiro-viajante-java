package tsp;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class TSPServer {

    public static void main(String[] args) {
        // --- CONFIGURAÇÃO DO PROBLEMA ---
        // Matriz de distâncias (exemplo para 4 cidades, mas deve ser usada para mais)
        // Para testes reais, use matrizes maiores.
        final int NUM_CITIES = 11; // Mude aqui para testar diferentes proporções
        final int[][] distanceMatrix = generateMatrix(NUM_CITIES);

        System.out.println("Iniciando servidor para resolver o PCV com " + NUM_CITIES + " cidades.");
        
        long startTime = System.nanoTime();

        try {
            // Obtém o registro RMI
            // Substitua "localhost" pelo IP do seu servidor
            Registry registry = LocateRegistry.getRegistry("localhost");

            // Lista os serviços (workers) registrados
            String[] registered = registry.list();
            List<TSPInterface> workers = new ArrayList<>();
            for (String name : registered) {
                if (name.startsWith("TSPWorker")) {
                    System.out.println("Encontrado worker: " + name);
                    TSPInterface worker = (TSPInterface) registry.lookup(name);
                    workers.add(worker);
                }
            }
            
            if (workers.isEmpty()) {
                System.err.println("Nenhum worker encontrado. A aplicação será encerrada.");
                return;
            }

            // --- DIVISÃO DO TRABALHO ---
            // A cidade inicial é sempre a 0.
            // Dividimos o trabalho com base na segunda cidade da rota.
            // Rota: 0 -> 1 -> ... | 0 -> 2 -> ... | 0 -> 3 -> ...
            List<List<Integer>> subproblems = new ArrayList<>();
            for (int i = 1; i < NUM_CITIES; i++) {
                List<Integer> partialPath = new ArrayList<>();
                partialPath.add(0);
                partialPath.add(i);
                subproblems.add(partialPath);
            }

            // --- DISTRIBUIÇÃO E PROCESSAMENTO ---
            List<Result> results = new ArrayList<>();
            int workerIndex = 0;
            
            // Usaremos threads no servidor para chamar os workers em paralelo
            List<Thread> serverThreads = new ArrayList<>();

            for (List<Integer> subproblem : subproblems) {
                final TSPInterface worker = workers.get(workerIndex);
                
                Thread thread = new Thread(() -> {
                    try {
                        System.out.println("Enviando subproblema " + subproblem + " para o worker...");
                        Result result = worker.solveTSP(subproblem, distanceMatrix);
                        synchronized (results) {
                            results.add(result);
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao comunicar com o worker: " + e.getMessage());
                        // Lógica para reatribuir a tarefa seria necessária em um sistema robusto
                    }
                });
                
                serverThreads.add(thread);
                thread.start();
                
                // Distribuição Round-Robin entre os workers
                workerIndex = (workerIndex + 1) % workers.size();
            }

            // Espera todas as chamadas remotas terminarem
            for (Thread t : serverThreads) {
                t.join();
            }

            // --- CONSOLIDAÇÃO DOS RESULTADOS ---
            Result bestResult = null;
            for (Result res : results) {
                if (bestResult == null || res.cost < bestResult.cost) {
                    bestResult = res;
                }
            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // em milissegundos

            if (bestResult != null) {
                System.out.println("\n--- RESULTADO FINAL ---");
                System.out.println("Melhor rota encontrada: " + bestResult.path);
                System.out.println("Custo total: " + bestResult.cost);
                System.out.println("Tempo de execução distribuído: " + duration + " ms");
            } else {
                System.out.println("Nenhum resultado foi retornado pelos workers.");
            }

        } catch (Exception e) {
            System.err.println("Exceção no Servidor: " + e.toString());
            e.printStackTrace();
        }
    }

    // Função para gerar uma matriz de distância aleatória e simétrica
    public static int[][] generateMatrix(int n) {
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else {
                    int dist = (int) (Math.random() * 100) + 1; // Distâncias entre 1 e 100
                    matrix[i][j] = dist;
                    matrix[j][i] = dist;
                }
            }
        }
        return matrix;
    }
}