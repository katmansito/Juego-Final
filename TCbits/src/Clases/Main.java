package Clases;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        // Crear ventana principal
        JFrame ventana = new JFrame("TCbits");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setUndecorated(true); // Sin barra de título ni bordes

        // Obtener resolución de pantalla
        GraphicsDevice dispositivo = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode displayMode = dispositivo.getDisplayMode();
        int anchoPantalla = displayMode.getWidth();
        int altoPantalla = displayMode.getHeight();

        // Panel de menú principal (fondo negro + botón)
        JPanel panelMenu = new JPanel();
        panelMenu.setBackground(Color.BLACK);
        panelMenu.setLayout(new GridBagLayout()); // Para centrar el botón
        GridBagConstraints gbc = new GridBagConstraints();

        // Crear botón Jugar
        JButton btnJugar = new JButton("Jugar");
        btnJugar.setFont(new Font("Arial", Font.BOLD, 40));
        btnJugar.setBackground(Color.DARK_GRAY);
        btnJugar.setForeground(Color.WHITE);
        btnJugar.setFocusPainted(false);
        btnJugar.setPreferredSize(new Dimension(300, 100));

        // Agregar botón al panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelMenu.add(btnJugar, gbc);

        ventana.add(panelMenu);
        ventana.pack();

        // Forzar pantalla completa
        dispositivo.setFullScreenWindow(ventana);

        // Listener del botón
        btnJugar.addActionListener(e -> {
            ventana.remove(panelMenu); // Quitar menú
            Panel panelJuego = new Panel(); // Crear tu panel de juego
            panelJuego.setFocusable(true);
            panelJuego.requestFocusInWindow();
            ventana.add(panelJuego); // ✅ ahora agregamos la variable correcta
            ventana.revalidate();
            ventana.repaint();
            panelJuego.requestFocus(); // Asegurar que reciba input de teclado
        });

        // Permitir salir con ESC
        ventana.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    dispositivo.setFullScreenWindow(null);
                    ventana.dispose();
                }
            }
        });

        ventana.setVisible(true);
    }
}
