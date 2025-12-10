package menu.proyectofinal;
import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_Arduino;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProyectoFinal extends JFrame implements SerialPortEventListener {

    private static final PanamaHitek_Arduino arduino = new PanamaHitek_Arduino();

    private final JTextArea logArea;
    private final JLabel lblestadobzn;
    private final JLabel lblpack;
    private final JButton btncerrar;

    public ProyectoFinal() {
        super("Monitoreo Buzón Inteligente");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 450);
        this.setLayout(new BorderLayout());

        lblestadobzn = new JLabel();
        lblestadobzn.setForeground(Color.BLUE);
        lblpack = new JLabel();
        lblpack.setForeground(Color.GRAY);

        
        btncerrar = new JButton("Cerrar Buzoonn Manualmente");
        btncerrar.addActionListener(e -> enviarComandoCerrar());
        btncerrar.setVisible(false);

        JPanel pnlestado = new JPanel();
        pnlestado.add(lblestadobzn);
        pnlestado.add(lblpack);
        pnlestado.add(btncerrar);
        this.add(pnlestado, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        this.add(new JScrollPane(logArea), BorderLayout.CENTER);

        iniciarComunicacion();

        this.setVisible(true);
    }

    private void iniciarComunicacion() {
        try {
            arduino.arduinoRXTX("COM9", 9600, this);

        } catch (ArduinoException e) {
            logArea.append("[FATAL] No se pudo conectar al Arduino: " + e.getMessage() + "\n");
            lblestadobzn.setText("ERROR DE CONEXION");
            lblestadobzn.setForeground(Color.RED);
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        try {
            if (arduino.isMessageAvailable()) {
                String data = arduino.printMessage();
                String dataLimpia = data.trim().replaceAll("[^\\p{Print}]", "");
                procesarDatosArduino(dataLimpia);
            }
        } catch (SerialPortException | ArduinoException ex) {
            logArea.append("[ERROR] Error de comunicación serial: " + ex.getMessage() + "\n");
        }
    }
    
    private void enviarComandoCerrar() {
        try {
            arduino.sendData("CERRAR_MANUAL\n");
            logArea.append("<< Comando enviado: CERRAR_MANUAL\n");

            
            lblestadobzn.setForeground(Color.CYAN);
            btncerrar.setVisible(false);
        } catch (ArduinoException | SerialPortException e) {
            logArea.append("Nose envio el comando para el cierre: " + e.getMessage() + "\n");
        }
    }
    private void procesarDatosArduino(String data) {
        logArea.append(">> " + data + "\n");

        
        if (data.contains("ueo reconocido: abriendo buzon")) {
            lblestadobzn.setIcon(new ImageIcon(getClass().getResource("/imagenes/gifbuzon.gif")));
            lblestadobzn.setForeground(Color.ORANGE);
            btncerrar.setVisible(true);
        } else if (data.contains("epartidor reconocido: abriendo para depositar la entrega") || data.contains("PIN correcto: abriendo buzon")) {
            lblestadobzn.setIcon(new ImageIcon(getClass().getResource("/imagenes/cartero2.gif")));
            lblestadobzn.setForeground(Color.ORANGE);
            btncerrar.setVisible(false);
        }
        
        if (data.contains("Buzon iniciado") || data.contains("cerrarBuzon") || data.contains("ID desconocicdo")) { 
            lblestadobzn.setIcon(new ImageIcon(getClass().getResource("/imagenes/cerradito.PNG")));
            lblestadobzn.setForeground(Color.BLUE);
            btncerrar.setVisible(false);
        }

        if (data.contains("Paquete detectado")) {
            lblestadobzn.setIcon(new ImageIcon(getClass().getResource("/imagenes/paquete.gif")));
            lblestadobzn.setForeground(Color.GREEN);
            btncerrar.setVisible(false);
        } else if (data.contains("Sensor lee vacio")) { 
            lblestadobzn.setForeground(Color.GRAY);
        }

        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ProyectoFinal::new);
    }
}
