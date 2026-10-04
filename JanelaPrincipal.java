import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;

// View passiva só exibe e encaminha eventos ao Presenter.
public final class JanelaPrincipal implements VisaoEditor, VisaoResultados {
    private static final int ATRASO_ANALISE_MS = 150;
    private final JFrame janela = new JFrame("Pascalino IDE — analisador léxico");
    private final JTextArea editor = new JTextArea();
    private final DefaultListModel<Mensagem> modeloMensagens = new DefaultListModel<>();
    private final JList<Mensagem> listaMensagens = new JList<>(modeloMensagens);
    private final DefaultTableModel modeloTokens = new DefaultTableModel(new String[]{"Token", "Lexema", "Lin", "Col", "Atributo"}, 0);
    private final DefaultTableModel modeloSimbolos = new DefaultTableModel(new String[]{"Símbolo", "Categoria", "1ª linha"}, 0);
    private final JLabel rotuloStatus = new JLabel(" ");
    private final PintorSublinhadoOndulado pintorErro = new PintorSublinhadoOndulado(Color.RED);
    private final PintorSublinhadoOndulado pintorAviso = new PintorSublinhadoOndulado(new Color(230, 140, 0));
    private PresenterAnalise presenter;

    JanelaPrincipal(){
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14)); 
        editor.setTabSize(2);
        listaMensagens.setCellRenderer(new DefaultListCellRenderer() {
            @Override 
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f){
                Component c = super.getListCellRendererComponent(l, v, i, s, f);
                
                if(!s){
                    c.setForeground(((Mensagem) v).ehErro() ? new Color(200, 30, 30) : new Color(190, 110, 0));
                }

                return c;
            }
        });
        
        listaMensagens.addListSelectionListener(e -> {
            Mensagem m = listaMensagens.getSelectedValue();
            
            if(m != null && !e.getValueIsAdjusting()){ 
                presenter.aoSelecionarMensagem(m);
            }
        });

        // Análise em tempo real
        javax.swing.Timer temporizador = new javax.swing.Timer(ATRASO_ANALISE_MS, e -> presenter.aoTextoAlterado());
        temporizador.setRepeats(false);
        editor.getDocument().addDocumentListener(new DocumentListener(){
            @Override 
            public void insertUpdate(DocumentEvent e){ 
                temporizador.restart();
            }

            @Override 
            public void removeUpdate(DocumentEvent e){ 
                temporizador.restart();
            }

            @Override 
            public void changedUpdate(DocumentEvent e) {}
        });
        
        JButton botaoCompilar = new JButton("▶ Compilar");
        JButton botaoExemplo = new JButton("Exemplo");

        botaoCompilar.addActionListener(e -> presenter.aoCompilar());
        botaoExemplo.addActionListener(e -> presenter.aoCarregarExemplo());

        JToolBar barra = new JToolBar(); 
        barra.setFloatable(false);
        barra.add(botaoCompilar); 
        barra.add(botaoExemplo); 
        barra.add(rotuloStatus);
        
        JTabbedPane abas = new JTabbedPane();
        abas.add("Mensagens", new JScrollPane(listaMensagens));
        abas.add("Tokens", new JScrollPane(new JTable(modeloTokens)));
        abas.add("Tabela de símbolos", new JScrollPane(new JTable(modeloSimbolos)));
        
        JSplitPane divisor = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(editor), abas);
        divisor.setResizeWeight(0.6);
        janela.add(barra, BorderLayout.NORTH); janela.add(divisor);
        janela.setSize(900, 700); janela.setLocationRelativeTo(null);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    void definirPresenter(PresenterAnalise presenter){ 
        this.presenter = presenter; 
    }

    void exibir(){ 
        janela.setVisible(true); 
    }

    @Override 
    public String obterTexto(){ 
        return editor.getText(); 
    }

    @Override 
    public void definirTexto(String texto){ 
        editor.setText(texto); 
    }

    @Override 
    public void selecionarTrecho(int inicio, int fim){ 
        editor.requestFocus(); 
        editor.select(inicio, fim); 
    }

    @Override 
    public void marcarProblemas(List<Mensagem> mensagens){
        editor.getHighlighter().removeAllHighlights();
        for (Mensagem m : mensagens) {
            try { 
                editor.getHighlighter().addHighlight(m.getInicio(), m.getFim(), m.ehErro() ? pintorErro : pintorAviso); 
            }
            catch (BadLocationException ignorada) {}
        }
    }

    @Override 
    public void exibirMensagens(List<Mensagem> mensagens){
        modeloMensagens.clear(); 
        mensagens.forEach(modeloMensagens::addElement);
    }
    
    @Override 
    public void exibirTokens(List<Token> tokens){
        modeloTokens.setRowCount(0);
        for (Token t : tokens){
            modeloTokens.addRow(new Object[]{t.getTipo(), t.getLexema(), t.getLinha(), t.getColuna(), t.getAtributo()});
        }
    }

    @Override 
    public void exibirSimbolos(List<Simbolo> simbolos){
        modeloSimbolos.setRowCount(0);
        for (Simbolo s : simbolos){
            modeloSimbolos.addRow(new Object[]{s.getNome(), s.getCategoria(), s.getPrimeiraLinha()});
        }
    }
    @Override 
    public void exibirStatus(String status){ 
        rotuloStatus.setText(status); 
    }
}