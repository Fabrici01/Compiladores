import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnalisadorSintatico {
    private final List<Token> tokens;
    private final List<Mensagem> mensagensLexicas;
    private final List<Mensagem> erros = new ArrayList<>();
    private final List<Mensagem> avisos = new ArrayList<>();
    private int indice = 0;
    private boolean panico = false;
    private int profundidade = 0;
    private static Set<String> INICIO_EXPRESSAO = Set.of("(", "+", "-", "ID", "LITERAL", "NUM");
    private static Set<String> INICIO_INSTRUCAO = Set.of("ID", "IF", "WHILE", "REPEAT", "FOR", "BREAK", "CONTINUE", "BEGIN");
    private static Set<String> OPERADORES_RELACIONAIS = Set.of("=", "<>", "<", "<=", ">", ">=");
    private static Set<String> SINCRONIZA = Set.of(";", ")", "]", ",", "THEN", "DO", "TO", "DOWNTO",
        "BEGIN", "END", "VAR", "CONST", "TYPE", "PROCEDURE", "FUNCTION", "IF", "WHILE", "FOR", "REPEAT", "UNTIL");

    public AnalisadorSintatico(List<Token> tokens, List<Mensagem> mensagensLexicas) {
        this.tokens = tokens;
        this.mensagensLexicas = mensagensLexicas;
    }

    private Token devolveTokenAtual(){
        if(indice >= tokens.size()){
            return tokens.get(tokens.size() - 1);
        }

        return tokens.get(indice);
    }

    private void avancaToken(){
        if (!verificarTipo("EOF")){
            indice++;
        }
    }

    private boolean pertence(Set<String> conjunto){
        return conjunto.contains(devolveTokenAtual().getTipo());
    }

    private void registrarErroSintatico(Token t, String texto) {
        erros.add(new Mensagem(t.getInicio(), t.getFim(), t.getLinha(), t.getColuna(), Severidade.ERRO, texto, "Erro Sintático"));
    }

    private void registrarAviso(Token t, String texto) {
        avisos.add(new Mensagem(t.getInicio(), t.getFim(), t.getLinha(), t.getColuna(), Severidade.AVISO, texto, "Aviso"));
    }

    private boolean lacunaLexica() {
        int fimAnterior = 0;
        if (indice > 0) {
            fimAnterior = tokens.get(indice - 1).getFim();
        }
        int inicioAtual = devolveTokenAtual().getInicio();
        for (int i = 0; i < mensagensLexicas.size(); i++) {
            Mensagem m = mensagensLexicas.get(i);
            if (m.ehErro() && m.getInicio() >= fimAnterior && m.getInicio() < inicioAtual) {
                return true;
            }
        }
        return false;
    }

    private void reportarErro(String texto){
    if(panico){
        return;
    }
    panico = true;
    if(lacunaLexica()){
        return;
    }
    Token t = devolveTokenAtual();
    if(verificarTipo("EOF") && indice > 0){
        t = tokens.get(indice - 1);
    }
    registrarErroSintatico(t, texto);
}

    private void  sincronizar(){
        if(panico){
            while(!pertence(SINCRONIZA) && !verificarTipo("EOF")){
                avancaToken();
            }
        }
    }

    private boolean verificarTipo(String tipo) {
        return devolveTokenAtual().getTipo().equals(tipo);
    }

    private boolean match(String tipo){
        if(verificarTipo(tipo)){
            panico = false;
            avancaToken();
            return true;
        } else {
            reportarErro("Esperado: " + tipo + ", encontrado: " + devolveTokenAtual().getTipo());
            return false;
        }
    }

    private boolean ehFim() {
        return verificarTipo("EOF");
    }

    private String descricaoAtual() {
        if (ehFim()) {
            return "fim do arquivo";
        }
        return "'" + devolveTokenAtual().getLexema() + "'";
    }

    private boolean entrar() {
        profundidade++;
        if (profundidade > 200) {
            reportarErro("construção aninhada demais para ser analisada");
            sincronizar();
            return false;
        }
        return true;
    }

    // fator -> '(' expr ')' | ID varResto | NUM | LITERAL
    private void fator() {
        if (verificarTipo("(")) {
            avancaToken();
            expr();
            match(")");
        } else if (verificarTipo("ID")) {
            avancaToken();
            varResto();
        } else if (verificarTipo("NUM") || verificarTipo("LITERAL")) {
            avancaToken();
        } else {
            reportarErro("esperado uma expressão, encontrado " + descricaoAtual());
            sincronizar();
        }
    }

    // varResto -> '[' exprOp ']' | '.' ID | vazio
    private void varResto() {
        if (verificarTipo("[")) {
            avancaToken();
            exprOp();
            match("]");
        } else if (verificarTipo(".")) {
            avancaToken();
            match("ID");
        }
        // vazio: não consome nada
    }

    // unario -> ('+' | '-')? fator
    private void unario() {
        if (verificarTipo("+") || verificarTipo("-")) {
            avancaToken();
        }
        fator();
    }

    // termo -> unario (('*' | '/') unario)*
    private void termo() {
        unario();
        while (verificarTipo("*") || verificarTipo("/")) {
            avancaToken();
            unario();
        }
    }

    // exprOp -> termo (('+' | '-') termo)*
    private void exprOp() {
        termo();
        while (verificarTipo("+") || verificarTipo("-")) {
            avancaToken();
            termo();
        }
    }

    // exprComp -> exprOp (operadorRelacional exprOp)*
    private void exprComp() {
        exprOp();
        while (pertence(OPERADORES_RELACIONAIS)) {
            avancaToken();
            exprOp();
        }
    }

    // expr -> exprComp (('OU' | 'E') exprComp)*
    private void expr() {
        if (!entrar()) {
            profundidade--;
            return;
        }
        exprComp();
        while (verificarTipo("OU") || verificarTipo("E")) {
            avancaToken();
            exprComp();
        }
        profundidade--;
    }

    private void programa(){} //FAZER DPS

    public List<Mensagem> analisar() {
    if (tokens.isEmpty()) {
        return new ArrayList<>();
    }
    programa();
    List<Mensagem> todas = new ArrayList<>(erros);
    todas.addAll(avisos);
    return todas;
}
}
