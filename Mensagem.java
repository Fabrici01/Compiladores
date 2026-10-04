public final class Mensagem {
    private final int inicio;
    private final int fim;
    private final int linha;
    private final int coluna;
    
    private final Severidade severidade;
    private final String texto;
    
    public Mensagem(int inicio, int fim, int linha, int coluna, Severidade severidade, String texto){
        this.inicio = inicio;
        this.fim = Math.max(fim, inicio + 1);
        this.linha = linha;
        this.coluna = coluna;
        this.severidade = severidade;
        this.texto = texto;
    }

    public int getInicio(){ 
        return inicio; 
    }

    public int getFim(){ 
        return fim; 
    }

    public boolean ehErro(){ 
        return severidade == Severidade.ERRO; 
    }
    
    @Override 
    public String toString(){ 
        return "[" + linha + ":" + coluna + "] " + texto; 
    }
}