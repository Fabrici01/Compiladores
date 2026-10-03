import java.util.List;

/** Estratégia de aviso: novas regras são adicionadas sem alterar o analisador (Aberto/Fechado). */
interface RegraAviso {
    List<Mensagem> verificar(List<Token> tokens);
}