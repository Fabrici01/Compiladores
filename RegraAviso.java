import java.util.List;

// Aplicando o Strategy nos Avisos
public interface RegraAviso {
    List<Mensagem> verificar(List<Token> tokens);
}