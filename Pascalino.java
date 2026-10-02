import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;

/** Pascalino IDE + analisador léxico. Java 17+.  Compilar: javac Pascalino.java  |  Executar: java Pascalino */
public class Pascalino {

    // ===================== ANALISADOR LÉXICO =====================
    record Tok(String t, String lex, int l, int c, String attr) {}
    record Msg(int s, int e, int l, int c, boolean err, String m) {
        public String toString() { return "[" + l + ":" + c + "] " + m; }
    }

    // Palavras reservadas (Anexo I + FOR/TO/DOWNTO). Case-insensitive: comparação em maiúsculas.
    static final Set<String> KW = new LinkedHashSet<>(Arrays.asList(
        ("PROGRAM BEGIN END CONST VAR INTEGER REAL CHAR STRING PROCEDURE FUNCTION IF THEN ELSE WHILE DO "
        + "REPEAT UNTIL BREAK CONTINUE FOR TO DOWNTO OU E").split(" ")));
    static final String[] OPS2 = {":=", "<>", "<=", ">="};
    static final String OPS1 = "+-*/=<>()[],;:.";
    static final int MAXID = 15;
    static final Set<String> BEFORE_STMT = Set.of(";", "BEGIN", "THEN", "ELSE", "DO", "REPEAT");

    static class Lexer {
        final String src; final int n;
        int i = 0, line = 1, col = 1;
        final List<Tok> toks = new ArrayList<>();
        final List<Msg> msgs = new ArrayList<>();
        final List<int[]> pos = new ArrayList<>();               // [início, fim] de cada token
        final Map<String, String[]> sym = new LinkedHashMap<>(); // tabela de símbolos

        Lexer(String s) { src = s; n = s.length(); }

        char at(int k) { return k < n ? src.charAt(k) : '\0'; }
        void adv() { if (src.charAt(i) == '\n') { line++; col = 1; } else col++; i++; }
        static boolean letter(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); }
        static boolean digit(char c) { return c >= '0' && c <= '9'; }
        static boolean alnum_(char c) { return letter(c) || digit(c) || c == '_'; }
        void error(int s, int e, int l, int c, String m) { msgs.add(new Msg(s, Math.max(e, s + 1), l, c, true, m)); }
        void tok(String t, int s, int l, int c, String attr) {
            toks.add(new Tok(t, src.substring(s, i), l, c, attr)); pos.add(new int[]{s, i});
        }

