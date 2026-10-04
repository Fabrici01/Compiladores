public final class Simbolo {
    private final String nome;
    private final String categoria;
    private final String primeiraLinha;

    public Simbolo(String nome, String categoria, String primeiraLinha){
        this.nome = nome;
        this.categoria = categoria;
        this.primeiraLinha = primeiraLinha;       
    }

    public String getNome(){
        return nome; 
    }

    public String getCategoria(){
        return categoria; 
    }

    public String getPrimeiraLinha(){ 
        return primeiraLinha; 
    }
}