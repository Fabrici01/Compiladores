import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PresenterAnalise {
    private final AnalisadorLexico analisador;
    private final VisaoEditor editor;
    private final VisaoResultados resultados;

    public PresenterAnalise(AnalisadorLexico analisador, VisaoEditor editor, VisaoResultados resultados) {
        this.analisador = analisador; 
        this.editor = editor; 
        this.resultados = resultados;
    }

    public void iniciar(){ 
        editor.definirTexto(ProgramaExemplo.TEXTO); 
        analisar(""); 
    }

    public void aoTextoAlterado(){ 
        analisar(""); 
    }
    
    public void aoCompilar(){ 
        analisar(" — análise léxica concluída");
    }

    public void aoCarregarExemplo(){ 
        editor.definirTexto(ProgramaExemplo.TEXTO); 
    }

    public void aoSelecionarMensagem(Mensagem mensagem){ 
        editor.selecionarTrecho(mensagem.getInicio(), mensagem.getFim());
    }

    private void analisar(String sufixoStatus) {
        ResultadoAnalise lexico = analisador.analisar(editor.obterTexto());
        ResultadoAnalise r = acrescentarSintaxe(lexico);
        editor.marcarProblemas(r.getMensagens());
        resultados.exibirMensagens(r.getMensagens());
        resultados.exibirTokens(r.getTokens());
        resultados.exibirSimbolos(r.getSimbolos());
        resultados.exibirStatus("  " + r.getTokens().size() + " tokens, " + r.contarErros() + " erro(s), "
            + r.contarAvisos() + " aviso(s)" + sufixoStatus);
    }

    // Roda o parser sobre os tokens do léxico e junta as mensagens, ordenadas por posição.
    // Se o parser falhar de forma inesperada, a análise léxica continua aparecendo.
    private ResultadoAnalise acrescentarSintaxe(ResultadoAnalise lexico) {
        List<Mensagem> todas = new ArrayList<>(lexico.getMensagens());
        try {
            todas.addAll(new AnalisadorSintatico(lexico.getTokens(), lexico.getMensagens()).analisar());
        } catch (Throwable falha) {
            return lexico;
        }
        todas.sort(Comparator.comparingInt(Mensagem::getInicio));
        return new ResultadoAnalise(lexico.getTokens(), todas, lexico.getSimbolos());
    }
}