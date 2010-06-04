package de.fhma.ss10.srn.tischbein.gui.launcher;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

/**
 * Splash Screen.
 * 
 * @author Smolli
 */
public final class SplashScreen extends JDialog {

    /** Wartezeit für den Schließmechanismus. */
    private static final int ROUNDTRIP = 250;
    /** Die Dimension des SplashScreens. Muss dem Bild entsprechen. */
    private static final Dimension SPLAH_SIZE = new Dimension(400, 300);
    /** SplashScreen soll mind. 3 Sek. sichtbar sein. */
    private static final int DESTROY_TIMEOUT = 3000;
    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(SplashScreen.class);
    /** Serial UID. */
    private static final long serialVersionUID = -8362265543091405513L;
    /** Hält das Startbild von unserem großen Meister Johann Heinrich Wilhelm Tischbein. */
    private BufferedImage image = null;
    /** Hält den Startzeitpunkt, wann der SplashScreen gestartet wurde. */
    private final long start;
    /** Hält den Selbstzerstörungs-Timer. */
    private final Timer selfDestruct;

    /**
     * Standard Ctor.
     */
    public SplashScreen() {
        super();

        this.initComponents();

        this.selfDestruct = new Timer(SplashScreen.DESTROY_TIMEOUT, new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                SplashScreen.this.setVisible(false);
            }

        });

        this.selfDestruct.start();

        this.start = System.currentTimeMillis();

        this.setModal(true);
        this.setVisible(true);
    }

    @Override
    public void dispose() {
        this.selfDestruct.stop();

        super.dispose();
    }

    @Override
    public void paint(final Graphics graph) {
        if (this.image != null) {
            final Graphics2D graph2D = (Graphics2D) graph;

            graph2D.drawImage(this.image, null, 0, 0);
        }
    }

    @Override
    public void setVisible(final boolean value) {
        try {
            while (!value && (System.currentTimeMillis() - this.start < SplashScreen.DESTROY_TIMEOUT)) {
                Thread.sleep(SplashScreen.ROUNDTRIP);
            }
        } catch (final InterruptedException e) {
            SplashScreen.LOG.warn("Vorzeitiges Ende!", e);
        }

        super.setVisible(value);
    }

    /**
     * Erstellt alle Komponenten.
     */
    private void initComponents() {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setResizable(false);
        this.setUndecorated(true);
        this.setSize(SplashScreen.SPLAH_SIZE);
        this.setLocationRelativeTo(null);

        try {
            this.image = ImageIO.read(new File("img/tischbein.png"));
        } catch (final IOException e) {
            SplashScreen.LOG.error("Kann SplashScreen-Grafik nicht laden!", e);
        }
    }

}
