package tsp;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Classe auxiliar para encapsular o resultado
class Result implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public int cost;
    public List<Integer> path;

    public Result(int cost, List<Integer> path) {
        this.cost = cost;
        this.path = path;
    }
}

public class TSPWorker implements TSPInterface {

    public TSPWorker() {
        super();
    }
    
    // A implementação real do resolvedor de força bruta para um subproblema
    @Override
    public Result solveTSP(List<Integer> partialPath, int[][] distanceMatrix) throws RemoteException {
        System.out.println("Recebi uma tarefa para processar a rota parcial: " + partialPath);
        
        List<Integer> citiesToVisit = new ArrayList<>();
        int numCities = distanceMatrix.length;
        for (int i = 0; i < numCities; i++) {
            if (!partialPath.contains(i)) {
                citiesToVisit.add(i);
            }
        }

        List<Integer> bestPath = null;
        int minCost = Integer.MAX_VALUE;

        // Gera todas as permutações das cidades restantes
        List<List<Integer>> permutations = new ArrayList<>();
        generatePermutations(citiesToVisit, 0, permutations);
        
        for (List<Integer> p : permutations) {
            List<Integer> currentPath = new ArrayList<>(partialPath);
            currentPath.addAll(p);
            
            int currentCost = 0;
            for (int i = 0; i < currentPath.size() - 1; i++) {
                currentCost += distanceMatrix[currentPath.get(i)][currentPath.get(i+1)];
            }
            // Adiciona o custo de voltar à cidade inicial
            currentCost += distanceMatrix[currentPath.get(currentPath.size()-1)][currentPath.get(0)];
            
            if (currentCost < minCost) {
                minCost = currentCost;
                bestPath = new ArrayList<>(currentPath);
            }
        }
        System.out.println("Tarefa concluída. Custo mínimo encontrado: " + minCost);
        return new Result(minCost, bestPath);
    }
    
    // Função auxiliar para gerar permutações
    private void generatePermutations(List<Integer> arr, int k, List<List<Integer>> permutations) {
        for(int i = k; i < arr.size(); i++){
            Collections.swap(arr, i, k);
            generatePermutations(arr, k+1, permutations);
            Collections.swap(arr, k, i);
        }
        if (k == arr.size() -1){
            permutations.add(new ArrayList<>(arr));
        }
    }

    public static void main(String[] args) {
        try {
            // Cria e exporta uma instância do objeto remoto (o próprio worker)
            TSPWorker worker = new TSPWorker();
            
            // O "stub" é a representação do objeto remoto que o servidor usará
            TSPInterface stub = (TSPInterface) UnicastRemoteObject.exportObject(worker, 0);

            // Obtém o registro RMI (espera-se que o rmiregistry esteja rodando)
            // Substitua "localhost" pelo IP do servidor se estiver em máquinas diferentes
            Registry registry = LocateRegistry.getRegistry("localhost");
            
            // Registra o stub no registro com um nome único
            // Ex: "TSPWorker1", "TSPWorker2", etc.
            String workerName = "TSPWorker" + System.currentTimeMillis();
            registry.rebind(workerName, stub);

            System.out.println("Worker '" + workerName + "' pronto e registrado.");

        } catch (Exception e) {
            System.err.println("Exceção no Worker: " + e.toString());
            e.printStackTrace();
        }
    }
}