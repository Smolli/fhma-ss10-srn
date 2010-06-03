package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.fhma.ss10.srn.tischbein.core.db.dbms.Database;
import de.fhma.ss10.srn.tischbein.core.db.user.User;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;
import de.fhma.ss10.srn.tischbein.gui.actions.LoginAction.LoginActionParent;

/**
 * Action zum Anlegen eines neuen Benutzers.
 * 
 * @author Smolli
 */
public final class NewUserAction extends AbstractAction implements LoginActionParent {

    /** Serial UID. */
    private static final long serialVersionUID = -6210538596713120230L;

    /** Hält das Listner-Objekt. */
    private final LoginActionParent listener;

    /**
     * Erstellt eine neue "Benutzer anlegen"-Action.
     * 
     * @param listenerObject
     *            Das Listener-Objekt.
     */
    public NewUserAction(final LoginActionParent listenerObject) {
        super();

        this.listener = listenerObject;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        try {
            final String pass = this.listener.getPassword();
            final String name = this.listener.getUsername();

            if (pass.isEmpty()) {
                throw new IllegalArgumentException("Passwort darf nicht leer sein!");
            }

            if (name.isEmpty()) {
                throw new IllegalArgumentException("Benutzername darf nicht leer sein!");
            }

            Database.getInstance().createUser(name, pass);

            new LoginAction(this).actionPerformed(event);
        } catch (final Exception ex) {
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
        Object result;

        if (key.equals(Action.NAME)) {
            result = "Neuer Benutzer";
        } else {
            result = super.getValue(key);
        }

        return result;
    }

    @Override
    public void login(final User user) {
        try {
            this.listener.login(user);
        } catch (final Exception e) {
            GuiUtils.displayError("kann den Benutzer nicht einloggen!", e);
        }
    }

}
