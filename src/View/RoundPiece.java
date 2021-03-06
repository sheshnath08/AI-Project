package View;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Created by sheshnath on 4/10/2016.
 */
public class RoundPiece extends Piece {
    
    public Color color = Color.red;
    

    public RoundPiece() {
    }
    
    /**
     * Constructs a RoundPiece of the given color.
     **/
    public RoundPiece(Color color) {
        this.color = color;
    }
    
    /**
     * Draws this <code>RoundPiece</code> on the given <code>Graphics</code>.
     * Drawing should be limited to the provided <code>java.awt.Rectangle</code>.
     *
     */
    public void paint(Graphics g, Rectangle r) {
        Color oldColor = g.getColor();
        g.setColor(color);
        g.fillOval(r.x, r.y, r.width, r.height);
        if (this.equals(board.getSelectedPiece())) {
            g.setColor(Color.BLACK);
            g.drawOval(r.x, r.y, r.width, r.height);
        }
        g.setColor(oldColor);
    }
}