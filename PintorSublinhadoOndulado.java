import java.awt.*;
import javax.swing.text.*;

// Desenha o sublinhado.
public final class PintorSublinhadoOndulado implements Highlighter.HighlightPainter {
    private final Color cor;
    public PintorSublinhadoOndulado(Color cor){ 
        this.cor = cor;
    }
    
    @Override 
    public void paint(Graphics g, int p0, int p1, Shape limites, JTextComponent componente){
        try {
            var a = componente.modelToView2D(p0); 
            var b = componente.modelToView2D(p1);
            int x0 = (int) a.getX();
            int x1 = (int) b.getX();
            int y = (int) a.getMaxY() - 2;
            g.setColor(cor);

            for(int x = x0, sobe = 0; x < x1; x += 2, sobe ^= 1){
                g.drawLine(x, y + sobe, x + 2, y + (sobe ^ 1));
            }
        } catch (BadLocationException ignorada) {}
    }
}