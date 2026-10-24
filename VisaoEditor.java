import java.util.List;

public interface VisaoEditor {
    String obterTexto();
    void definirTexto(String texto);
    void selecionarTrecho(int inicio, int fim);
    void marcarProblemas(List<Mensagem> mensagens);
}