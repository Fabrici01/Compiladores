import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnalisadorSintatico {
    private final List<Token> tokens;
    private final List<Mensagem> mensagensLexicas;
    private final List<Mensagem> erros = new ArrayList<>();
    private final List<Mensagem> avisos = new ArrayList<>();
    private int indice = 0;
    private boolean panico = false;
    private int profundidade = 0;
    private static Set<String> INICIO_EXPRESSAO = Set.of("(", "+", "-", "ID", "LITERAL", "NUM");
    private static Set<String> INICIO_INSTRUCAO = Set.of("ID", "IF", "WHILE", "REPEAT", "FOR", "BREAK", "CONTINUE", "BEGIN");
    private static Set<String> OPERADORES_RELACIONAIS = Set.of("=", "<>", "<", "<=", ">", ">=");
    private static Set<String> SINCRONIZA = Set.of(";", ")", "]", ",", "THEN", "DO", "TO", "DOWNTO",
        "BEGIN", "END", "VAR", "CONST", "TYPE", "PROCEDURE", "FUNCTION", "IF", "WHILE", "FOR", "REPEAT", "UNTIL");

    public AnalisadorSintatico(List<Token> tokens, List<Mensagem> mensagensLexicas) {
        this.tokens = tokens;
        this.mensagensLexicas = mensagensLexicas;
    }

    private Token devolveTokenAtual(){
        if(indice >= tokens.size()){
            return tokens.get(tokens.size() - 1);
        }

        return tokens.get(indice);
    }

    private void avancaToken(){
        if (!devolveTokenAtual().getTipo().equals("EOF")){
            indice++;
        }
    }

    private boolean pertence(Set<String> conjunto){
        return conjunto.contains(devolveTokenAtual().getTipo());
    }

    private void registrarErroSintatico(Token t, String texto) {
        erros.add(new Mensagem(t.getInicio(), t.getFim(), t.getLinha(), t.getColuna(), Severidade.ERRO, texto, "Erro Sintático"));
    }

    private void registrarAviso(Token t, String texto) {
        avisos.add(new Mensagem(t.getInicio(), t.getFim(), t.getLinha(), t.getColuna(), Severidade.AVISO, texto, "Aviso"));
    }

    private void reportarErro(String texto){
        if(!panico){
            Token t = devolveTokenAtual();
            registrarErroSintatico(t, texto);
            panico = true;
        }
    }

    private void  sincronizar(){
        if(panico){
            while(!pertence(SINCRONIZA) && !devolveTokenAtual().getTipo().equals("EOF")){
                avancaToken();
            }
        }
    }

    private boolean match(String tipo){
        if(devolveTokenAtual().getTipo().equals(tipo)){
            avancaToken();
            return true;
        } else {
            reportarErro("Esperado: " + tipo + ", encontrado: " + devolveTokenAtual().getTipo());
            return false;
        }
    }
}
