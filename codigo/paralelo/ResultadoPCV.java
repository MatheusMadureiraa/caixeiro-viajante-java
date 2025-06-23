import java.util.List;

public class ResultadoPCV {
    private final List<CidadePCV> rota;
    private final double distancia;

    public ResultadoPCV(List<CidadePCV> rota, double distancia) {
        this.rota = rota;
        this.distancia = distancia;
    }

    public List<CidadePCV> getRota() {
        return rota;
    }

    public double getDistancia() {
        return distancia;
    }
}