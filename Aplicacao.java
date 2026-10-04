import java.util.List;
import javax.swing.SwingUtilities;

public class Aplicacao {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AnalisadorLexico analisador = new AnalisadorLexicoPascalino(
                List.of(new RegraInstrucaoSemEfeito(), new RegraEnumeracaoDuplicada()),
                        new ConstrutorTabelaSimbolos());
            JanelaPrincipal janela = new JanelaPrincipal();
            PresenterAnalise presenter = new PresenterAnalise(analisador, janela, janela);
            janela.definirPresenter(presenter);
            janela.exibir();
            presenter.iniciar();
        });
    }
}
