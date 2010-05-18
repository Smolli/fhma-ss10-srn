package de.fhma.ss10.srn.tischbein.gui.frames;

import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction;
import de.fhma.ss10.srn.tischbein.gui.actions.DeleteAction;
import de.fhma.ss10.srn.tischbein.gui.actions.LogoutAction;
import de.fhma.ss10.srn.tischbein.gui.actions.UploadAction;
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
     * @throws DatabaseException
     * @throws CryptoException
     */
    public WorkFrame(final User newUser) throws CryptoException, DatabaseException {
        this.closeButton.setAction(new CloseAction(this));
        this.logoutButton.setAction(new LogoutAction(this));
        this.uploadButton.setAction(new UploadAction());
        this.deleteButton.setAction(new DeleteAction());

        this.user = newUser;

        this.myFilesLlist.setListData(this.user.getFileListObject().getFileList());

        this.setVisible(true);
    }

    @Override
    public void close() {
        Database.shutdown();

        this.dispose();
    }

    @Override
    public void logout() {
        this.user.lock();

        new LoginFrame();

        this.close();
    }

}
