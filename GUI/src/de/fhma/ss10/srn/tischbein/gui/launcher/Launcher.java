package de.fhma.ss10.srn.tischbein.gui.launcher;

import javax.swing.SwingUtilities;

import de.fhma.ss10.srn.tischbein.gui.frames.LoginFrame;

/**
 * Launcher-Klasse für die GUI-Applikation.
 * 
 * @author Smolli
 */
public final class Launcher {

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
                LoginFrame frame = new LoginFrame();

                frame.setVisible(true);
            }

        });
    }

}
