import java.util.*;

/** Implementação do analisador léxico. Regras de aviso e tabela de símbolos são injetadas. */
final class AnalisadorLexicoPascalino implements AnalisadorLexico {
    private final List<RegraAviso> regrasAviso;
    private final ConstrutorTabelaSimbolos construtorTabela;

    AnalisadorLexicoPascalino(List<RegraAviso> regrasAviso, ConstrutorTabelaSimbolos construtorTabela) {
        this.regrasAviso = regrasAviso; this.construtorTabela = construtorTabela;
    }

    @Override public ResultadoAnalise analisar(String codigoFonte) {
        Varredura varredura = new Varredura(codigoFonte);
        varredura.executar();
        List<Mensagem> mensagens = new ArrayList<>(varredura.mensagens);
        for (RegraAviso regra : regrasAviso) mensagens.addAll(regra.verificar(varredura.tokens));
        mensagens.sort(Comparator.comparingInt(Mensagem::getInicio));
        return new ResultadoAnalise(varredura.tokens, mensagens, construtorTabela.construir(varredura.tokens));
    }

    /** Estado de uma varredura (um objeto por análise, então o analisador não guarda estado). */
    private static final class Varredura {
        final String texto; final int tamanho;
        int posicao = 0, linha = 1, coluna = 1;
        final List<Token> tokens = new ArrayList<>();
        final List<Mensagem> mensagens = new ArrayList<>();

        Varredura(String texto) { this.texto = texto; this.tamanho = texto.length(); }

        char espiar(int deslocamento) { int p = posicao + deslocamento; return p < tamanho ? texto.charAt(p) : '\0'; }
        char atual() { return texto.charAt(posicao); }
        void avancar() { if (atual() == '\n') { linha++; coluna = 1; } else coluna++; posicao++; }
        static boolean ehLetra(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); }
        static boolean ehDigito(char c) { return c >= '0' && c <= '9'; }
        static boolean ehLetraDigitoOuUnderline(char c) { return ehLetra(c) || ehDigito(c) || c == '_'; }

        void registrarErro(int ini, int fim, int lin, int col, String texto) {
            mensagens.add(new Mensagem(ini, fim, lin, col, Mensagem.Severidade.ERRO, texto));
        }
        void criarToken(String tipo, int ini, int lin, int col, String atributo) {
            tokens.add(new Token(tipo, texto.substring(ini, posicao), lin, col, ini, posicao, atributo));
        }

        /** Laço principal: simula o AFD escolhendo o caminho pelo primeiro caractere. */
        void executar() {
            while (posicao < tamanho) {
                char c = atual(); int ini = posicao, lin = linha, col = coluna;
                if (Character.isWhitespace(c)) { avancar(); continue; }
                if (c == '/' && espiar(1) == '/') { consumirComentarioDeLinha(); continue; }
                if (c == '{' || (c == '(' && espiar(1) == '*')) { consumirComentarioDeBloco(ini, lin, col); continue; }
                if (ehLetra(c)) { lerIdentificadorOuReservada(ini, lin, col); continue; }
                if (ehDigito(c)) { lerNumero(ini, lin, col); continue; }
                if (c == '\'') { lerLiteral(ini, lin, col); continue; }
                if (lerOperador(ini, lin, col)) continue;
                recuperarErroModoPanico(ini, lin, col, c);
            }
        }

        void consumirComentarioDeLinha() { while (posicao < tamanho && atual() != '\n') avancar(); }

        void consumirComentarioDeBloco(int ini, int lin, int col) {
            char abertura = atual();
            String fechamento = abertura == '{' ? "}" : "*)";
            avancar(); if (abertura == '(') avancar();
            while (posicao < tamanho) {
                if (texto.startsWith(fechamento, posicao)) { for (int k = 0; k < fechamento.length(); k++) avancar(); return; }
                avancar();
            }
            registrarErro(ini, ini + (abertura == '{' ? 1 : 2), lin, col, "Comentário não fechado");
        }

        void lerIdentificadorOuReservada(int ini, int lin, int col) {
            while (posicao < tamanho && ehLetraDigitoOuUnderline(atual())) avancar();
            String palavra = texto.substring(ini, posicao), maiuscula = palavra.toUpperCase();
            if (palavra.length() > Vocabulario.TAMANHO_MAXIMO_IDENTIFICADOR) {
                registrarErro(ini, posicao, lin, col, "Identificador '" + palavra + "' excede "
                    + Vocabulario.TAMANHO_MAXIMO_IDENTIFICADOR + " caracteres");
            } else if (Vocabulario.PALAVRAS_RESERVADAS.contains(maiuscula)) criarToken(maiuscula, ini, lin, col, "-");
            else criarToken("ID", ini, lin, col, palavra.toLowerCase());
        }

        void lerNumero(int ini, int lin, int col) {
            while (posicao < tamanho && ehDigito(atual())) avancar();
            if (espiar(0) == '.' && ehDigito(espiar(1))) { avancar(); while (posicao < tamanho && ehDigito(atual())) avancar(); }
            if (ehLetra(espiar(0)) || espiar(0) == '_') {
                while (posicao < tamanho && ehLetraDigitoOuUnderline(atual())) avancar();
                registrarErro(ini, posicao, lin, col, "Número mal formado (identificador não pode começar com dígito)");
                return;
            }
            criarToken("NUM", ini, lin, col, texto.substring(ini, posicao));
        }

        void lerLiteral(int ini, int lin, int col) {
            avancar();
            while (posicao < tamanho && atual() != '\'' && atual() != '\n') avancar();
            if (espiar(0) == '\'') { avancar(); criarToken("LITERAL", ini, lin, col, texto.substring(ini + 1, posicao - 1)); }
            else registrarErro(ini, posicao, lin, col, "Literal não fechado");
        }

        boolean lerOperador(int ini, int lin, int col) {
            for (String op : Vocabulario.OPERADORES_DUPLOS)
                if (texto.startsWith(op, posicao)) { avancar(); avancar(); criarToken(op, ini, lin, col, "-"); return true; }
            if (Vocabulario.OPERADORES_SIMPLES.indexOf(atual()) >= 0) {
                avancar(); criarToken(String.valueOf(texto.charAt(ini)), ini, lin, col, "-"); return true;
            }
            return false;
        }

        /** Recuperação de erros (modo pânico): descarta até espaço ou delimitador de sincronização. */
        void recuperarErroModoPanico(int ini, int lin, int col, char invalido) {
            avancar();
            while (posicao < tamanho && !Character.isWhitespace(atual())
                    && Vocabulario.DELIMITADORES_SINCRONIZACAO.indexOf(atual()) < 0) avancar();
            registrarErro(ini, posicao, lin, col, "Caractere inválido '" + invalido + "'");
        }
    }
}