package de.fhma.ss10.srn.tischbein.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction;
import de.fhma.ss10.srn.tischbein.gui.actions.LoginAction;
import de.fhma.ss10.srn.tischbein.gui.actions.NewUserAction;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction.CloseActionParent;
import de.fhma.ss10.srn.tischbein.gui.actions.LoginAction.LoginActionParent;
import de.fhma.ss10.srn.tischbein.gui.forms.LoginForm;

/**
 * Hauptfenster.
 * 
 * @author Smolli
 */
public final class LoginFrame extends LoginForm implements ActionListener, CloseActionParent, LoginActionParent {

    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(LoginFrame.class);
    /** Serial UID. */
    private static final long serialVersionUID = 5425989856036812026L;

    /**
     * Erstellt ein neues Login-Fenster.
     */
    public LoginFrame() {
        super();

        this.closeButton.setAction(new CloseAction(this));
        this.addUserButton.setAction(new NewUserAction(this));
        this.loginButton.setAction(new LoginAction(this));

        this.usernameField.addActionListener(this);
        this.usernameField.setModel(new DefaultComboBoxModel(Database.getInstance().getUsers().toArray()));
        this.usernameField.setSelectedItem("");

        this.setVisible(true);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final String command = event.getActionCommand();

        if ("comboBoxChanged".equals(command)) {
            this.comboboxChanged();
        }
    }

    /**
     * Schließt das Hauptfenster und beendet die Applikation.
     */
    @Override
    public void closeFrame() {
        //        Database.getInstance().shutdown();

        this.dispose();
    }

    @Override
    public String getPassword() {
        return String.valueOf(this.passwordField.getPassword());
    }

    @Override
    public String getUsername() {
        final Object item = this.usernameField.getSelectedItem();
        String result = null;

        if (item instanceof String) {
            result = (String) item;
        } else if (item instanceof User) {
            result = ((User) item).getName();
        }

        return result;
    }

    @Override
    public void login(final User newUser) throws CryptoException, DatabaseException {
        new WorkFrame(newUser);

        this.closeFrame();
    }

    /**
     * Wird aufgerufen, wenn sich der Inhalt der ComboBox geändert hat.
     */
    private void comboboxChanged() {
        final Object item = this.usernameField.getSelectedItem();
        String username = null;

        if (item instanceof String) {
            username = this.getUsername((String) item);
        } else if (item instanceof User) {
            username = ((User) item).getName();
        }

        this.setFields(item, username);
    }

    /**
     * Holt den Benutzernamen aus der Datenbank. Dient dazu, die Benutzer so zu schreiben, wie sie sich ursprünglich
     * registriet haben.
     * <p>
     * Beim Registrieren wird der Name so in die Datenbank geschrieben, wie ihn der Benutzer eingegeben hat. Die ID ist
     * allerdings immer in Kleinbuchstaben. Deshalb sind susi und SUsi die selben Benutzer.
     * 
     * @param name
     *            Der eingegebene Name.
     * @return Der Benutzername in der Datenbank.
     */
    private String getUsername(final String name) {
        String username = null;

        try {
            if (Database.getInstance().hasUser(name)) {
                final User user = Database.getInstance().getUser(name);

                username = user.getName();
            }
        } catch (final DatabaseException e) {
            LoginFrame.LOG.debug("Seltsame Störung im Raum-Zeit-Gefüge!", e);
        }

        return username;
    }

    /**
     * Setzt die einzelnen Felder anhand der übergebenen Parameter.
     * 
     * @param item
     *            Das gewählte Element der ComboBox.
     * @param username
     *            Der ermittelte Benutzername.
     */
    private void setFields(final Object item, final String username) {
        if (username == null) {
            this.loginButton.setEnabled(false);
            if (((String) item).isEmpty()) {
                this.addUserButton.setEnabled(false);
            } else {
                this.addUserButton.setEnabled(true);
            }
        } else {
            this.usernameField.setSelectedItem(username);
            this.loginButton.setEnabled(true);
            this.addUserButton.setEnabled(false);
        }
    }

}
