public final class Token {
    private final String tipo;
    private final String lexema;
    private final String atributo;
    
    private final int linha;
    private final int coluna;
    private final int inicio;
    private final int fim;

    public Token(String tipo, String lexema, int linha, int coluna, int inicio, int fim, String atributo){
        this.tipo = tipo; 
        this.lexema = lexema;
        this.linha = linha;
        this.coluna = coluna;
        this.inicio = inicio;
        this.fim = fim;
        this.atributo = atributo;
    }

    public String getTipo(){
        return tipo; 
    }

    public String getLexema(){ 
        return lexema; 
    }
    
    public int getLinha(){ 
        return linha; 
    }
    
    public int getColuna(){ 
        return coluna; 
    }
    
    public int getInicio(){ 
        return inicio; 
    }
    
    public int getFim(){ 
        return fim; 
    }
    
    public String getAtributo(){ 
        return atributo; 
    }
}