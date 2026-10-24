import java.util.List;

public interface VisaoResultados {
    void exibirMensagens(List<Mensagem> mensagens);
    void exibirTokens(List<Token> tokens);
    void exibirSimbolos(List<Simbolo> simbolos);
    void exibirStatus(String status);
}