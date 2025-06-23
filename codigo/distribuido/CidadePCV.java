import java.io.Serializable;

public class CidadePCV implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nome;
    private final int x;
    private final int y;

    public CidadePCV(String nome, int x, int y) {
        this.nome = nome;
        this.x = x;
        this.y = y;
    }

    public String getNome() { return nome; }
    public int getX() { return x; }
    public int getY() { return y; }

    public double distanciaPara(CidadePCV outra) {
        double dx = this.x - outra.x;
        double dy = this.y - outra.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return nome;
    }
}