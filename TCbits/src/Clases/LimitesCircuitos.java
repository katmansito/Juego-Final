package Clases;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class LimitesCircuitos {

    private BufferedImage mapa;

    public LimitesCircuitos(String ruta) {
        try {
            mapa = ImageIO.read(new File(ruta));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Tipos de terreno
    public enum Terreno {
        PISTA,      // rojo ff0000
        PASTO,      // azul 3a00ff
        GRAVA,      // amarillo ffdf00
        PIANO,      // rosa ff00fa
        MURO,       // verde 35ff00
        FUERA       // cualquier otro
    }

    // Detectar el terreno en un pixel (x,y)
    public Terreno detectarTerreno(int x, int y) {
        if (x < 0 || y < 0 || x >= mapa.getWidth() || y >= mapa.getHeight()) {
            return Terreno.FUERA;
        }

        // Tomar el color ignorando alpha
        int rgb = mapa.getRGB(x, y) & 0xFFFFFF;

        switch (rgb) {
            case 0xFF0000: return Terreno.PISTA;   // rojo
            case 0x3A00FF: return Terreno.PASTO;   // azul
            case 0xFFDF00: return Terreno.GRAVA;   // amarillo
            case 0xFF00FA: return Terreno.PIANO;   // rosa
            case 0x35FF00: return Terreno.MURO;    // verde
            default:       return Terreno.FUERA;
        }
    }
}
