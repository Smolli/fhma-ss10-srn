package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;
import de.fhma.ss10.srn.tischbein.gui.launcher.Launcher;

/**
 * Action zum einloggen eines Benutzers.
 * 
 * @author Smolli
 */
public final class LoginAction extends AbstractAction {

    /** Serial UID. */
    private static final long serialVersionUID = 4294231070983688689L;

    /** Hält den Benutzernamen. */
    private final JTextField username;
    /** Hält das Benutzerpasswort. */
    private final JPasswordField userpass;

    /**
     * Erstellt eine neue "Benutzer einloggen"-Action.
     * 
     * @param nameField
     *            Das {@link JTextField}, das den Benutzernamen enthält.
     * @param passField
     *            Das {@link JPasswordField}, das das Benutzerpasswort enthält.
     */
    public LoginAction(final JTextField nameField, final JPasswordField passField) {
        this.username = nameField;
        this.userpass = passField;
    }

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        try {
            String name = this.username.getText();

            if (name.isEmpty()) {
                throw new Exception("Benutzername muss angegeben werden!");
            }

            User user = Database.getInstance().loginUser(name, new String(this.userpass.getPassword()));

            Launcher.getFrame().setUser(user);
        } catch (Exception ex) {
            GuiUtils.displayError("Kann den Benutzer nicht einloggen!", ex);
        }
    }

    @Override
    public Object getValue(final String key) {
        if (key.equals(Action.NAME)) {
            return "Login";
        } else {
            return super.getValue(key);
        }
    }

}
