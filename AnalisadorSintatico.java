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

    // bloco -> 'BEGIN' instrucoes 'END' ';'
    private void bloco() {
        match("BEGIN");
        instrucoes();
        match("END");
        match(";");
    }

    // instrucoes -> (inst)*   (para em END, UNTIL ou fim do arquivo)
    private void instrucoes() {
        boolean descartou = false;
        while (!ehFim() && !verificarTipo("END") && !verificarTipo("UNTIL")) {
            if (pertence(INICIO_INSTRUCAO)) {
                if (descartou) {
                    panico = false;      // depois de descartar lixo, o próximo erro real deve aparecer
                    descartou = false;
                }
                int antes = indice;
                inst();
                if (indice == antes) {
                    avancaToken();       // trava de segurança: toda volta precisa consumir token
                }
            } else {
                reportarErro("token inesperado " + descricaoAtual());
                avancaToken();
                descartou = true;
            }
        }
    }

    // inst: controla a profundidade e delega ao corpo
    private void inst() {
        if (!entrar()) {
            profundidade--;
            return;
        }
        instCorpo();
        profundidade--;
    }

    private void instCorpo() {
        if (verificarTipo("ID")) {
            Token id = devolveTokenAtual();
            avancaToken();
            instResto(id);
        } else if (verificarTipo("IF")) {
            avancaToken();
            expr();
            match("THEN");
            inst();
            if (verificarTipo("ELSE")) {   // ELSE pertence ao IF mais próximo
                avancaToken();
                inst();
            }
        } else if (verificarTipo("WHILE")) {
            avancaToken();
            expr();
            match("DO");
            inst();
        } else if (verificarTipo("REPEAT")) {
            avancaToken();
            instrucoes();
            match("UNTIL");
            expr();
            match(";");
        } else if (verificarTipo("FOR")) {
            avancaToken();
            match("ID");
            match(":=");
            expr();
            if (verificarTipo("TO") || verificarTipo("DOWNTO")) {
                avancaToken();
            } else {
                reportarErro("esperado 'TO' ou 'DOWNTO', encontrado " + descricaoAtual());
            }
            expr();
            match("DO");
            inst();
        } else if (verificarTipo("BREAK") || verificarTipo("CONTINUE")) {
            avancaToken();
            match(";");
        } else if (verificarTipo("BEGIN")) {
            bloco();
        } else {
            reportarErro("esperado uma instrução, encontrado " + descricaoAtual());
        }
    }

    // instResto: o que vem depois de "ID" no começo de uma instrução
    private void instResto(Token id) {
        if (verificarTipo(":=")) {               // x := expr ;
            avancaToken();
            expr();
            match(";");
        } else if (verificarTipo("[")) {         // v[i] := expr ;
            avancaToken();
            expr();
            match("]");
            match(":=");
            expr();
            match(";");
        } else if (verificarTipo(".")) {         // r.campo := expr ;
            avancaToken();
            match("ID");
            match(":=");
            expr();
            match(";");
        } else if (verificarTipo("(")) {         // f(a, b) ;
            avancaToken();
            parametros2();
            match(")");
            match(";");
        } else if (verificarTipo(";")) {         // só o identificador: não tem efeito
            avancaToken();
            registrarAviso(id, "Instrução com somente o identificador '" + id.getLexema() + "' não tem efeito");
        } else {
            reportarErro("esperado ':=', '[', '.', '(' ou ';', encontrado " + descricaoAtual());
            sincronizar();
        }
    }

    // parametros2 -> (expr (',' expr)*)?   argumentos de uma chamada
    private void parametros2() {
        if (pertence(INICIO_EXPRESSAO)) {
            expr();
            while (verificarTipo(",")) {
                avancaToken();
                expr();
            }
        }
    }

    private void tipo(){
        if(verificarTipo("INTEGER") || verificarTipo("REAL") || verificarTipo("CHAR") || verificarTipo("STRING") || verificarTipo("ID")){
            panico = false;
            avancaToken();
        }else{
            reportarErro("esperado um tipo (INTEGER, REAL, CHAR, STRING ou ID), encontrado " + descricaoAtual());
            sincronizar();
        }
    }

    private void conjuntoIds(){
        while(verificarTipo(",")){
            match(",");
            match("ID");
        }
    }

    private void declVar(){
        match("ID");
        conjuntoIds();
        match(":");
        tipo();
        match(";");
    }

    private void declVarList(){
        while(verificarTipo("ID")){
            declVar();
        }
    }

    private void declaracaoVariavel(){
        if(verificarTipo("VAR")){
            match("VAR");
            declVarList();
        }
    }

    private void corpoTipo(){
        if(verificarTipo("(")){
            match("(");
            match("ID");
            conjuntoIds();
            match(")");
        }else if(verificarTipo("RECORD")){
            match("RECORD");
            declVarList();
            match("END");
        }else{
            reportarErro("esperado '(' ou RECORD, encontrado " + descricaoAtual());
            sincronizar();
        }
    }

    private void declTipoList(){
        while(verificarTipo("ID")){
            match("ID");
            match("=");
            corpoTipo();
            match(";");
        }
    }

    private void declaracaoTipos(){
        if(verificarTipo("TYPE")){
            match("TYPE");
            declTipoList();
        }
    }

    private void declConsResto(){
        if(verificarTipo(":")){
            match(":");
            tipo();
            match("=");
            valor();
            match(";");
        }else if(verificarTipo("=")){
            match("=");
            valor();
            match(";");
        }else{
            reportarErro("Esperava : ou = e encontrou " + descricaoAtual());
            sincronizar();
        }
    }

    private void declConsList(){
        while(verificarTipo("ID")){
            match("ID");
            declConsResto();
        }
    }

    private void declaracaoConsts(){
        if(verificarTipo("CONST")){
            match("CONST");
            declConsList();
        }
    }

    private void valor(){
        unario();
    }

    private void parametros(){
        declVarList();
    }

    private void declProc(){
        if(verificarTipo("PROCEDURE")){
            match("PROCEDURE");
            match("ID");
            match("(");
            parametros();
            match(")");
            match(";");
            declaracaoVariavel();
            bloco();
        }else if(verificarTipo("FUNCTION")){
            match("FUNCTION");
            match("ID");
            match("(");
            parametros();
            match(")");
            match(":");
            tipo();
            match(";");
            declaracaoVariavel();
            bloco();
        }else{
            reportarErro("Esperado PROCEDURE ou FUNCTION, encontrado " + descricaoAtual());
            sincronizar();
        }
    }

    private void declProcs(){
        while(verificarTipo("PROCEDURE") || verificarTipo("FUNCTION")){
            declProc();
        }
    }

    private void declaracoes(){
        declaracaoTipos();
        declaracaoVariavel();
        declaracaoConsts();
        declProcs();
    }

    private void programa(){
        match("PROGRAM");
        match("ID");
        match(";");
        declaracoes();
        match("BEGIN");
        instrucoes();
        match("END");
        match(".");
        // Exigência extra: nada pode sobrar depois do '.'
        if (!ehFim()) {
            reportarErro("texto inesperado depois do fim do programa");
        }
    }

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
