package de.fhma.ss10.srn.tischbein.gui.launcher;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.gui.GuiUtils;
import de.fhma.ss10.srn.tischbein.gui.frames.LoginFrame;

/**
 * Launcher-Klasse für die GUI-Applikation.
 * 
 * @author Smolli
 */
public final class Launcher {

    /**
     * {@link Runnable} für das Programm.
     * 
     * @author Smolli
     */
    private static final class ProgramThread implements Runnable {

        @Override
        public void run() {

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (final Exception e) {
                Launcher.LOG.error("Error setting native Look&Feel!", e);
            }

            new LoginFrame();
        }

    }

    /**
     * {@link Runnable} für den SplashScreen.
     * 
     * @author Smolli
     */
    private static final class SplashScreenThread implements Runnable {

        @Override
        public void run() {
            new SplashScreen();
        }

    }

    /** Hält den Produktnamen. */
    public static final String PRODUCT_NAME = "Tischbein v0.99 RC1";
    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(Launcher.class);

    /**
     * Haupteinstiegspunkt für die GUI-Applikation.
     * 
     * @param args
     *            Programmparameter.
     */
    public static void main(final String[] args) {
        final Launcher launcher = new Launcher();

        launcher.run();
    }

    /**
     * Geschützter Standard-Ctor.
     */
    private Launcher() {
    }

    /**
     * Einstiegspunkt.
     */
    public void run() {
        try {
            SwingUtilities.invokeLater(new SplashScreenThread());

            SwingUtilities.invokeLater(new ProgramThread());
        } catch (final Exception e) {
            GuiUtils.displayError("Unerwarteter Fehler!", e);
        }
    }
}
