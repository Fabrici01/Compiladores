import java.util.*;

public final class Vocabulario {
    public static final Set<String> PALAVRAS_RESERVADAS = new LinkedHashSet<>(Arrays.asList(
        ("PROGRAM BEGIN END CONST VAR INTEGER REAL CHAR STRING PROCEDURE FUNCTION IF THEN ELSE WHILE DO "
        + "REPEAT UNTIL BREAK CONTINUE FOR TO DOWNTO OU E").split(" ")));

    public static final String[] OPERADORES_DUPLOS = {":=", "<>", "<=", ">="};
    
    public static final String OPERADORES_SIMPLES = "+-*/=<>()[],;:.";

    public static final int TAMANHO_MAXIMO_IDENTIFICADOR = 15;

    // Pontos de sincronização da recuperação de erros em modo pânico.
    public static final String DELIMITADORES_SINCRONIZACAO = ";,()[]";
}