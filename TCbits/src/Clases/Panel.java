package Clases;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Panel extends JPanel implements KeyListener {

    private Galvez galvez; // Escenario
    private Auto auto;     // Auto

    public Panel() {
        setBackground(Color.GREEN); // Fondo del panel
        setFocusable(true);
        addKeyListener(this);

        galvez = new Galvez();
        auto = new Auto(2807, 1144);  // Posición inicial del auto

        // Timer para actualizar y repintar (~60 FPS)
        Timer timer = new Timer(16, e -> {
            auto.actualizar();

           repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Calcular offset para centrar la cámara en el auto
        int camX = (int) (getWidth() / 2 - auto.getX());
        int camY = (int) (getHeight() / 2 - auto.getY());

        // Aplicar transformación
        g2d.translate(camX, camY);

        // Dibujar escenario
        if (galvez != null) {
            galvez.dibujar(g2d, galvez.getAnchoImagen(), galvez.getAltoImagen());
        }

        // Dibujar auto
        if (auto != null) {
            auto.dibujar(g2d);
        }

        // ===== HUD: mostrar posición del auto =====
        g2d.translate(-camX, -camY); // volver a coordenadas del panel
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(
            String.format("Auto X: %.1f  Y: %.1f", auto.getX(), auto.getY()), 
            20, 20
        );

        g2d.dispose();
    }

    // ===== KeyListener =====
    @Override
    public void keyPressed(KeyEvent e) {
        if (auto != null) auto.keyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (auto != null) auto.keyReleased(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
