import java.util.List;

public class Resultado {
    private final List<Cidade> rota;
    private final double distancia;

    public Resultado(List<Cidade> rota, double distancia) {
        this.rota = rota;
        this.distancia = distancia;
    }

    public List<Cidade> getRota() { return rota; }
    public double getDistancia() { return distancia; }
}