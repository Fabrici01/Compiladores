import java.util.List;

/** Modelo: tudo que uma análise léxica produz. */
final class ResultadoAnalise {
    private final List<Token> tokens;
    private final List<Mensagem> mensagens;
    private final List<Simbolo> simbolos;
    ResultadoAnalise(List<Token> tokens, List<Mensagem> mensagens, List<Simbolo> simbolos) {
        this.tokens = tokens; this.mensagens = mensagens; this.simbolos = simbolos;
    }
    List<Token> getTokens() { return tokens; }
    List<Mensagem> getMensagens() { return mensagens; }
    List<Simbolo> getSimbolos() { return simbolos; }
    long contarErros() { return mensagens.stream().filter(Mensagem::ehErro).count(); }
    long contarAvisos() { return mensagens.size() - contarErros(); }
}