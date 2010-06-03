package de.fhma.ss10.srn.tischbein.gui;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

/**
 * Utilitiy-Klasse für alle Sachen in der GUI.
 * 
 * @author Smolli
 */
public final class GuiUtils {

    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(GuiUtils.class);

    public static void display(final String message) {
        JOptionPane.showMessageDialog(null, message, "Tischbein sagt:", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Zeigt einen Modaldialog mit der Fehlermeldung an.
     * 
     * @param title
     *            Die Überschift des Dialogs.
     * @param exception
     *            Der Grund für den Fehler.
     */
    public static void displayError(final String title, final Exception exception) {
        GuiUtils.LOG.error(title, exception);

        String message = exception.getMessage();

        if (message == null) {
            message = "-- Unbekannt --";
        }

        final StringBuilder text = new StringBuilder(message);
        Throwable cause = exception.getCause();

        while (cause != null) {
            text.append("\n\nWeil:\n");
            text.append(cause.getMessage());

            cause = cause.getCause();
        }

        JOptionPane.showMessageDialog(null, text.toString(), title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Geschützter Standard-Ctor.
     */
    private GuiUtils() {
        super();
    }
}
