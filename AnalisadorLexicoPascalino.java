import java.util.*;

// Implementação do analisador léxico.
public final class AnalisadorLexicoPascalino implements AnalisadorLexico {
    private final List<RegraAviso> regrasAviso;
    private final ConstrutorTabelaSimbolos construtorTabela;

    public AnalisadorLexicoPascalino(List<RegraAviso> regrasAviso, ConstrutorTabelaSimbolos construtorTabela) {
        this.regrasAviso = regrasAviso;
        this.construtorTabela = construtorTabela;
    }

    @Override 
    public ResultadoAnalise analisar(String codigoFonte) {
        Varredura varredura = new Varredura(codigoFonte);
        varredura.executar();
        List<Mensagem> mensagens = new ArrayList<>(varredura.mensagens);
        
        for (RegraAviso regra : regrasAviso){
            mensagens.addAll(regra.verificar(varredura.tokens));
        }

        mensagens.sort(Comparator.comparingInt(Mensagem::getInicio));
        
        return new ResultadoAnalise(varredura.tokens, mensagens, construtorTabela.construir(varredura.tokens));
    }
}