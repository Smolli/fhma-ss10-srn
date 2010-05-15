package de.fhma.ss10.srn.tischbein.gui.frames;

import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction;
import de.fhma.ss10.srn.tischbein.gui.actions.LogoutAction;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction.CloseActionListener;
import de.fhma.ss10.srn.tischbein.gui.actions.LogoutAction.LogoutActionListener;
import de.fhma.ss10.srn.tischbein.gui.forms.WorkForm;

/**
 * Arbeitsfenster.
 * 
 * @author Smolli
 */
public final class WorkFrame extends WorkForm implements CloseActionListener, LogoutActionListener {

    /** Serial UID. */
    private static final long serialVersionUID = -5369888389274792872L;

    /** Hält den eingeloggten Benutzer. */
    private final User user;

    /**
     * Erstellt ein neues Arbeitsfenster mit dem übergebenen Benutzer.
     * 
     * @param newUser
     *            Der Benutzer.
     */
    public WorkFrame(final User newUser) {
        this.closeButton.setAction(new CloseAction(this));
        this.logoutButton.setAction(new LogoutAction(this));

        this.user = newUser;

        this.setVisible(true);
    }

    @Override
    public void close() {
        this.dispose();
    }

    @Override
    public void logout() {
        this.user.lock();

        new LoginFrame();

        this.close();
    }

}