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
    private final TabelaSimbolos tabelaSimbolo = new TabelaSimbolos();

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

    private void registrarErroSemantico(Token t, String texto) {
        erros.add(new Mensagem(t.getInicio(), t.getFim(), t.getLinha(), t.getColuna(), Severidade.ERRO, texto, "Erro Semântico"));
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

    private void sincronizar(){
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
    private Tipo fator() {
        if (verificarTipo("(")) {
            avancaToken();
            Tipo tipoExpr = expr();
            match(")");

            return tipoExpr;
        } else if (verificarTipo("ID")) {
            Token id = devolveTokenAtual();
            avancaToken();

            Tipo base;
            Simbolo simbolo = tabelaSimbolo.buscar(id.getLexema());

            if(simbolo == null){
                registrarErroSemantico(id, "'" + id.getLexema() + "' não declarado");
                base = Tipo.ERRO;
            }else{
                base = simbolo.getTipo();
            }

            return varResto(base);
        } else if (verificarTipo("NUM")) {
            Token num = devolveTokenAtual();
            avancaToken();

            if (num.getLexema().contains(".")) {
                return Tipo.REAL;
            }

            return Tipo.INTEGER;

            } else if (verificarTipo("LITERAL")) {
                Token literal = devolveTokenAtual();
                avancaToken();

                if (literal.getAtributo().length() == 1){
                    return Tipo.CHAR;
                }

                return Tipo.STRING;
        } else {
            reportarErro("esperado uma expressão, encontrado " + descricaoAtual());
            sincronizar();
        }

        return Tipo.ERRO;
    }

    // varResto -> '[' exprOp ']' | '.' ID | vazio
    private Tipo varResto(Tipo base) {
        if (verificarTipo("[")) {
            Token abre = devolveTokenAtual();
            avancaToken();
            exprOp();
            match("]");

            if (base == Tipo.ERRO){
                return Tipo.ERRO;
            }

            if (base == Tipo.STRING){
                return Tipo.CHAR;
            }

            registrarErroSemantico(abre, "valor do tipo " + base + " não pode ser indexado");
            return Tipo.ERRO;

        }else if (verificarTipo(".")) {
            avancaToken();
            match("ID");
            return Tipo.ERRO; //Provisório
        }

        return base;
    }

    // unario -> ('+' | '-')? fator
    private Tipo unario() {
        if (verificarTipo("+") || verificarTipo("-")) {
            avancaToken();
        }
        return fator();
    }

    // termo -> unario (('*' | '/') unario)*
    private Tipo termo() {
        Tipo tipoTermo = unario();
        while (verificarTipo("*") || verificarTipo("/")) {
            avancaToken();
            unario();
        }

        return tipoTermo;
    }

    // exprOp -> termo (('+' | '-') termo)*
    private Tipo exprOp() {
        Tipo tipoExprOp = termo();
        while (verificarTipo("+") || verificarTipo("-")) {
            avancaToken();
            termo();
        }

        return tipoExprOp;
    }

    // exprComp -> exprOp (operadorRelacional exprOp)*
    private Tipo exprComp() {
        Tipo tipoComp = exprOp();
        while (pertence(OPERADORES_RELACIONAIS)) {
            avancaToken();
            exprOp();
        }

        return tipoComp;
    }

    // expr -> exprComp (('OU' | 'E') exprComp)*
    private Tipo expr() {
        if (!entrar()) {
            profundidade--;
            return Tipo.ERRO;
        }
        Tipo tipoExpr = exprComp();
        while (verificarTipo("OU") || verificarTipo("E")) {
            avancaToken();
            exprComp();
        }
        profundidade--;
        return tipoExpr;
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

    private Tipo tipo(){
        if(verificarTipo("INTEGER")){
            panico = false;
            avancaToken();

            return Tipo.INTEGER;
        }else if(verificarTipo("REAL")){
            panico = false;
            avancaToken();

            return Tipo.REAL;
        }else if(verificarTipo("CHAR")){
            panico = false;
            avancaToken();

            return Tipo.CHAR;
        }else if(verificarTipo("STRING") ){
            panico = false;
            avancaToken();

            return Tipo.STRING;
        }else if(verificarTipo("ID")){
            panico = false;
            avancaToken();

            return Tipo.ERRO;  //Provisório
        }

        reportarErro("esperado um tipo (INTEGER, REAL, CHAR, STRING ou ID), encontrado " + descricaoAtual());
        sincronizar();
        return Tipo.ERRO;
    }

    private List<Token> conjuntoIds(){
        List<Token> nomes = new ArrayList<>();

        while(verificarTipo(",")){
            match(",");
            Token tokenAtual = devolveTokenAtual();
            if(match("ID")){
                nomes.add(tokenAtual);
            }
        }

        return nomes;
    }

    private void declVar(Categoria categoria){
        List<Token> nomes = new ArrayList<>();
        Token tokenAtual = devolveTokenAtual();

        if(match("ID")){
            nomes.add(tokenAtual);
        }

        nomes.addAll(conjuntoIds());
        match(":");
        Tipo tipoAtual = tipo();

        for(Token t : nomes){
            if(categoria != null){
                Simbolo simbolo = new Simbolo(t.getLexema(), categoria, tipoAtual, t.getLinha());

                if(!tabelaSimbolo.inserir(simbolo)){
                    registrarErroSemantico(t,  t.getLexema() + " já declarado");
                }
            }
        }

        match(";");
    }

    private void declVarList(Categoria categoria){
        while(verificarTipo("ID")){
            declVar(categoria);
        }
    }

    private void declaracaoVariavel(){
        if(verificarTipo("VAR")){
            match("VAR");
            declVarList(Categoria.VARIAVEL);
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
            declVarList(null);
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
        declVarList(Categoria.PARAMETRO);
    }

    private void declProc(){
        if(verificarTipo("PROCEDURE")){
            match("PROCEDURE");
            Token tokenNome = devolveTokenAtual();
            tabelaSimbolo.abrirEscopo();

            if(match("ID")){
                if(!tabelaSimbolo.inserirNoGlobal(new Simbolo(tokenNome.getLexema(), Categoria.PROCEDIMENTO, Tipo.VAZIO, tokenNome.getLinha()))){
                    registrarErroSemantico(tokenNome, tokenNome.getLexema() + " já declarado");
                }
            }

            match("(");
            parametros();
            match(")");
            match(";");
            declaracaoVariavel();
            bloco();

            tabelaSimbolo.fecharEscopo();
        }else if(verificarTipo("FUNCTION")){
            match("FUNCTION");
            Token tokenNome = devolveTokenAtual();
            tabelaSimbolo.abrirEscopo();
            boolean bool = match("ID");
            match("(");
            parametros();
            match(")");
            match(":");
            Tipo tipoFuncao = tipo();
            match(";");
            if(bool){
                if(!tabelaSimbolo.inserirNoGlobal(new Simbolo(tokenNome.getLexema(), Categoria.FUNCAO, tipoFuncao, tokenNome.getLinha()))){
                    registrarErroSemantico(tokenNome, tokenNome.getLexema() + " já declarado");
                }
            }
            declaracaoVariavel();
            bloco();
            tabelaSimbolo.fecharEscopo();
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
