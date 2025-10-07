package Clases;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import javax.swing.ImageIcon;

public class Auto {

    private double x, y;           
    private double velocidad;      
    private double angulo;         

    // Parámetros base
    private final double aceleracionBase = 0.3; 
    private final double frenoBase = 0.3;       
    private final double rotacionBase = 4.0;    
    private final double friccionBase = 0.03;   
    private final double velocidadMaxBase = 18.0; 

    private final int ancho = 68;
    private final int alto = 25;

    // Terreno (factores dinámicos)
    private double factorAceleracion = 1.0;
    private double factorVelMax = 1.0;
    private double friccionExtra = 0.0;

    // Teclas
    private boolean adelante, atras, izquierda, derecha;

    // Imagen del auto
    private Image imagenAuto;

    // Animación de llamas
    private Image[] llamas;
    private int llamaFrame = 0;
    private int llamaDelay = 0;

    public Auto(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.velocidad = 5;
        this.angulo = 180;

        // Cargar imagen del auto
        ImageIcon icon = new ImageIcon("src/Imagenes/Auto_Base.png");
        imagenAuto = icon.getImage();

        // Cargar imágenes de llamas
        llamas = new Image[4];
        for (int i = 0; i < llamas.length; i++) {
            llamas[i] = new ImageIcon("src/Imagenes/llama" + (i + 1) + ".png").getImage();
        }
    }

    public void actualizar() {
        // Acelerar y frenar
        if (adelante) velocidad += aceleracionBase * factorAceleracion;
        if (atras) velocidad -= frenoBase;

        // Limitar velocidad máxima según el terreno
        double velMax = velocidadMaxBase * factorVelMax;
        if (velocidad > velMax) velocidad = velMax;
        if (velocidad < -velMax / 2) velocidad = -velMax / 2; // marcha atrás limitada

        // ===== Girar dependiente de la velocidad (inverso) =====
        double velAbs = Math.abs(velocidad);
        double factorVelocidad = 1.0 / (1.0 + velAbs / 6.0); 
        double rotacion = rotacionBase * factorVelocidad;

        if (izquierda) angulo -= rotacion;
        if (derecha) angulo += rotacion;

        // Aplicar movimiento
        x += velocidad * Math.cos(Math.toRadians(angulo));
        y += velocidad * Math.sin(Math.toRadians(angulo));

        // Fricción base + extra del terreno
        double frTotal = friccionBase + friccionExtra;
        if (velocidad > 0) {
            velocidad *= (1 - frTotal);
            if (velocidad < 0.05) velocidad = 0;
        } else if (velocidad < 0) {
            velocidad *= (1 - frTotal);
            if (velocidad > -0.05) velocidad = 0;
        }
    }

    public void dibujar(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(x, y);
        g2d.rotate(Math.toRadians(angulo + 180));

        // Dibuja el auto
        g2d.drawImage(imagenAuto, -ancho / 2, -alto / 2, null);

        // ===== Animación de llamas =====
        boolean mostrarLlama = (atras || (!adelante && velocidad > 0)) && velocidad >= 0;

        if (mostrarLlama) {
            llamaDelay++;
            if (llamaDelay > 5) { 
                llamaFrame = (llamaFrame + 1) % llamas.length;
                llamaDelay = 0;
            }

            int offsetX = -ancho / 5;
            int offsetY = 13;
            g2d.drawImage(llamas[llamaFrame], offsetX, offsetY, null);
        }

      
        g2d.dispose();
    }

    // ===== Controles WASD (Auto 1) =====
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

    // ===== Controles Flechas (Auto 2) =====
    public void keyPressedFlechas(int keyCode) {
        if (keyCode == KeyEvent.VK_UP) adelante = true;
        if (keyCode == KeyEvent.VK_DOWN) atras = true;
        if (keyCode == KeyEvent.VK_LEFT) izquierda = true;
        if (keyCode == KeyEvent.VK_RIGHT) derecha = true;
    }

    public void keyReleasedFlechas(int keyCode) {
        if (keyCode == KeyEvent.VK_UP) adelante = false;
        if (keyCode == KeyEvent.VK_DOWN) atras = false;
        if (keyCode == KeyEvent.VK_LEFT) izquierda = false;
        if (keyCode == KeyEvent.VK_RIGHT) derecha = false;
    }

    // ===== Getters =====
    public double getX() { return x; }
    public double getY() { return y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }

    // ===== Hitbox rotada =====
    public Polygon getHitboxRotada() {
        int hw = ancho / 2;
        int hh = alto / 2;

        int[] puntosX = { -hw, hw, hw, -hw };
        int[] puntosY = { -hh, -hh, hh, hh };

        double rad = Math.toRadians(angulo);

        int[] rotX = new int[4];
        int[] rotY = new int[4];

        for (int i = 0; i < 4; i++) {
            double rx = puntosX[i] * Math.cos(rad) - puntosY[i] * Math.sin(rad);
            double ry = puntosX[i] * Math.sin(rad) + puntosY[i] * Math.cos(rad);

            rotX[i] = (int) (x + rx);
            rotY[i] = (int) (y + ry);
        }

        return new Polygon(rotX, rotY, 4);
    }

    // ===== Ajustes según terreno =====
    public void aplicarTerreno(ColisionesGalvez.Terreno terreno) {
        switch (terreno) {
            case PISTA:
                friccionExtra = 0.0;
                factorAceleracion = 1.0;
                factorVelMax = 1.0;
                break;
            case PIANO:
                friccionExtra = 0.02;
                factorAceleracion = 0.9;
                factorVelMax = 0.95;
                break;
            case PASTO:
                friccionExtra = 0.06;
                factorAceleracion = 0.7;
                factorVelMax = 0.6;
                break;
            case GRAVA:
                friccionExtra = 0.1;
                factorAceleracion = 0.5;
                factorVelMax = 0.4;
                break;
            case MURO:
                if (velocidad > 0) velocidad = 0; 
                friccionExtra = 0.0;
                factorAceleracion = 0.0;
                factorVelMax = 0.0;
                break;
            case FUERA:
                friccionExtra = 0.08;
                factorAceleracion = 0.6;
                factorVelMax = 0.5;
                break;
        }
    }

    // ===== Detectar choque y empujar (con hitbox rotada) =====
    public void detectarChoqueYEmpujar(Auto otro) {
        Polygon p1 = this.getHitboxRotada();
        Polygon p2 = otro.getHitboxRotada();

        Area area1 = new Area(p1);
        area1.intersect(new Area(p2));

        if (!area1.isEmpty()) {
            // Vector entre centros
            double dx = this.x - otro.x;
            double dy = this.y - otro.y;
            double distancia = Math.sqrt(dx * dx + dy * dy);

            if (distancia == 0) distancia = 0.1;

            double empuje = 5.0;
            double pushX = (dx / distancia) * empuje;
            double pushY = (dy / distancia) * empuje;

            this.x += pushX;
            this.y += pushY;
            otro.x -= pushX;
            otro.y -= pushY;
        }
    }
}
