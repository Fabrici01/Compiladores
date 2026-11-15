public final class Simbolo {
    private final String nome;
    private final Categoria categoria;
    private final Tipo tipo;
    private final int linha;

    public Simbolo(String nome, Categoria categoria, Tipo tipo, int linha){
        this.nome = nome;
        this.categoria = categoria;
        this.tipo = tipo;
        this.linha = linha;
    }

    public Simbolo(String nome, Categoria categoria){
        this.nome = nome;
        this.categoria = categoria;
        this.tipo = Tipo.VAZIO;
        this.linha = 0;
    }

    public String getNome(){
        return this.nome;
    }

    public Categoria getCategoria(){
        return this.categoria;
    }

    public Tipo getTipo(){
        return this.tipo;
    }

    public int getLinha(){
        return this.linha;
    }

    public String getPrimeiraLinha(){
        if(this.linha <= 0){
            return "-";
        }

        return String.valueOf(this.linha);
    }
}
