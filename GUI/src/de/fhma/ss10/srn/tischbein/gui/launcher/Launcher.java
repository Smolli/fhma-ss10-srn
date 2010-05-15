package de.fhma.ss10.srn.tischbein.gui.launcher;

import javax.swing.SwingUtilities;

import de.fhma.ss10.srn.tischbein.gui.MainFrame;

/**
 * Launcher-Klasse für die GUI-Applikation.
 * 
 * @author Smolli
 */
public final class Launcher {

    /** Hält des Hauptframe. */
    private static MainFrame frame;

    /**
     * Gibt den {@link MainFrame}, der die Hauptansicht enthält, zurück.
     * 
     * @return Der Hauptframe.
     */
    public static MainFrame getFrame() {
        return Launcher.frame;
    }

    /**
     * Haupteinstiegspunkt für die GUI-Applikation.
     * 
     * @param args
     *            Programmparameter.
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Launcher.frame = new MainFrame();

                Launcher.frame.setVisible(true);
            }

        });
    }

}
