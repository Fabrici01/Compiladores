import java.util.List;

public final class ResultadoAnalise {
    private final List<Token> tokens;
    private final List<Mensagem> mensagens;
    private final List<Simbolo> simbolos;
    
    public ResultadoAnalise(List<Token> tokens, List<Mensagem> mensagens, List<Simbolo> simbolos){
        this.tokens = tokens;
        this.mensagens = mensagens;
        this.simbolos = simbolos;
    }

    public List<Token> getTokens(){ 
        return tokens; 
    }

    public List<Mensagem> getMensagens(){ 
        return mensagens; 
    }

    public List<Simbolo> getSimbolos(){ 
        return simbolos; 
    }

    public long contarErros(){ 
        return mensagens.stream().filter(Mensagem::ehErro).count();
    }

    public long contarAvisos(){ 
        return mensagens.size() - contarErros(); 
    }
}