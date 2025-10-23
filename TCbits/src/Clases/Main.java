package Clases;

import javax.swing.*;
import java.awt.*;

public class Main {

    private static int seleccionJugador1 = -1;
    private static int seleccionJugador2 = -1;

    // Nombres de los autos
    private static final String[] NOMBRES_AUTOS = {"Ford", "Chevrolet", "Torino", "Dodge", "Toyota"};

    // Nombres de los circuitos
    private static final String[] NOMBRES_CIRCUITOS = {"Galvez", "La Plata", "Toay"};

    public static void main(String[] args) {
        JFrame ventana = new JFrame("TCbits");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setUndecorated(true);

        GraphicsDevice dispositivo = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        dispositivo.setFullScreenWindow(ventana);

        JPanel panelMenu = crearMenuPrincipal(ventana, dispositivo);
        ventana.add(panelMenu);
        ventana.setVisible(true);
    }

    private static JPanel crearMenuPrincipal(JFrame ventana, GraphicsDevice dispositivo) {
        JPanel panelMenu = new JPanel();
        panelMenu.setBackground(Color.BLACK);
        panelMenu.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 0, 30, 0);

        JButton btnVueltaRapida = crearBoton("Vuelta Rápida");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelMenu.add(btnVueltaRapida, gbc);

        JButton btnVersus = crearBoton("Versus");
        gbc.gridy = 1;
        panelMenu.add(btnVersus, gbc);

        btnVueltaRapida.addActionListener(e -> {
            ventana.remove(panelMenu);
            mostrarSeleccionAutos(ventana, false, dispositivo);
        });

        btnVersus.addActionListener(e -> {
            ventana.remove(panelMenu);
            mostrarSeleccionAutos(ventana, true, dispositivo);
        });

        ventana.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    dispositivo.setFullScreenWindow(null);
                    ventana.dispose();
                }
            }
        });

        return panelMenu;
    }

    private static JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 40));
        boton.setBackground(Color.DARK_GRAY);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setPreferredSize(new Dimension(400, 100));
        return boton;
    }

    private static void mostrarSeleccionAutos(JFrame ventana, boolean modoVersus, GraphicsDevice dispositivo) {
        JPanel panelSeleccion = new JPanel();
        panelSeleccion.setBackground(Color.BLACK);
        panelSeleccion.setLayout(new BorderLayout());

        JLabel titulo = new JLabel(modoVersus ? "Jugador 1 - Elige tu auto" : "Elige tu auto", SwingConstants.CENTER);
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 40));
        panelSeleccion.add(titulo, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel();
        panelBotones.setOpaque(false);
        panelBotones.setLayout(new GridLayout(1, NOMBRES_AUTOS.length, 30, 0));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(100, 100, 100, 100));

        JButton[] botonesAutos = new JButton[NOMBRES_AUTOS.length];
        for (int i = 0; i < NOMBRES_AUTOS.length; i++) {
            int autoID = i;
            botonesAutos[i] = new JButton(NOMBRES_AUTOS[i]);
            botonesAutos[i].setFont(new Font("Arial", Font.BOLD, 30));
            botonesAutos[i].setBackground(Color.GRAY);
            botonesAutos[i].setForeground(Color.WHITE);
            botonesAutos[i].setFocusPainted(false);
            botonesAutos[i].addActionListener(e -> {
                manejarSeleccionAuto(ventana, dispositivo, modoVersus, autoID, titulo, botonesAutos, panelSeleccion);
            });
            panelBotones.add(botonesAutos[i]);
        }

        panelSeleccion.add(panelBotones, BorderLayout.CENTER);

        ventana.add(panelSeleccion);
        ventana.revalidate();
        ventana.repaint();
    }

    private static void manejarSeleccionAuto(JFrame ventana, GraphicsDevice dispositivo, boolean modoVersus,
                                             int autoID, JLabel titulo, JButton[] botonesAutos, JPanel panelSeleccion) {

        if (!modoVersus) {
            seleccionJugador1 = autoID;
            mostrarSeleccionCircuito(ventana, false, NOMBRES_AUTOS[seleccionJugador1], "");
            return;
        }

        if (seleccionJugador1 == -1) {
            seleccionJugador1 = autoID;
            titulo.setText("Jugador 2 - Elige tu auto");
            botonesAutos[autoID].setEnabled(false);
        } else if (seleccionJugador2 == -1 && autoID != seleccionJugador1) {
            seleccionJugador2 = autoID;
            mostrarSeleccionCircuito(ventana, true,
                    NOMBRES_AUTOS[seleccionJugador1], NOMBRES_AUTOS[seleccionJugador2]);
        }
    }

    private static void mostrarSeleccionCircuito(JFrame ventana, boolean modoVersus,
                                                  String marca1, String marca2) {
        ventana.getContentPane().removeAll();

        JPanel panelCircuito = new JPanel();
        panelCircuito.setBackground(Color.BLACK);
        panelCircuito.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 30, 30, 30);

        JLabel titulo = new JLabel("Selecciona el Circuito", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 40));
        titulo.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        panelCircuito.add(titulo, gbc);

        String[] circuitos = NOMBRES_CIRCUITOS;
        for (int i = 0; i < circuitos.length; i++) {
            int idx = i;
            JButton btnCircuito = new JButton(circuitos[i]);
            btnCircuito.setFont(new Font("Arial", Font.BOLD, 30));
            btnCircuito.setBackground(Color.GRAY);
            btnCircuito.setForeground(Color.WHITE);
            btnCircuito.setFocusPainted(false);
            gbc.gridwidth = 1;
            gbc.gridy = 1;
            gbc.gridx = i;
            btnCircuito.addActionListener(e -> {
                iniciarCircuito(ventana, modoVersus, marca1, marca2, circuitos[idx]);
            });
            panelCircuito.add(btnCircuito, gbc);
        }

        ventana.add(panelCircuito);
        ventana.revalidate();
        ventana.repaint();
    }

    private static void iniciarCircuito(JFrame ventana, boolean modoVersus,
                                        String marca1, String marca2, String circuito) {
        ventana.getContentPane().removeAll();
        JPanel panelJuego;
        switch (circuito) {
            case "Galvez":
                panelJuego = new PanelGalvez(modoVersus, marca1, marca2);
                break;
            case "La Plata":
                panelJuego = new PanelLaPlata(modoVersus, marca1, marca2);
                break;
            case "Toay":
                panelJuego = new PanelToay(modoVersus, marca1, marca2);
                break;
            default:
                panelJuego = new PanelGalvez(modoVersus, marca1, marca2);
        }

        panelJuego.setFocusable(true);
        ventana.add(panelJuego);
        ventana.revalidate();
        ventana.repaint();
        panelJuego.requestFocus();
    }
}
