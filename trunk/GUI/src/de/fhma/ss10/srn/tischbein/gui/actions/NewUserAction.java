package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.gui.launcher.Launcher;

public class NewUserAction extends AbstractAction {

    private static final long serialVersionUID = -6210538596713120230L;

    @Override
    public void actionPerformed(final ActionEvent e) {
        String name = Launcher.getFrame().getUsername().getText();
        String pass = new String(Launcher.getFrame().getUserpass().getPassword());

        try {
            Database.getInstance().createUser(name, pass);

            new LoginAction().actionPerformed(e);
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
