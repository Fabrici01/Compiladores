/** Modelo: um token reconhecido (tipo, lexema, posição e atributo). Imutável. */
final class Token {
    private final String tipo, lexema, atributo;
    private final int linha, coluna, inicio, fim;
    Token(String tipo, String lexema, int linha, int coluna, int inicio, int fim, String atributo) {
        this.tipo = tipo; this.lexema = lexema; this.linha = linha; this.coluna = coluna;
        this.inicio = inicio; this.fim = fim; this.atributo = atributo;
    }
    String getTipo() { return tipo; }
    String getLexema() { return lexema; }
    int getLinha() { return linha; }
    int getColuna() { return coluna; }
    int getInicio() { return inicio; }
    int getFim() { return fim; }
    String getAtributo() { return atributo; }
}