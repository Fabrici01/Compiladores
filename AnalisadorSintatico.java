import java.util.ArrayList;
import java.util.List;

public class AnalisadorSintatico {
    private final List<Token> tokens;
    private final List<Mensagem> mensagensLexicas;
    private final List<Mensagem> erros = new ArrayList<>();


    public AnalisadorSintatico(List<Token> tokens, List<Mensagem> mensagensLexicas) {
        this.tokens = tokens;
        this.mensagensLexicas = mensagensLexicas;
    }

    private void registrarErroSintatico(int ini, int fim, int lin, int col, String texto) {
        erros.add(new Mensagem(ini, fim, lin, col, Severidade.ERRO, texto, "Erro Sintático"));
    }
}
