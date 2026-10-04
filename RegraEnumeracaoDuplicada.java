import java.util.*;

// Aviso para constante repetida dentro de uma enumeração.
public final class RegraEnumeracaoDuplicada implements RegraAviso {

    @Override
    public List<Mensagem> verificar(List<Token> tokens) {
        List<Mensagem> avisos = new ArrayList<>();
        boolean dentroDeType = false;

        for(int k = 0; k < tokens.size(); k++){
            String tipo = tokens.get(k).getTipo();

            if(tipo.equals("TYPE")){
                dentroDeType = true;
            }else if(tipo.equals("VAR") || tipo.equals("CONST") || tipo.equals("BEGIN")
                    || tipo.equals("PROCEDURE") || tipo.equals("FUNCTION")) {
                dentroDeType = false;
            }

            if(dentroDeType && tipo.equals("=") && k + 1 < tokens.size()
                    && tokens.get(k + 1).getTipo().equals("(")){
                k = verificarEnumeracao(tokens, k + 2, avisos);
            }
        }
        return avisos;
    }

    // Lê as constantes até o ')' e devolve o índice onde parou.
    private int verificarEnumeracao(List<Token> tokens, int inicio, List<Mensagem> avisos){
        Set<String> vistas = new HashSet<>();
        int k = inicio;

        while(k < tokens.size() && !tokens.get(k).getTipo().equals(")")){
            Token t = tokens.get(k);
            if(t.getTipo().equals("ID") && !vistas.add(t.getAtributo())){
                avisos.add(new Mensagem(t.getInicio(), t.getFim(), t.getLinha(), t.getColuna(),
                    Severidade.AVISO,
                    "Warning: constante '" + t.getLexema() + "' repetida na enumeração"));
            }
            k++;
        }
        return k;
    }
}