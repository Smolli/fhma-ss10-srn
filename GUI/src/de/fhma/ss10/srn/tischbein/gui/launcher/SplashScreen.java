package de.fhma.ss10.srn.tischbein.gui.launcher;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

/**
 * Splash Screen.
 * 
 * @author Smolli
 */
public final class SplashScreen extends JDialog {

    private static final Logger LOG = Logger.getLogger(SplashScreen.class);
    /** Serial UID. */
    private static final long serialVersionUID = -8362265543091405513L;
    /** Hält das Startbild von unserem großen Meister Johann Heinrich Wilhelm Tischbein. */
    private BufferedImage image = null;
    private final long start;

    /**
     * Standard Ctor.
     */
    public SplashScreen() {
        super((JFrame) null, false);

        this.start = System.currentTimeMillis();

        this.initComponents();

        this.setVisible(true);
    }

    @Override
    public void paint(final Graphics graph) {
        super.paint(graph);

        if (this.image != null) {
            final Graphics2D graph2D = (Graphics2D) graph;

            graph2D.drawImage(this.image, null, 0, 0);
        }
    }

    @Override
    public void setVisible(final boolean value) {
        try {
            while (!value && (System.currentTimeMillis() - this.start < 5000)) {
                Thread.sleep(250);
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

        this.setSize(400, 300);

        try {
            this.image = ImageIO.read(new File("img/tischbein.png"));
        } catch (final IOException e) {
            SplashScreen.LOG.error("Kann SplashScreen-Grafik nicht laden!", e);
        }
    }

}
