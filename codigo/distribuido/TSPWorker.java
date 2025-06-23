import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class TSPWorker implements TSPInterface {

    public TSPWorker() {}

    @Override
    public ResultadoPCV resolverSubRota(CidadePCV origem, CidadePCV segundaCidade, List<CidadePCV> cidadesParaPermutar) throws RemoteException {
        System.out.println("WORKER: Recebida tarefa para rota iniciando com " + origem.getNome() + " -> " + segundaCidade.getNome());
        
        List<CidadePCV> restoDasCidades = new ArrayList<>(cidadesParaPermutar);
        restoDasCidades.remove(segundaCidade);

        CalculadorDePermutacao calculador = new CalculadorDePermutacao(origem, segundaCidade);
        calculador.permutar(restoDasCidades, 0);

        System.out.println("WORKER: Tarefa " + segundaCidade.getNome() + " concluída. Menor distância local: " + String.format("%.2f", calculador.getMenorDistanciaLocal()));
        return new ResultadoPCV(calculador.getMelhorRotaLocal(), calculador.getMenorDistanciaLocal());
    }

    public static void main(String[] args) {
        try {
            String host = (args.length < 1) ? "localhost" : args[0];
            
            TSPWorker worker = new TSPWorker();
            TSPInterface stub = (TSPInterface) UnicastRemoteObject.exportObject(worker, 0);

            Registry registry = LocateRegistry.getRegistry(host);
            
            // Cria um nome único para cada worker que se conecta
            String workerName = "TSPWorker-" + System.currentTimeMillis();
            registry.rebind(workerName, stub);

            System.out.println("Worker '" + workerName + "' pronto e registrado no host '" + host + "'.");

        } catch (Exception e) {
            System.err.println("Excecao no Worker: " + e.toString());
            e.printStackTrace();
        }
    }
}