public class Cidade {
    String nome;
    int x;
    int y;

    public Cidade(String nome, int x, int y) {
        this.nome = nome;
        this.x = x;
        this.y = y;
    }

    public double distanciaPara(Cidade outra) {
        int deltaX = this.x - outra.x;
        int deltaY = this.y - outra.y;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    @Override
    public String toString() {
        return this.nome;
    }
}