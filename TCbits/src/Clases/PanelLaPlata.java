package Clases;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

public class PanelLaPlata extends JPanel implements KeyListener {

    private LaPlata laPlata;
    private Auto auto;
    private Auto auto2;

    private boolean modoVersus; // 👈 nuevo

    // ==== Cronómetro ====
    private long inicioCronometro;
    private boolean cronometroCorriendo;

    private int vueltas;

    private double lastX1, lastY1;
    private double lastX2, lastY2;

    private static final long VUELTA_COOLDOWN_NANOS = 22_000_000_000L;
    private long ultimaVueltaTime;

    // ==== Constructor ====
    public PanelLaPlata(boolean modoVersus, String marca1, String marca2) {
        this.modoVersus = modoVersus;
        setFocusable(true);
        addKeyListener(this);

        laPlata = new LaPlata();

        // Crear autos según el modo
        auto = new Auto(3146, 1183, marca1); // 👈 primer auto con su marca

        if (modoVersus) {
            auto2 = new Auto(3146, 1098, marca2); // 👈 segundo auto con su marca
        } else {
            auto2 = null; // 👈 solo un auto en modo vuelta rápida
        }

        setPreferredSize(new Dimension(laPlata.getAnchoImagen(), laPlata.getAltoImagen()));

        inicioCronometro = System.nanoTime();
        cronometroCorriendo = true;
        vueltas = 0;

        lastX1 = auto.getX();
        lastY1 = auto.getY();
        if (auto2 != null) {
            lastX2 = auto2.getX();
            lastY2 = auto2.getY();
        }

        ultimaVueltaTime = System.nanoTime() - VUELTA_COOLDOWN_NANOS;

        Timer timer = new Timer(16, e -> {
            double prevX1 = lastX1;
            double prevY1 = lastY1;
            double prevX2 = lastX2;
            double prevY2 = lastY2;

            actualizarAuto(auto);
            if (auto2 != null) actualizarAuto(auto2);

            if (modoVersus && auto2 != null) {
                auto.detectarChoqueYEmpujar(auto2);
                detectarPasoPorMeta(prevX2, prevY2, auto2.getX(), auto2.getY());
                lastX2 = auto2.getX();
                lastY2 = auto2.getY();
            }

            detectarPasoPorMeta(prevX1, prevY1, auto.getX(), auto.getY());
            lastX1 = auto.getX();
            lastY1 = auto.getY();

            repaint();
        });
        timer.start();
    }

    private void actualizarAuto(Auto a) {
        a.actualizar();
        int xCentro = (int) (a.getX() + a.getAncho() / 2.0);
        int yCentro = (int) (a.getY() + a.getAlto() / 2.0);
        // Por ahora no hay LimitesCircuitos
    }

    private void detectarPasoPorMeta(double xAnterior, double yAnterior, double xActual, double yActual) {
        boolean antesLadoIzq = xAnterior < 3110;
        boolean ahoraLadoDer = xActual >= 3110;
        boolean antesLadoDer = xAnterior > 3110;
        boolean ahoraLadoIzq = xActual <= 3110;
        boolean yDentroAntes = yAnterior >= 1040 && yAnterior <= 1264;
        boolean yDentroAhora = yActual >= 1040 && yActual <= 1264;

        boolean cruzo = ((antesLadoIzq && ahoraLadoDer) || (antesLadoDer && ahoraLadoIzq))
                        && (yDentroAntes || yDentroAhora);

        if (cruzo) {
            long ahora = System.nanoTime();
            if (ahora - ultimaVueltaTime >= VUELTA_COOLDOWN_NANOS) {
                vueltas++;
                ultimaVueltaTime = ahora;
                reiniciarCronometro();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        double x1 = auto.getX();
        double y1 = auto.getY();
        double x2 = modoVersus && auto2 != null ? auto2.getX() : auto.getX();
        double y2 = modoVersus && auto2 != null ? auto2.getY() : auto.getY();

        double centroX = (x1 + x2) / 2.0;
        double centroY = (y1 + y2) / 2.0;

        double margen = 200;
        double dx = Math.abs(x1 - x2) + auto.getAncho() + margen;
        double dy = Math.abs(y1 - y2) + auto.getAlto() + margen;

        double zoomX = getWidth() / dx;
        double zoomY = getHeight() / dy;
        double zoom = Math.min(zoomX, zoomY);

        double minZoom = 0.3;
        double maxZoom = 2.0;
        zoom = Math.max(minZoom, Math.min(maxZoom, zoom));

        double vistaAncho = getWidth() / zoom;
        double vistaAlto = getHeight() / zoom;
        double mitadVistaAncho = vistaAncho / 2.0;
        double mitadVistaAlto = vistaAlto / 2.0;

        double minX = mitadVistaAncho;
        double maxX = laPlata.getAnchoImagen() - mitadVistaAncho;
        double minY = mitadVistaAlto;
        double maxY = laPlata.getAltoImagen() - mitadVistaAlto;

        if (centroX < minX) centroX = minX;
        if (centroX > maxX) centroX = maxX;
        if (centroY < minY) centroY = minY;
        if (centroY > maxY) centroY = maxY;

        AffineTransform at = new AffineTransform();
        at.translate(getWidth() / 2.0, getHeight() / 2.0);
        at.scale(zoom, zoom);
        at.translate(-centroX, -centroY);

        g2d.setTransform(at);

        laPlata.dibujar(g2d, laPlata.getAnchoImagen(), laPlata.getAltoImagen());
        auto.dibujar(g2d);
        if (modoVersus && auto2 != null) auto2.dibujar(g2d);

        // HUD
        g2d.setTransform(new AffineTransform());
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        g2d.drawString(String.format("Auto1 X: %.1f  Y: %.1f", auto.getX(), auto.getY()), 20, 20);
        if (modoVersus && auto2 != null)
            g2d.drawString(String.format("Auto2 X: %.1f  Y: %.1f", auto2.getX(), auto2.getY()), 20, 40);

        if (cronometroCorriendo) {
            long tiempoActual = System.nanoTime();
            long tiempoTranscurrido = tiempoActual - inicioCronometro;
            double segundos = tiempoTranscurrido / 1_000_000_000.0;
            int minutos = (int) (segundos / 60);
            double segRestantes = segundos % 60;
            g2d.drawString(String.format("Tiempo: %02d:%05.2f", minutos, segRestantes), getWidth() - 150, 20);
        }

        g2d.drawString("Vueltas: " + vueltas, getWidth() - 150, 40);
        g2d.dispose();
    }

    public void reiniciarCronometro() { inicioCronometro = System.nanoTime(); }
    public void detenerCronometro() { cronometroCorriendo = false; }
    public void reanudarCronometro() { cronometroCorriendo = true; inicioCronometro = System.nanoTime(); }

    @Override
    public void keyPressed(KeyEvent e) {
        if (auto != null) auto.keyPressed(e.getKeyCode());
        if (modoVersus && auto2 != null) auto2.keyPressedFlechas(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (auto != null) auto.keyReleased(e.getKeyCode());
        if (modoVersus && auto2 != null) auto2.keyReleasedFlechas(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
