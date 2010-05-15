package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;
import de.fhma.ss10.srn.tischbein.gui.actions.LoginAction.LoginActionListener;

/**
 * Action zum Anlegen eines neuen Benutzers.
 * 
 * @author Smolli
 */
public final class NewUserAction extends AbstractAction implements LoginActionListener {

    /**
     * Listener-Interface für die NewUserAction.
     * 
     * @author Smolli
     */
    public interface NewUserActionListener extends LoginActionListener {
    }

    /** Serial UID. */
    private static final long serialVersionUID = -6210538596713120230L;

    /** Hält das Listner-Objekt. */
    private final NewUserActionListener listener;

    /**
     * Erstellt eine neue "Benutzer anlegen"-Action.
     * 
     * @param listenerObject
     *            Das Listener-Objekt.
     */
    public NewUserAction(final NewUserActionListener listenerObject) {
        this.listener = listenerObject;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            String pass = this.listener.getPassword();
            String name = this.listener.getUsername();

            if (pass.isEmpty()) {
                throw new Exception("Passwort darf nicht leer sein!");
            }

            if (name.isEmpty()) {
                throw new Exception("Benutzername darf nicht leer sein!");
            }

            Database.getInstance().createUser(name, pass);

            new LoginAction(this).actionPerformed(e);
        } catch (Exception ex) {
            GuiUtils.displayError("Kann den Benutzer nicht anlegen!", ex);
        }
    }

    @Override
    public String getPassword() {
        return this.listener.getPassword();
    }

    @Override
    public String getUsername() {
        return this.listener.getUsername();
    }

    @Override
    public Object getValue(final String key) {
        if (key.equals(Action.NAME)) {
            return "Neuer Benutzer";
        } else {
            return super.getValue(key);
        }
    }

    @Override
    public void login(final User user) {
        this.listener.login(user);
    }

}
