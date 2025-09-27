package Clases;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Auto {

    private double x, y;           // Posición
    private double velocidad;      // Velocidad actual
    private double angulo;         // Dirección en grados
    private final double aceleracion = 0.3; // aceleración
    private final double freno = 0.2;       // freno al presionar S
    private final double rotacion = 4.0;    // grados por frame
    private final double friccion = 0.15;   // fricción para detenerse más rápido
    private final double velocidadMax = 30.0; // velocidad máxima

    private final int ancho = 40;
    private final int alto = 20;

    // Teclas
    private boolean adelante, atras, izquierda, derecha;

    public Auto(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.velocidad = 0;
        this.angulo = 180;
    }

    public void actualizar() {
        // Acelerar y frenar
        if (adelante) {
            velocidad += aceleracion;
        }
        if (atras) {
            velocidad -= freno;
        }

        // Limitar velocidad máxima
        if (velocidad > velocidadMax) velocidad = velocidadMax;
        if (velocidad < -velocidadMax) velocidad = -velocidadMax;

        // Girar
        if (izquierda) {
            angulo -= rotacion;
        }
        if (derecha) {
            angulo += rotacion;
        }

        // Aplicar movimiento
        x += velocidad * Math.cos(Math.toRadians(angulo));
        y += velocidad * Math.sin(Math.toRadians(angulo));

        // Fricción para ir frenando solo
        if (velocidad > 0) {
            velocidad -= friccion;
            if (velocidad < 0) velocidad = 0;
        } else if (velocidad < 0) {
            velocidad += friccion;
            if (velocidad > 0) velocidad = 0;
        }
    }

    public void dibujar(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(x, y);
        g2d.rotate(Math.toRadians(angulo));

        g2d.setColor(Color.RED);
        g2d.fillRect(-ancho / 2, -alto / 2, ancho, alto);

        g2d.dispose();
    }

    // Métodos para manejar teclas
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_W) adelante = true;
        if (keyCode == KeyEvent.VK_S) atras = true;
        if (keyCode == KeyEvent.VK_A) izquierda = true;
        if (keyCode == KeyEvent.VK_D) derecha = true;
    }

    public void keyReleased(int keyCode) {
        if (keyCode == KeyEvent.VK_W) adelante = false;
        if (keyCode == KeyEvent.VK_S) atras = false;
        if (keyCode == KeyEvent.VK_A) izquierda = false;
        if (keyCode == KeyEvent.VK_D) derecha = false;
    }
    
   
    public double getX() { return x; }
    public double getY() { return y; }
}
