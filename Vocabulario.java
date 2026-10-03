import java.util.*;

/** Modelo: regras fixas da linguagem Pascalino (Anexo I + FOR/TO/DOWNTO). */
final class Vocabulario {
    private Vocabulario() {}
    static final Set<String> PALAVRAS_RESERVADAS = new LinkedHashSet<>(Arrays.asList(
        ("PROGRAM BEGIN END CONST VAR INTEGER REAL CHAR STRING PROCEDURE FUNCTION IF THEN ELSE WHILE DO "
        + "REPEAT UNTIL BREAK CONTINUE FOR TO DOWNTO OU E").split(" ")));
    static final String[] OPERADORES_DUPLOS = {":=", "<>", "<=", ">="};
    static final String OPERADORES_SIMPLES = "+-*/=<>()[],;:.";
    static final int TAMANHO_MAXIMO_IDENTIFICADOR = 15;
    /** Pontos de sincronização da recuperação de erros em modo pânico. */
    static final String DELIMITADORES_SINCRONIZACAO = ";,()[]";
}