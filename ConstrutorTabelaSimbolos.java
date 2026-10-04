import java.util.*;

// Responsável por montar a tabela de símbolos.
public final class ConstrutorTabelaSimbolos {
    
    public List<Simbolo> construir(List<Token> tokens){
        Map<String, Simbolo> tabela = new LinkedHashMap<>();

        for (String k : Vocabulario.PALAVRAS_RESERVADAS){
            tabela.put(k, new Simbolo(k, "palavra reservada", "-"));
        }

        for (String o : Vocabulario.OPERADORES_DUPLOS){
            tabela.put(o, new Simbolo(o, "operador", "-"));
        }

        for (char o : Vocabulario.OPERADORES_SIMPLES.toCharArray()){
            tabela.put("" + o, new Simbolo("" + o, "operador/pontuação", "-"));
        }
        
        for (Token t : tokens){
            if (t.getTipo().equals("ID")){
                tabela.putIfAbsent(t.getAtributo(), new Simbolo(t.getAtributo(), "identificador", "" + t.getLinha()));
            }
        }
        
        return new ArrayList<>(tabela.values());
    }
}