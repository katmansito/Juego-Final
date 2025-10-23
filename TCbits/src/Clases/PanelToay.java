package Clases;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

public class PanelToay extends JPanel implements KeyListener {

    private Toay toay;
    private Auto auto;
    private Auto auto2;
    private LimitesCircuitos limites;

    private boolean modoVersus;

    // ==== Cronómetro ====
    private long inicioCronometro;
    private boolean cronometroCorriendo;

    private int vueltas;

    private double lastX1, lastY1;
    private double lastX2, lastY2;

    private static final long VUELTA_COOLDOWN_NANOS = 18_000_000_000L;
    private long ultimaVueltaTime;

    // ==== Constructor ====
    public PanelToay(boolean modoVersus, String marca1, String marca2) {
        this.modoVersus = modoVersus;
        setFocusable(true);
        addKeyListener(this);

        toay = new Toay();
        limites = new LimitesCircuitos("src/imagenes/Limite_Toay.png");

        // Crear autos según el modo
        auto = new Auto(2087, 1303, marca1, "Toay");

        if (modoVersus) {
            auto2 = new Auto(2087, 1392, marca2, "Toay");
        }else {
            auto2 = null;
        }

        setPreferredSize(new Dimension(toay.getAnchoImagen(), toay.getAltoImagen()));

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
        // Actualiza posición y colisiones con límites
        a.actualizar(limites);

        // Detectar tipo de terreno según el centro del auto
        int xCentro = (int) (a.getX() + a.getAncho() / 2.0);
        int yCentro = (int) (a.getY() + a.getAlto() / 2.0);
        LimitesCircuitos.Terreno terreno = limites.detectarTerreno(xCentro, yCentro);
        a.aplicarTerreno(terreno);
    }

    private void detectarPasoPorMeta(double xAnterior, double yAnterior, double xActual, double yActual) {
        // Meta en Toay: x = 3087, y entre 1230 y 1480
        double xMeta = 2087;
        double xCruce = 21500; // margen que asegura el cruce válido (antihorario)
        int yMin = 1230;
        int yMax = 1480;

        // Verifica si el auto cruza de derecha a izquierda (antihorario)
        boolean antesDelLadoDerecho = xAnterior > xMeta;
        boolean ahoraDelLadoIzquierdo = xActual < xCruce;

        // Verifica que esté dentro del rango vertical de la meta
        boolean yDentro = (yActual >= yMin && yActual <= yMax) || (yAnterior >= yMin && yAnterior <= yMax);

        if (antesDelLadoDerecho && ahoraDelLadoIzquierdo && yDentro) {
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
        
        // === PRIMERO: pintar TODO el panel con el color verde base ===
        g2d.setColor(new Color(0x0e4500));
        g2d.fillRect(0, 0, getWidth(), getHeight());

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
        double maxX = toay.getAnchoImagen() - mitadVistaAncho;
        double minY = mitadVistaAlto;
        double maxY = toay.getAltoImagen() - mitadVistaAlto;

        if (centroX < minX) centroX = minX;
        if (centroX > maxX) centroX = maxX;
        if (centroY < minY) centroY = minY;
        if (centroY > maxY) centroY = maxY;

        AffineTransform at = new AffineTransform();
        at.translate(getWidth() / 2.0, getHeight() / 2.0);
        at.scale(zoom, zoom);
        at.translate(-centroX, -centroY);

        g2d.setTransform(at);

        toay.dibujar(g2d, toay.getAnchoImagen(), toay.getAltoImagen());
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
