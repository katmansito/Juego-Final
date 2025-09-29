package Clases;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Panel extends JPanel implements KeyListener {

    private Galvez galvez; // Escenario
    private Auto auto;     // Auto

    // ==== Cron�metro ====
    private long inicioCronometro;
    private boolean cronometroCorriendo;

    // ==== Contador de vueltas ====
    private int vueltas;
    private double lastX; // posicion anterior del auto
    private double lastY;

    public Panel() {
        setBackground(Color.GREEN); 
        setFocusable(true);
        addKeyListener(this);

        galvez = new Galvez();
        auto = new Auto(3984, 3455);  // Posicion inicial del auto

        // Inicializar cron�metro
        inicioCronometro = System.nanoTime();
        cronometroCorriendo = true;

        // Inicializar vueltas
        vueltas = -1;
        lastX = auto.getX();
        lastY = auto.getY();

        // Timer para actualizar y repintar (~60 FPS)
        Timer timer = new Timer(16, e -> {
            double prevX = lastX;
            double prevY = lastY;

            auto.actualizar();

            detectarPasoPorMeta(prevX, prevY, auto.getX(), auto.getY());
            
            lastX = auto.getX();
            lastY = auto.getY();

            repaint();
        });
        timer.start();
    }

    private void detectarPasoPorMeta(double xAnterior, double yAnterior, double xActual, double yActual) {
        // Solo detectar cruce si en alg�n frame el auto pas� de un lado a otro de la l�nea x=2780
        boolean antesLadoIzq = xAnterior < 2780;
        boolean ahoraLadoDer = xActual >= 2780;

        boolean antesLadoDer = xAnterior > 2780;
        boolean ahoraLadoIzq = xActual <= 2780;

        boolean yDentroAntes = yAnterior >= 1000 && yAnterior <= 1194;
        boolean yDentroAhora = yActual >= 1000 && yActual <= 1194;

        if ((antesLadoIzq && ahoraLadoDer || antesLadoDer && ahoraLadoIzq) 
            && (yDentroAntes || yDentroAhora)) {
            vueltas++;
            reiniciarCronometro();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Calcular offset para centrar la c�mara en el auto
        int camX = (int) (getWidth() / 2 - auto.getX());
        int camY = (int) (getHeight() / 2 - auto.getY());

        // Aplicar transformaci�n
        g2d.translate(camX, camY);

        // Dibujar escenario
        if (galvez != null) {
            galvez.dibujar(g2d, galvez.getAnchoImagen(), galvez.getAltoImagen());
        }

        // Dibujar auto
        if (auto != null) {
            auto.dibujar(g2d);
        }

        // ===== HUD =====
        g2d.translate(-camX, -camY); 
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        // Mostrar posici�n
        g2d.drawString(
            String.format("Auto X: %.1f  Y: %.1f", auto.getX(), auto.getY()), 
            20, 20
        );

        // Mostrar cron�metro
        if (cronometroCorriendo) {
            long tiempoActual = System.nanoTime();
            long tiempoTranscurrido = tiempoActual - inicioCronometro;
            double segundos = tiempoTranscurrido / 1_000_000_000.0;

            int minutos = (int) (segundos / 60);
            double segRestantes = segundos % 60;

            g2d.drawString(
                String.format("Tiempo: %02d:%05.2f", minutos, segRestantes), 
                getWidth() - 150, 20
            );
        }

        // Mostrar contador de vueltas
        g2d.drawString("Vueltas: " + vueltas, getWidth() - 150, 40);

        g2d.dispose();
    }

    // ===== M�todos para controlar el cron�metro =====
    public void reiniciarCronometro() {
        inicioCronometro = System.nanoTime();
    }

    public void detenerCronometro() {
        cronometroCorriendo = false;
    }

    public void reanudarCronometro() {
        cronometroCorriendo = true;
        inicioCronometro = System.nanoTime();
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
