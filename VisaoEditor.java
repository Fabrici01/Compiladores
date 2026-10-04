import java.util.List;

// Contrato da View para o editor de código.
public interface VisaoEditor {
    String obterTexto();
    void definirTexto(String texto);
    void selecionarTrecho(int inicio, int fim);
    void marcarProblemas(List<Mensagem> mensagens);
}