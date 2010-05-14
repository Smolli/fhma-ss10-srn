package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.gui.launcher.Launcher;

public class LoginAction extends AbstractAction {

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        String name = Launcher.getFrame().getUsername().getText();
        String pass = new String(Launcher.getFrame().getUserpass().getPassword());

        try {
            Database.getInstance().loginUser(name, pass);
        } catch (Exception ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(null, ex.getMessage(), "Kann den Benutzer nicht anlegen!",
                    JOptionPane.ERROR_MESSAGE);
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
