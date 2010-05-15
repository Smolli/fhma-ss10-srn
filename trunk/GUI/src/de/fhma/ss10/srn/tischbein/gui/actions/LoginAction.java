package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;

/**
 * Action zum einloggen eines Benutzers.
 * 
 * @author Smolli
 */
public final class LoginAction extends AbstractAction {

    public interface LoginActionListener {

        String getPassword();

        String getUsername();

        void login(User user);

    }

    /** Serial UID. */
    private static final long serialVersionUID = 4294231070983688689L;

    private final LoginActionListener listener;

    /**
     * Erstellt eine neue "Benutzer einloggen"-Action.
     */
    public LoginAction(final LoginActionListener listenerObject) {
        this.listener = listenerObject;
    }

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        try {
            //            String name = this.username.getText();
            String name = this.listener.getUsername();

            if (name.isEmpty()) {
                throw new Exception("Benutzername muss angegeben werden!");
            }

            User user = Database.getInstance().loginUser(name, this.listener.getPassword());

            this.listener.login(user);
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
