package de.fhma.ss10.srn.tischbein.gui.launcher;

import de.fhma.ss10.srn.tischbein.gui.GuiUtils;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import de.fhma.ss10.srn.tischbein.gui.frames.LoginFrame;

/**
 * Launcher-Klasse für die GUI-Applikation.
 * 
 * @author Smolli
 */
public final class Launcher {

    /** Hält den Produktnamen. */
    public static final String PRODUCT_NAME = "Tischbein v0.2";

    /**
     * Haupteinstiegspunkt für die GUI-Applikation.
     * 
     * @param args
     *            Programmparameter.
     */
    public static void main(final String[] args) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception e) {
                        System.out.println("Error setting native LAF: " + e);
                    }

                    new LoginFrame();
                }

            });
        } catch (Exception e) {
            GuiUtils.displayError("Unerwarteter Fehler!", e);
        }
    }
}
