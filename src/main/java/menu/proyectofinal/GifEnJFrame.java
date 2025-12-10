package menu.proyectofinal;
import javax.swing.*;
public class GifEnJFrame extends JFrame {

    public GifEnJFrame() {
        JLabel etiqueta = new JLabel();
        etiqueta.setIcon(new ImageIcon(getClass().getResource("/imagenes/gifbuzon.gif")));
        
        add(etiqueta);
        setTitle("GIF en JFrame");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    public static void main(String[] args) {
        new GifEnJFrame();
    }
    
}

