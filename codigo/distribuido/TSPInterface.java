package tsp;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

// Define a interface para o objeto remoto (Worker)
public interface TSPInterface extends Remote {
    // Método que o servidor chamará para que o worker resolva um subproblema
    Result solveTSP(List<Integer> partialPath, int[][] distanceMatrix) throws RemoteException;
}