        // Função principal de varredura (scanner): simula o AFD caractere a caractere
        void run() {
            while (i < n) {
                char ch = src.charAt(i); int s = i, l = line, c = col;
                if (Character.isWhitespace(ch)) { adv(); continue; }                 // remove espaços
                if (ch == '/' && at(i + 1) == '/') { while (i < n && src.charAt(i) != '\n') adv(); continue; }
                if (ch == '{' || (ch == '(' && at(i + 1) == '*')) {                  // { } e (* *)
                    String close = ch == '{' ? "}" : "*)"; adv(); if (ch == '(') adv();
                    boolean ok = false;
                    while (i < n) { if (src.startsWith(close, i)) { for (int k = 0; k < close.length(); k++) adv(); ok = true; break; } adv(); }
                    if (!ok) error(s, s + (ch == '{' ? 1 : 2), l, c, "Comentário não fechado");
                    continue;
                }
                if (letter(ch)) {                                                    // ID ou palavra reservada
                    while (i < n && alnum_(src.charAt(i))) adv();
                    String w = src.substring(s, i), up = w.toUpperCase();
                    if (w.length() > MAXID) { error(s, i, l, c, "Identificador '" + w + "' excede " + MAXID + " caracteres"); continue; }
                    if (KW.contains(up)) tok(up, s, l, c, "-"); else tok("ID", s, l, c, w.toLowerCase());
                    continue;
                }
                if (digit(ch)) {                                                     // NUM: digitos | digitos.digitos
                    while (i < n && digit(src.charAt(i))) adv();
                    if (at(i) == '.' && digit(at(i + 1))) { adv(); while (i < n && digit(src.charAt(i))) adv(); }
                    if (letter(at(i)) || at(i) == '_') {
                        while (i < n && alnum_(src.charAt(i))) adv();
                        error(s, i, l, c, "Número mal formado (identificador não pode começar com dígito)"); continue;
                    }
                    tok("NUM", s, l, c, src.substring(s, i)); continue;
                }
                if (ch == '\'') {                                                    // LITERAL
                    adv(); while (i < n && src.charAt(i) != '\'' && src.charAt(i) != '\n') adv();
                    if (at(i) == '\'') { adv(); tok("LITERAL", s, l, c, src.substring(s + 1, i - 1)); }
                    else error(s, i, l, c, "Literal não fechado");
                    continue;
                }
                for (String o : OPS2) if (src.startsWith(o, i)) { adv(); adv(); tok(o, s, l, c, "-"); s = -1; break; }
                if (s == -1) continue;
                if (OPS1.indexOf(ch) >= 0) { adv(); tok(String.valueOf(ch), s, l, c, "-"); continue; }
                // RECUPERAÇÃO DE ERROS (modo pânico): descarta símbolos até um delimitador de sincronização
                adv();
                while (i < n && !Character.isWhitespace(src.charAt(i)) && ";,()[]".indexOf(src.charAt(i)) < 0) adv();
                error(s, i, l, c, "Caractere inválido '" + ch + "'");
            }
            // Warning: instrução composta apenas por um ID (sem efeito)
            for (int k = 1; k + 1 < toks.size(); k++)
                if (toks.get(k).t().equals("ID") && toks.get(k + 1).t().equals(";") && BEFORE_STMT.contains(toks.get(k - 1).t())) {
                    Tok t = toks.get(k);
                    msgs.add(new Msg(pos.get(k)[0], pos.get(k)[1], t.l(), t.c(), false,
                        "Warning: instrução com somente o identificador '" + t.lex() + "' não tem efeito"));
                }
            msgs.sort(Comparator.comparingInt(Msg::s));
            // Tabela de símbolos: tokens conhecidos + identificadores do programa
            for (String k : KW) sym.put(k, new String[]{k, "palavra reservada", "-"});
            for (String o : OPS2) sym.put(o, new String[]{o, "operador", "-"});
            for (char o : OPS1.toCharArray()) sym.put("" + o, new String[]{"" + o, "operador/pontuação", "-"});
            for (Tok t : toks) if (t.t().equals("ID")) sym.putIfAbsent(t.attr(), new String[]{t.attr(), "identificador", "" + t.l()});
        }
    }

    // ===================== IDE (Swing) =====================
    /** Sublinhado em zigue-zague: vermelho = erro, laranja = warning. */
    static class Squiggle implements Highlighter.HighlightPainter {
        final Color color; Squiggle(Color c) { color = c; }
        public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent tc) {
            try {
                var a = tc.modelToView2D(p0); var b = tc.modelToView2D(p1);
                int x0 = (int) a.getX(), x1 = (int) b.getX(), y = (int) a.getMaxY() - 2;
                g.setColor(color);
                for (int x = x0, up = 0; x < x1; x += 2, up ^= 1) g.drawLine(x, y + up, x + 2, y + (up ^ 1));
            } catch (BadLocationException ignored) {}
        }
    }

    static final String EXEMPLO = """
        program Demo;
        const max = 10;
        var soma, i : integer;
        { comentário de bloco }
        begin
          soma := 0;
          for i := 1 to max do
            soma := soma + i;   // soma acumulada
          soma;
          Valor_Muito_Longo_Demais := 3;
          x := 5 $ 2;
        end.
        """;

    public static void main(String[] args) { SwingUtilities.invokeLater(Pascalino::gui); }

    static void gui() {
        JFrame f = new JFrame("Pascalino IDE — analisador léxico");
        JTextArea ed = new JTextArea(EXEMPLO);
        ed.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14)); ed.setTabSize(2);
        DefaultListModel<Msg> msgModel = new DefaultListModel<>();
        JList<Msg> msgList = new JList<>(msgModel);
        msgList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean h) {
                Component c = super.getListCellRendererComponent(l, v, i, s, h);
                if (!s) c.setForeground(((Msg) v).err() ? new Color(200, 30, 30) : new Color(190, 110, 0));
                return c;
            }
        });
        // Clicar na mensagem seleciona o trecho no editor
        msgList.addListSelectionListener(e -> {
            Msg m = msgList.getSelectedValue();
            if (m != null && !e.getValueIsAdjusting()) { ed.requestFocus(); ed.select(m.s(), m.e()); }
        });
        DefaultTableModel tokM = new DefaultTableModel(new String[]{"Token", "Lexema", "Lin", "Col", "Atributo"}, 0);
        DefaultTableModel symM = new DefaultTableModel(new String[]{"Símbolo", "Categoria", "1ª linha"}, 0);
        JLabel status = new JLabel(" ");

        Runnable analyze = () -> {
            Lexer lx = new Lexer(ed.getText()); lx.run();
            Highlighter h = ed.getHighlighter(); h.removeAllHighlights();
            Squiggle red = new Squiggle(Color.RED), orange = new Squiggle(new Color(230, 140, 0));
            msgModel.clear();
            int ne = 0, nw = 0;
            for (Msg m : lx.msgs) {
                try { h.addHighlight(m.s(), m.e(), m.err() ? red : orange); } catch (BadLocationException ignored) {}
                msgModel.addElement(m); if (m.err()) ne++; else nw++;
            }
            tokM.setRowCount(0);
            for (Tok t : lx.toks) tokM.addRow(new Object[]{t.t(), t.lex(), t.l(), t.c(), t.attr()});
            symM.setRowCount(0);
            for (String[] r : lx.sym.values()) symM.addRow(r);
            status.setText("  " + lx.toks.size() + " tokens, " + ne + " erro(s), " + nw + " aviso(s)");
        };

        // Análise em tempo real: reanalisa 150 ms após a última digitação
        javax.swing.Timer debounce = new javax.swing.Timer(150, e -> analyze.run());
        debounce.setRepeats(false);
        ed.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { debounce.restart(); }
            public void removeUpdate(DocumentEvent e) { debounce.restart(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        JButton compile = new JButton("▶ Compilar"), example = new JButton("Exemplo");
        compile.addActionListener(e -> { analyze.run(); status.setText(status.getText() + " — análise léxica concluída"); });
        example.addActionListener(e -> ed.setText(EXEMPLO));
        JToolBar bar = new JToolBar(); bar.setFloatable(false); bar.add(compile); bar.add(example); bar.add(status);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Mensagens", new JScrollPane(msgList));
        tabs.add("Tokens", new JScrollPane(new JTable(tokM)));
        tabs.add("Tabela de símbolos", new JScrollPane(new JTable(symM)));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(ed), tabs);
        split.setResizeWeight(0.6);
        f.add(bar, BorderLayout.NORTH); f.add(split);
        f.setSize(900, 700); f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        analyze.run();
    }
}
