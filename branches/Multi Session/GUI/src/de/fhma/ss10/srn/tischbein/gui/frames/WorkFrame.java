package de.fhma.ss10.srn.tischbein.gui.frames;

import javax.swing.DefaultListModel;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction;
import de.fhma.ss10.srn.tischbein.gui.actions.DeleteAction;
import de.fhma.ss10.srn.tischbein.gui.actions.LogoutAction;
import de.fhma.ss10.srn.tischbein.gui.actions.NewSessionAction;
import de.fhma.ss10.srn.tischbein.gui.actions.UploadAction;
import de.fhma.ss10.srn.tischbein.gui.launcher.Launcher;

/**
 * Arbeitsfenster.
 * 
 * @author Smolli
 */
public final class WorkFrame extends WorkFrameBase {

    /** Serial UID. */
    private static final long serialVersionUID = -5369888389274792872L;

    /**
     * Erstellt ein neues Arbeitsfenster mit dem übergebenen Benutzer.
     * 
     * @param user
     *            Der Benutzer.
     */
    public WorkFrame(final User user) {
        super();

        this.setCurrentUser(user);

        Database.getInstance();
        Database.addChangeListener(this);

        this.setupActions();

        this.userFilesList.setSelectionModel(new FilesSelectionModel(this, this.userFilesList));
        this.otherFilesList.setSelectionModel(new FilesSelectionModel(this, this.otherFilesList));
        this.accessTable.setModel(new AccessTableModel(this));
        this.initLists();

        this.setTitle(Launcher.PRODUCT_NAME + " - " + user.getName());

        this.setVisible(true);
    }

    @Override
    public void dispose() {
        Database.getInstance();
        Database.removeChangeListener(this);

        super.dispose();
    }

    //    @Override
    //    public void notifyChange() {
    //        this.initLists();
    //
    //        this.userFilesList.repaint();
    //        this.fileView.setText("");
    //    }

    /**
     * Initialisiert die drei GUI-Listen.
     */
    private void initLists() {
        this.userFilesList.setModel(new DefaultListModel());

        this.accessTable.setVisible(false);

        this.otherFilesList.setModel(new DefaultListModel());

        this.updateLists();
    }

    /**
     * Vergibt die Actions an die GUI-Elemente.
     */
    private void setupActions() {
        this.closeButton.setAction(new CloseAction(this));
        this.logoutButton.setAction(new LogoutAction(this));
        this.uploadButton.setAction(new UploadAction(this));
        this.deleteButton.setAction(new DeleteAction(this));
        this.newsessionButton.setAction(new NewSessionAction());
    }

}
