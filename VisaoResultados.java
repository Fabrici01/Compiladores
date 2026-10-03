import java.util.List;

/** Contrato da View para os painéis de resultado (interface segregada). */
interface VisaoResultados {
    void exibirMensagens(List<Mensagem> mensagens);
    void exibirTokens(List<Token> tokens);
    void exibirSimbolos(List<Simbolo> simbolos);
    void exibirStatus(String status);
}