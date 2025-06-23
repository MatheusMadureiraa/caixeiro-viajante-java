import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Renomeado de BuscaDePermutacaoThread para maior clareza. O código interno é o seu.
class CalculadorDePermutacao {
    private List<CidadePCV> melhorRotaLocal;
    private double menorDistanciaLocal = Double.POSITIVE_INFINITY;
    private final CidadePCV cidadeOrigem;
    private final CidadePCV cidadeInicialDaPermutacao;

    public CalculadorDePermutacao(CidadePCV origem, CidadePCV inicialDaPermutacao) {
        this.cidadeOrigem = origem;
        this.cidadeInicialDaPermutacao = inicialDaPermutacao;
    }
    
    // Seu método permutar() permanece aqui, inalterado...
    public void permutar(List<CidadePCV> cidades, int k) {
        if (k == cidades.size()) {
            double distanciaAtual = calcularDistanciaDaRota(cidades);
            if (distanciaAtual < this.menorDistanciaLocal) {
                this.menorDistanciaLocal = distanciaAtual;
                this.melhorRotaLocal = new ArrayList<>();
                this.melhorRotaLocal.add(cidadeOrigem);
                this.melhorRotaLocal.add(cidadeInicialDaPermutacao);
                this.melhorRotaLocal.addAll(cidades);
                this.melhorRotaLocal.add(cidadeOrigem);
            }
        } else {
            for (int i = k; i < cidades.size(); i++) {
                Collections.swap(cidades, k, i);
                permutar(cidades, k + 1);
                Collections.swap(cidades, k, i);
            }
        }
    }
    
    // Seu método calcularDistanciaDaRota() permanece aqui, inalterado...
    private double calcularDistanciaDaRota(List<CidadePCV> cidadesPermutadas) {
        double distancia = 0;
        CidadePCV anterior = cidadeInicialDaPermutacao;
        distancia += cidadeOrigem.distanciaPara(anterior);
        for (CidadePCV atual : cidadesPermutadas) {
            distancia += anterior.distanciaPara(atual);
            anterior = atual;
        }
        distancia += anterior.distanciaPara(cidadeOrigem);
        return distancia;
    }

    public List<CidadePCV> getMelhorRotaLocal() { return melhorRotaLocal; }
    public double getMenorDistanciaLocal() { return menorDistanciaLocal; }
}