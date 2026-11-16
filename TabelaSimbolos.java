import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.LinkedHashMap;

public class TabelaSimbolos{
    private Deque<Map<String, Simbolo>> pilhaEscopo = new ArrayDeque<>();

    public TabelaSimbolos(){
        Map<String, Simbolo> escopoGlobal = new LinkedHashMap<>();
        this.pilhaEscopo.push(escopoGlobal);
    }

    public void abrirEscopo(){
        Map<String, Simbolo> escopo = new LinkedHashMap<>();
        this.pilhaEscopo.push(escopo);
    }

    public void fecharEscopo(){
        if(this.pilhaEscopo.size() > 1){
            this.pilhaEscopo.pop();
        }else{
            System.out.println("Impossível fechar o escopo Global!!!");
        }
    }

    public boolean existeNoEscopoAtual(String nome){
        return this.pilhaEscopo.getFirst().containsKey(normaliza(nome));
    }

    public boolean inserir(Simbolo simbolo){
        if(!existeNoEscopoAtual(normaliza(simbolo.getNome()))){
            this.pilhaEscopo.getFirst().put(normaliza(simbolo.getNome()), simbolo);
            return true;
        }

        return false;
    }

    public boolean inserirNoGlobal(Simbolo simbolo){
        Map<String, Simbolo> escopoGlobal = this.pilhaEscopo.getLast();

        if(!escopoGlobal.containsKey(normaliza(simbolo.getNome()))){
            escopoGlobal.put(normaliza(simbolo.getNome()), simbolo);
            return true;
        }

        return false;
    }

    public Simbolo buscar(String nome){
        for(Map<String, Simbolo> m : this.pilhaEscopo){
            if(m.containsKey(normaliza(nome))){
                return m.get(normaliza(nome));
            }
        }

        return null;
    }

    private String normaliza(String nome){
        return nome.toUpperCase();
    }
}
