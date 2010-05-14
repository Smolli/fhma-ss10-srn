package de.fhma.ss10.srn.tischbein.gui.launcher;

import javax.swing.SwingUtilities;

import de.fhma.ss10.srn.tischbein.gui.MainFrame;

public class Launcher implements Runnable {

    private static MainFrame frame;

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Launcher());
    }

    @Override
    public void run() {
        Launcher.frame = new MainFrame();

        Launcher.frame.setVisible(true);
    }

    public static MainFrame getFrame() {
        return Launcher.frame;
    }

}
