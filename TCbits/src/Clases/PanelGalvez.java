package Clases;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

public class PanelGalvez extends JPanel implements KeyListener {

    private Galvez galvez;
    private Auto auto;
    private Auto auto2;
    private ColisionesGalvez colisiones;

    // ==== Cronómetro ====
    private long inicioCronometro;
    private boolean cronometroCorriendo;

    // ==== Contador de vueltas ====
    private int vueltas;

    // Últimas posiciones de cada auto (para detectar cruce entre frames)
    private double lastX1, lastY1;
    private double lastX2, lastY2;

    // ==== Control de cooldown entre vueltas ====
    // 22 segundos en nanosegundos
    private static final long VUELTA_COOLDOWN_NANOS = 22_000_000_000L;
    private long ultimaVueltaTime; // momento en que se contó la última vuelta (nanoTime)

    // ==== Constructor ====
    public PanelGalvez() {
        setFocusable(true);
        addKeyListener(this);

        galvez = new Galvez();
        auto = new Auto(3146, 1183);
        auto2 = new Auto(3146, 1098);

        colisiones = new ColisionesGalvez("src/imagenes/Limite_Galvez.png");

        setPreferredSize(new Dimension(galvez.getAnchoImagen(), galvez.getAltoImagen()));

        inicioCronometro = System.nanoTime();
        cronometroCorriendo = true;

        // Empezamos con 0 vueltas
        vueltas = 0;

        // Inicializamos últimas posiciones con la posición actual (primer frame)
        lastX1 = auto.getX();
        lastY1 = auto.getY();
        lastX2 = auto2.getX();
        lastY2 = auto2.getY();

        // Permitimos que la primera vuelta pueda contarse de inmediato:
        ultimaVueltaTime = System.nanoTime() - VUELTA_COOLDOWN_NANOS;

        Timer timer = new Timer(16, e -> {
            // Guardamos posiciones previas por auto
            double prevX1 = lastX1;
            double prevY1 = lastY1;
            double prevX2 = lastX2;
            double prevY2 = lastY2;

            // Actualizamos física/posiciones
            actualizarAuto(auto);
            actualizarAuto(auto2);

            // Choque entre autos
            auto.detectarChoqueYEmpujar(auto2);

            // Detectamos cruce para ambos autos (compara prev -> actual)
            detectarPasoPorMeta(prevX1, prevY1, auto.getX(), auto.getY());
            detectarPasoPorMeta(prevX2, prevY2, auto2.getX(), auto2.getY());

            // Actualizamos últimas posiciones para el siguiente frame
            lastX1 = auto.getX();
            lastY1 = auto.getY();
            lastX2 = auto2.getX();
            lastY2 = auto2.getY();

            repaint();
        });
        timer.start();
    }

    // ==== Actualizar Auto con colisiones ====
    private void actualizarAuto(Auto a) {
        a.actualizar();

        int xCentro = (int) (a.getX() + a.getAncho() / 2.0);
        int yCentro = (int) (a.getY() + a.getAlto() / 2.0);

        ColisionesGalvez.Terreno terreno = colisiones.detectarTerreno(xCentro, yCentro);
        a.aplicarTerreno(terreno);
    }

    /**
     * Detecta cruce de la línea de meta (x = 3110) comparando la posición anterior y la actual.
     * Cuenta la vuelta solo si han pasado al menos 22 segundos desde la última vuelta contada.
     */
    private void detectarPasoPorMeta(double xAnterior, double yAnterior, double xActual, double yActual) {
        // Línea de meta en X = 3110
        boolean antesLadoIzq = xAnterior < 3110;
        boolean ahoraLadoDer = xActual >= 3110;

        boolean antesLadoDer = xAnterior > 3110;
        boolean ahoraLadoIzq = xActual <= 3110;

        // Rango válido en Y: 1040 .. 1264
        boolean yDentroAntes = yAnterior >= 1040 && yAnterior <= 1264;
        boolean yDentroAhora = yActual >= 1040 && yActual <= 1264;

        boolean cruzo = ( (antesLadoIzq && ahoraLadoDer) || (antesLadoDer && ahoraLadoIzq) )
                        && (yDentroAntes || yDentroAhora);

        if (cruzo) {
            long ahora = System.nanoTime();
            long desdeUltima = ahora - ultimaVueltaTime;

            if (desdeUltima >= VUELTA_COOLDOWN_NANOS) {
                vueltas++;
                ultimaVueltaTime = ahora;
                reiniciarCronometro();
            }
            // si no alcanzó el cooldown, no hacemos nada (evita dobles)
        }
    }

    // ==== paintComponent ====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        double x1 = auto.getX();
        double y1 = auto.getY();
        double x2 = auto2.getX();
        double y2 = auto2.getY();

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
        double maxX = galvez.getAnchoImagen() - mitadVistaAncho;
        double minY = mitadVistaAlto;
        double maxY = galvez.getAltoImagen() - mitadVistaAlto;

        if (centroX < minX) centroX = minX;
        if (centroX > maxX) centroX = maxX;
        if (centroY < minY) centroY = minY;
        if (centroY > maxY) centroY = maxY;

        AffineTransform at = new AffineTransform();
        at.translate(getWidth() / 2.0, getHeight() / 2.0);
        at.scale(zoom, zoom);
        at.translate(-centroX, -centroY);

        g2d.setTransform(at);

        if (galvez != null) {
            galvez.dibujar(g2d, galvez.getAnchoImagen(), galvez.getAltoImagen());
        }

        if (auto != null) auto.dibujar(g2d);
        if (auto2 != null) auto2.dibujar(g2d);

        // HUD: volvemos al espacio de pantalla para dibujar texto
        g2d.setTransform(new AffineTransform());
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        g2d.drawString(String.format("Auto1 X: %.1f  Y: %.1f", auto.getX(), auto.getY()), 20, 20);
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

    // ==== Métodos Cronómetro ====
    public void reiniciarCronometro() { inicioCronometro = System.nanoTime(); }
    public void detenerCronometro() { cronometroCorriendo = false; }
    public void reanudarCronometro() { cronometroCorriendo = true; inicioCronometro = System.nanoTime(); }

    // ==== KeyListener ====
    @Override
    public void keyPressed(KeyEvent e) {
        if (auto != null) auto.keyPressed(e.getKeyCode());
        if (auto2 != null) auto2.keyPressedFlechas(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (auto != null) auto.keyReleased(e.getKeyCode());
        if (auto2 != null) auto2.keyReleasedFlechas(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
