/** Modelo: uma linha da tabela de símbolos. */
final class Simbolo {
    private final String nome, categoria, primeiraLinha;
    Simbolo(String nome, String categoria, String primeiraLinha) {
        this.nome = nome; this.categoria = categoria; this.primeiraLinha = primeiraLinha;
    }
    String getNome() { return nome; }
    String getCategoria() { return categoria; }
    String getPrimeiraLinha() { return primeiraLinha; }
}