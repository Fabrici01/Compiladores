import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public final class NumeracaoLinhas extends JComponent implements DocumentListener, CaretListener {
    private static final int MARGEM = 6;
    private final JTextArea editor;

    public NumeracaoLinhas(JTextArea editor) {
        this.editor = editor;
        setFont(editor.getFont());
        setForeground(Color.GRAY);
        setOpaque(true);
        setBackground(new Color(240, 240, 240));
        editor.getDocument().addDocumentListener(this);
        editor.addCaretListener(this);
    }

    private int totalLinhas() {
        return editor.getDocument().getDefaultRootElement().getElementCount();
    }

    @Override 
    public Dimension getPreferredSize(){
        FontMetrics fm = getFontMetrics(getFont());
        int digitos = String.valueOf(totalLinhas()).length();
        return new Dimension(fm.charWidth('0') * Math.max(digitos, 2) + 2 * MARGEM, 1);
    }

    @Override 
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(getFont());

        FontMetrics fm = g.getFontMetrics();
        Rectangle area = g.getClipBounds();
        Element raiz = editor.getDocument().getDefaultRootElement();
        int linhaAtual = raiz.getElementIndex(editor.getCaretPosition());

        try {
            int inicio = editor.viewToModel2D(new Point(0, area.y));
            int fim = editor.viewToModel2D(new Point(0, area.y + area.height));
            int primeira = raiz.getElementIndex(inicio);
            int ultima = raiz.getElementIndex(fim);

            for(int i = primeira; i <= ultima; i++){
                Rectangle r = editor.modelToView2D(raiz.getElement(i).getStartOffset()).getBounds();
                String numero = String.valueOf(i + 1);
                int x = getWidth() - MARGEM - fm.stringWidth(numero);
                int y = r.y + r.height - fm.getDescent();
                g.setColor(i == linhaAtual ? Color.BLACK : Color.GRAY);
                g.drawString(numero, x, y);
            }
        } catch (BadLocationException ignorada) {}
    }

    private void atualizar() {
        revalidate();
        repaint();
    }

    @Override
    public void insertUpdate(DocumentEvent e){ 
        atualizar(); 
    }

    @Override 
    public void removeUpdate(DocumentEvent e){ 
        atualizar(); 
    }

    @Override 
    public void changedUpdate(DocumentEvent e){ 
        atualizar(); 
    }

    @Override 
    public void caretUpdate(CaretEvent e){ 
        repaint(); 
    }
}