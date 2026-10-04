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
        ResultadoAnalise r = analisador.analisar(editor.obterTexto());
        editor.marcarProblemas(r.getMensagens());
        resultados.exibirMensagens(r.getMensagens());
        resultados.exibirTokens(r.getTokens());
        resultados.exibirSimbolos(r.getSimbolos());
        resultados.exibirStatus("  " + r.getTokens().size() + " tokens, " + r.contarErros() + " erro(s), "
            + r.contarAvisos() + " aviso(s)" + sufixoStatus);
    }
}