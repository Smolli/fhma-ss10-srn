package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.fhma.ss10.srn.tischbein.core.db.Database;

/**
 * Action zum Anlegen eines neuen Benutzers.
 * 
 * @author Smolli
 */
public final class NewUserAction extends AbstractAction {

    /** Serial UID. */
    private static final long serialVersionUID = -6210538596713120230L;

    /** Hält den Benutzernamen. */
    private final JTextField username;
    /** Hält das Benutzerpasswort. */
    private final JPasswordField userpass;

    /**
     * Erstellt eine neue "Benutzer anlegen"-Action.
     * 
     * @param nameField
     *            Das {@link JTextField} mit dem Benutzernamen.
     * @param passField
     *            Das {@link JPasswordField} mit dem Benutzerpasswort.
     */
    public NewUserAction(final JTextField nameField, final JPasswordField passField) {
        this.username = nameField;
        this.userpass = passField;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            Database.getInstance().createUser(this.username.getText(), new String(this.userpass.getPassword()));

            new LoginAction(this.username, this.userpass).actionPerformed(e);
        } catch (Exception ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(null, ex.getMessage(), "Kann den Benutzer nicht anlegen!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public Object getValue(final String key) {
        if (key.equals(Action.NAME)) {
            return "Neuer Benutzer";
        } else {
            return super.getValue(key);
        }
    }

}
