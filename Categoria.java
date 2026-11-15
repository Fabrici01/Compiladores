public enum Categoria{
    PALAVRA_RESERVADA("palavra reservada"),
    OPERADOR("operador"),
    PONTUACAO("operador/pontuação"),
    IDENTIFICADOR("identificador"),
    VARIAVEL("variável"),
    CONSTANTE("constante"),
    TIPO("tipo"),
    PROCEDIMENTO("procedimento"),
    FUNCAO("função"),
    PARAMETRO("parâmetro");

    private final String rotulo;

    Categoria(String rotulo){
        this.rotulo = rotulo;
    }

    @Override
    public String toString(){
        return rotulo;
    }
}
