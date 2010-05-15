package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.fhma.ss10.srn.tischbein.core.db.Database;

public class NewUserAction extends AbstractAction {

    private static final long serialVersionUID = -6210538596713120230L;
    private final JTextField username;
    private final JPasswordField userpass;

    public NewUserAction(final JTextField username, final JPasswordField userpass) {
        this.username = username;
        this.userpass = userpass;
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
