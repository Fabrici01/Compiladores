/** Modelo: erro léxico ou aviso, com posição no texto (inicio/fim), linha e coluna. */
final class Mensagem {
    enum Severidade { ERRO, AVISO }
    private final int inicio, fim, linha, coluna;
    private final Severidade severidade;
    private final String texto;
    Mensagem(int inicio, int fim, int linha, int coluna, Severidade severidade, String texto) {
        this.inicio = inicio; this.fim = Math.max(fim, inicio + 1); this.linha = linha;
        this.coluna = coluna; this.severidade = severidade; this.texto = texto;
    }
    int getInicio() { return inicio; }
    int getFim() { return fim; }
    int getLinha() { return linha; }
    int getColuna() { return coluna; }
    Severidade getSeveridade() { return severidade; }
    boolean ehErro() { return severidade == Severidade.ERRO; }
    @Override public String toString() { return "[" + linha + ":" + coluna + "] " + texto; }
}