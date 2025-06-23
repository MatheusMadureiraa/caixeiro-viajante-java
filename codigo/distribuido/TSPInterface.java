import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TSPInterface extends Remote {
    /**
     * Método que será chamado remotamente pelo Servidor.
     * Ele resolve um subconjunto do problema do caixeiro viajante.
     * @param origem A cidade inicial global da rota.
     * @param segundaCidade A segunda cidade da rota, que define o subproblema desta tarefa.
     * @param cidadesParaPermutar A lista de cidades restantes a serem permutadas.
     * @return O melhor resultado (rota e distância) encontrado para este subproblema.
     * @throws RemoteException
     */
    ResultadoPCV resolverSubRota(CidadePCV origem, CidadePCV segundaCidade, List<CidadePCV> cidadesParaPermutar) throws RemoteException;
}