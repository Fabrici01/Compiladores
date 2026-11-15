import java.util.*;

public final class ConstrutorTabelaSimbolos {
    
    public List<Simbolo> construir(List<Token> tokens){
        Map<String, Simbolo> tabela = new LinkedHashMap<>();

        for (String k : Vocabulario.PALAVRAS_RESERVADAS){
            tabela.put(k, new Simbolo(k, Categoria.PALAVRA_RESERVADA));
        }

        for (String o : Vocabulario.OPERADORES_DUPLOS){
            tabela.put(o, new Simbolo(o, Categoria.OPERADOR));
        }

        for (char o : Vocabulario.OPERADORES_SIMPLES.toCharArray()){
            tabela.put("" + o, new Simbolo("" + o, Categoria.PONTUACAO));
        }
        
        for (Token t : tokens){
            if (t.getTipo().equals("ID")){
                tabela.putIfAbsent(t.getAtributo(), new Simbolo(t.getAtributo(), Categoria.IDENTIFICADOR, Tipo.VAZIO, t.getLinha()));
            }
        }
        
        return new ArrayList<>(tabela.values());
    }
}
