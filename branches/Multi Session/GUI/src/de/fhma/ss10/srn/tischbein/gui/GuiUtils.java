package de.fhma.ss10.srn.tischbein.gui;

import javax.swing.JOptionPane;

/**
 * Utilitiy-Klasse für alle Sachen in der GUI.
 * 
 * @author Smolli
 */
public final class GuiUtils {

    /**
     * Zeigt einen Modaldialog mit der Fehlermeldung an.
     * 
     * @param title
     *            Die Überschift des Dialogs.
     * @param ex
     *            Der Grund für den Fehler.
     */
    public static void displayError(final String title, final Exception ex) {
        ex.printStackTrace();

        String message = ex.getMessage();

        if (message == null) {
            message = "-- Unbekannt --";
        }

        StringBuilder sb = new StringBuilder(message);
        Throwable t = ex.getCause();

        while (t != null) {
            sb.append("\n\nWeil:\n");
            sb.append(t.getMessage());

            t = t.getCause();
        }

        JOptionPane.showMessageDialog(null, sb.toString(), title, JOptionPane.ERROR_MESSAGE);
    }

}
