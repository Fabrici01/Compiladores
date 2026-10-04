import java.util.*;

// Aviso para instrução composta somente por um ID.
public final class RegraInstrucaoSemEfeito implements RegraAviso {
    private static final Set<String> TOKENS_ANTES_DE_INSTRUCAO = Set.of(";", "BEGIN", "THEN", "ELSE", "DO", "REPEAT");

    @Override 
    public List<Mensagem> verificar(List<Token> tokens){
        List<Mensagem> avisos = new ArrayList<>();
        for (int k = 1; k + 1 < tokens.size(); k++){
            Token atual = tokens.get(k);
            if(atual.getTipo().equals("ID") && tokens.get(k + 1).getTipo().equals(";")
                    && TOKENS_ANTES_DE_INSTRUCAO.contains(tokens.get(k - 1).getTipo())){
                
                avisos.add(new Mensagem(atual.getInicio(), atual.getFim(), atual.getLinha(), atual.getColuna(),
                    Severidade.AVISO,
                    "Warning: instrução com somente o identificador '" + atual.getLexema() + "' não tem efeito"));
            }
        }
        return avisos;
    }
}