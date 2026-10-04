import java.util.ArrayList;
import java.util.List;

public final class Varredura {
    private final String texto; 
    private final int tamanho;
    private int posicao = 0;
    private int linha = 1;
    private int coluna = 1;
    final List<Token> tokens = new ArrayList<>();
    final List<Mensagem> mensagens = new ArrayList<>();

    public Varredura(String texto){ 
        this.texto = texto; 
        this.tamanho = texto.length(); 
    }

    private char espiar(int deslocamento){ 
        int p = posicao + deslocamento; 

        if (p < tamanho){
            return texto.charAt(p);
        }
        return '\0';
    }

    private char atual(){ 
        return texto.charAt(posicao); 
    }
    
    private void avancar(){ 
        if(atual() == '\n'){ 
            linha++; 
            coluna = 1; 
        } else{
            coluna++; 
        }
        posicao++; 
    }

    private static boolean ehLetra(char c){ 
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); 
    }

    private static boolean ehDigito(char c){ 
        return c >= '0' && c <= '9'; 
    }

    private static boolean ehLetraDigitoOuUnderline(char c){ 
        return ehLetra(c) || ehDigito(c) || c == '_';
    }

    private void registrarErro(int ini, int fim, int lin, int col, String texto) {
        mensagens.add(new Mensagem(ini, fim, lin, col, Severidade.ERRO, texto));
    }

    private void criarToken(String tipo, int ini, int lin, int col, String atributo) {
        tokens.add(new Token(tipo, texto.substring(ini, posicao), lin, col, ini, posicao, atributo));
    }

    private void consumirComentarioDeLinha(){ 
        while(posicao < tamanho && atual() != '\n'){
            avancar(); 
        }
    }

    private void consumirComentarioDeBloco(int ini, int lin, int col) {
        char abertura = atual();
        String fechamento;

        if(abertura == '{') {
            fechamento = "}";
        } else {
            fechamento = "*)";
        }

        avancar(); 
        
        if(abertura == '('){
            avancar();
        }

        while(posicao < tamanho) {
            if(texto.startsWith(fechamento, posicao)){
                for (int k = 0; k < fechamento.length(); k++){
                    avancar(); 
                }
                return; 
            }
            avancar();
        }
        registrarErro(ini, ini + (abertura == '{' ? 1 : 2), lin, col, "Comentário não fechado");
    }

    private void lerIdentificadorOuReservada(int ini, int lin, int col){
        while(posicao < tamanho && ehLetraDigitoOuUnderline(atual())){
            avancar();
        }

        String palavra = texto.substring(ini, posicao);
        String maiuscula = palavra.toUpperCase();

        if(palavra.length() > Vocabulario.TAMANHO_MAXIMO_IDENTIFICADOR){
            registrarErro(ini, posicao, lin, col, "Identificador '" + palavra + "' excede "
                + Vocabulario.TAMANHO_MAXIMO_IDENTIFICADOR + " caracteres");

        } else if (Vocabulario.PALAVRAS_RESERVADAS.contains(maiuscula)){
            criarToken(maiuscula, ini, lin, col, "-");
        } else {
            criarToken("ID", ini, lin, col, palavra.toLowerCase());
        }
    }

    private void lerNumero(int ini, int lin, int col){
        while(posicao < tamanho && ehDigito(atual())){
            avancar();
        }

        if(espiar(0) == '.' && ehDigito(espiar(1))){ 
            avancar(); 
            while(posicao < tamanho && ehDigito(atual())){
                avancar();
            } 
        }

        if(ehLetra(espiar(0)) || espiar(0) == '_'){
            while(posicao < tamanho && ehLetraDigitoOuUnderline(atual())){
                avancar();
            }

            registrarErro(ini, posicao, lin, col, "Número mal formado (identificador não pode começar com dígito)");
            return;
        }
        criarToken("NUM", ini, lin, col, texto.substring(ini, posicao));
    }

    private void lerLiteral(int ini, int lin, int col){
        avancar();
        while(posicao < tamanho && atual() != '\'' && atual() != '\n'){
            avancar();
        }

        if(espiar(0) == '\''){ 
            avancar(); 
            criarToken("LITERAL", ini, lin, col, texto.substring(ini + 1, posicao - 1));
        }else{ 
            registrarErro(ini, posicao, lin, col, "Literal não fechado");
        }
    }

    private boolean lerOperador(int ini, int lin, int col){
        for(String op : Vocabulario.OPERADORES_DUPLOS){
            if(texto.startsWith(op, posicao)){ 
                avancar(); 
                avancar(); 
                criarToken(op, ini, lin, col, "-"); 
                return true; 
            }
        }
        if(Vocabulario.OPERADORES_SIMPLES.indexOf(atual()) >= 0) {
            avancar(); 
            criarToken(String.valueOf(texto.charAt(ini)), ini, lin, col, "-"); 
            return true;
        }
        return false;
    }

    // Recuperação de erros (modo pânico).
    private void recuperarErroModoPanico(int ini, int lin, int col, char invalido){
        avancar();
        while(posicao < tamanho && !Character.isWhitespace(atual())
                && Vocabulario.DELIMITADORES_SINCRONIZACAO.indexOf(atual()) < 0){
            avancar();
        }
        registrarErro(ini, posicao, lin, col, "Caractere inválido '" + invalido + "'");
    }

    //Simula o AFD escolhendo o caminho pelo primeiro caractere.
    public void executar() {
        while(posicao < tamanho) {
            char c = atual(); 
            int ini = posicao;
            int lin = linha;
            int col = coluna;

            if(Character.isWhitespace(c)){ 
                avancar(); 
                continue; 
            }

            if(c == '/' && espiar(1) == '/'){ 
                consumirComentarioDeLinha(); 
                continue; 
            }
            
            if(c == '{' || (c == '(' && espiar(1) == '*')){ 
                consumirComentarioDeBloco(ini, lin, col); 
                continue; 
            }

            if(ehLetra(c)){ 
                lerIdentificadorOuReservada(ini, lin, col); 
                continue; 
            }

            if(ehDigito(c)){ 
                lerNumero(ini, lin, col); 
                continue; 
            }

            if(c == '\''){ 
                lerLiteral(ini, lin, col);
                continue; 
            }
            
            if(lerOperador(ini, lin, col)){
                continue;
            }

            recuperarErroModoPanico(ini, lin, col, c);
        }
    }
}