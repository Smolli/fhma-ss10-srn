package de.fhma.ss10.srn.tischbein.gui.frames;

import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction;
import de.fhma.ss10.srn.tischbein.gui.actions.LoginAction;
import de.fhma.ss10.srn.tischbein.gui.actions.NewUserAction;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction.CloseActionListener;
import de.fhma.ss10.srn.tischbein.gui.actions.LoginAction.LoginActionListener;
import de.fhma.ss10.srn.tischbein.gui.actions.NewUserAction.NewUserActionListener;
import de.fhma.ss10.srn.tischbein.gui.forms.LoginForm;

/**
 * Hauptfenster.
 * 
 * @author Smolli
 */
public final class LoginFrame extends LoginForm implements LoginActionListener, CloseActionListener,
        NewUserActionListener {

    /** Serial UID. */
    private static final long serialVersionUID = 5425989856036812026L;

    /** Hält das Benutzerobjekt. */
    private User user;

    public LoginFrame() {
        this.closeButton.setAction(new CloseAction(this));
        this.addUserButton.setAction(new NewUserAction(this));
        this.loginButton.setAction(new LoginAction(this));
    }

    /**
     * Schließt das Hauptfenster und beendet die Applikation.
     */
    @Override
    public void close() {
        this.dispose();
    }

    @Override
    public String getPassword() {
        return new String(this.passwordField.getPassword());
    }

    @Override
    public String getUsername() {
        return this.usernameField.getText();
    }

    /**
     * Setzt den eingeloggten Benutzer und schaltet die Arbeitsansicht frei.
     * 
     * @param newUser
     *            Der Benutzer.
     */
    @Override
    public void login(final User newUser) {
        WorkFrame frame = new WorkFrame(newUser);

        this.close();
    }

}