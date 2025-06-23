import java.io.Serializable;
import java.util.List;

public class ResultadoPCV implements Serializable {
    // Adicione esta linha
    private static final long serialVersionUID = 2L;

    private final List<CidadePCV> rota;
    private final double distancia;

    // O resto do seu código permanece exatamente o mesmo...
    public ResultadoPCV(List<CidadePCV> rota, double distancia) {
        this.rota = rota;
        this.distancia = distancia;
    }

    public List<CidadePCV> getRota() { return rota; }
    public double getDistancia() { return distancia; }
}