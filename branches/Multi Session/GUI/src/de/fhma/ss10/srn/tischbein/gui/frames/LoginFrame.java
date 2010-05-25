package de.fhma.ss10.srn.tischbein.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;

import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction;
import de.fhma.ss10.srn.tischbein.gui.actions.LoginAction;
import de.fhma.ss10.srn.tischbein.gui.actions.NewUserAction;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction.CloseActionListener;
import de.fhma.ss10.srn.tischbein.gui.actions.NewUserAction.NewUserActionListener;
import de.fhma.ss10.srn.tischbein.gui.forms.LoginForm;

/**
 * Hauptfenster.
 * 
 * @author Smolli
 */
public final class LoginFrame extends LoginForm implements ActionListener, CloseActionListener, NewUserActionListener {

    /** Serial UID. */
    private static final long serialVersionUID = 5425989856036812026L;

    /**
     * Erstellt ein neues Login-Fenster.
     */
    public LoginFrame() {
        this.closeButton.setAction(new CloseAction(this));
        this.addUserButton.setAction(new NewUserAction(this));
        this.loginButton.setAction(new LoginAction(this));

        this.usernameField.addActionListener(this);
        this.usernameField.setModel(new DefaultComboBoxModel(Database.getInstance().getUsers()));
        this.usernameField.setSelectedItem("");

        this.setVisible(true);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        String command = event.getActionCommand();

        if (command.equals("comboBoxChanged")) {
            this.comboboxChanged();
        }
    }

    /**
     * Schließt das Hauptfenster und beendet die Applikation.
     */
    @Override
    public void close() {
        //        Database.getInstance().shutdown();

        this.dispose();
    }

    @Override
    public String getPassword() {
        return new String(this.passwordField.getPassword());
    }

    @Override
    public String getUsername() {
        Object item = this.usernameField.getSelectedItem();

        if (item instanceof String) {
            return (String) item;
        } else if (item instanceof User) {
            ((User) item).getName();
        }

        return null;
    }

    @Override
    public void login(final User newUser) throws CryptoException, DatabaseException {
        new WorkFrame(newUser);

        this.close();
    }

    /**
     * Wird aufgerufen, wenn sich der Inhalt der ComboBox geändert hat.
     */
    private void comboboxChanged() {
        Object item = this.usernameField.getSelectedItem();
        String username = null;

        if (item instanceof String) {
            User user;
            try {
                user = Database.getInstance().getUser((String) item);

                username = user.getName();
            } catch (DatabaseException e) {
                username = null;
            }
        } else if (item instanceof User) {
            username = ((User) item).getName();
        }

        this.setFields(item, username);
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
        if (username != null) {
            this.usernameField.setSelectedItem(username);
            this.loginButton.setEnabled(true);
            this.addUserButton.setEnabled(false);
        } else {
            this.loginButton.setEnabled(false);
            if (!((String) item).isEmpty()) {
                this.addUserButton.setEnabled(true);
            } else {
                this.addUserButton.setEnabled(false);
            }
        }
    }

}
