package Clases;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class Toay {

    private BufferedImage fondo;

    public Toay() {
        try {
            Image img = new ImageIcon(getClass().getResource("/imagenes/Trazado_Toay.png")).getImage();
            // Convertir Image a BufferedImage para poder leer píxeles
            fondo = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = fondo.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen Trazado_Toay.png");
            e.printStackTrace();
        }
    }

    public void dibujar(Graphics g, int ancho, int alto) {
        if (fondo != null) {
            int imgAncho = fondo.getWidth();
            int imgAlto = fondo.getHeight();

            int x = (ancho - imgAncho) / 2;
            int y = (alto - imgAlto) / 2;

            g.drawImage(fondo, x, y, null);
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, ancho, alto);
            g.setColor(Color.WHITE);
            g.drawString("No se pudo cargar Trazado_Toay.png", 50, 50);
        }
    }

    public int getAnchoImagen() {
        return fondo != null ? fondo.getWidth() : 0;
    }

    public int getAltoImagen() {
        return fondo != null ? fondo.getHeight() : 0;
    }
}
