import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnalisadorSintatico {
    private final List<Token> tokens;
    private final List<Mensagem> mensagensLexicas;
    private final List<Mensagem> erros = new ArrayList<>();
    private int indice = 0;
    private boolean panico = false;
    private int profundidade = 0;


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

    private void registrarErroSintatico(int ini, int fim, int lin, int col, String texto) {
        erros.add(new Mensagem(ini, fim, lin, col, Severidade.ERRO, texto, "Erro Sintático"));
    }

    private void reportarErro(String texto){
        if(!panico){
            Token t = devolveTokenAtual();
            registrarErroSintatico(t.getInicio(), t.getFim(), t.getLinha(), t.getColuna(), texto);
            panico = true;
        }
    }

}
