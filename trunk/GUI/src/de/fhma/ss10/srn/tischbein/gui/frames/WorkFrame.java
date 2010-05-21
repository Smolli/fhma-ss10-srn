package de.fhma.ss10.srn.tischbein.gui.frames;

import java.util.Vector;

import javax.swing.Action;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;
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

    /**
     * Spezialisiertes {@link TableModel} für die Rechtevergabe der Dateien.
     * 
     * @author Smolli
     */
    private final class AccessTableModel implements TableModel {

        /** Hält die Liste der angezeigten Benutzer. */
        private final Vector<User> users = Database.getInstance().getUsers(WorkFrame.this.currentUser);

        @Override
        public void addTableModelListener(final TableModelListener l) {
            // TODO Auto-generated method stub

        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Boolean.class;

                case 1:
                    return String.class;

                default:
                    return null;
            }
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(final int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "";

                case 1:
                    return "Benutzername";

                default:
                    return null;
            }
        }

        @Override
        public int getRowCount() {
            return this.users.size();
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            User selectedUser = this.users.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return WorkFrame.this.currentUser.getFileListObject().hasAccess(selectedUser,
                            WorkFrame.this.selectedFile);

                case 1:
                    return selectedUser.getName();

                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return true;

                case 1:
                    return false;

                default:
                    return false;
            }
        }

        @Override
        public void removeTableModelListener(final TableModelListener l) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
            if (columnIndex != 0) {
                return;
            }

            User selectedUser = this.users.get(rowIndex);
            try {
                WorkFrame.this.currentUser.getFileListObject().setAccess(selectedUser, WorkFrame.this.selectedFile,
                        (Boolean) value);
            } catch (Exception e) {
                GuiUtils.displayError("Konnte Recht nicht speichern!", e);
            }
        }
    }

    /**
     * Spezialisiertes {@link ListSelectionModel} für die Datei-Liste.
     * 
     * @author Smolli
     */
    private final class FilesSelectionModel extends DefaultListSelectionModel {

        /** Serial UID. */
        private static final long serialVersionUID = 9031173846551914083L;

        {
            this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        @Override
        public void setSelectionInterval(final int first, final int last) {
            super.setSelectionInterval(first, last);

            WorkFrame.this.selectedFile = WorkFrame.this.currentUser.getFileListObject().getFileList().get(last);

            WorkFrame.this.userTable.repaint();

            System.out.println(WorkFrame.this.selectedFile + " wurde ausgewählt");
        }
    }

    /** Serial UID. */
    private static final long serialVersionUID = -5369888389274792872L;

    /** Hält den eingeloggten Benutzer. */
    private final User currentUser;
    /** Hält die momentan ausgewählte Datei. */
    private transient FileItem selectedFile = null;

    /**
     * Erstellt ein neues Arbeitsfenster mit dem übergebenen Benutzer.
     * 
     * @param user
     *            Der Benutzer.
     */
    public WorkFrame(final User user) {
        this.currentUser = user;

        this.setupActions();

        this.myFilesLlist.setSelectionModel(new FilesSelectionModel());
        this.myFilesLlist.setListData(this.currentUser.getFileListObject().getFileList());

        this.userTable.setModel(new AccessTableModel());

        this.otherFilesList.setListData(this.currentUser.getFileListObject().getAccessList());

        this.setTitle("Tischbein v0.2 - " + user.getName());

        this.setVisible(true);
    }

    @Override
    public void close() {
        Database.getInstance().shutdown();

        this.dispose();
    }

    @Override
    public void logout() {
        this.currentUser.lock();

        new LoginFrame();

        this.close();
    }

    /**
     * Vergibt die {@link Action}s an die GUI-Elemente.
     */
    private void setupActions() {
        this.closeButton.setAction(new CloseAction(this));
        this.logoutButton.setAction(new LogoutAction(this));
        this.uploadButton.setAction(new UploadAction(this.currentUser));
        this.deleteButton.setAction(new DeleteAction());
    }

}
