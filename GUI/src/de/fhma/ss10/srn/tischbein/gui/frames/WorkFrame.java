package de.fhma.ss10.srn.tischbein.gui.frames;

import java.util.Vector;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.FileItemException;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction;
import de.fhma.ss10.srn.tischbein.gui.actions.DeleteAction;
import de.fhma.ss10.srn.tischbein.gui.actions.LogoutAction;
import de.fhma.ss10.srn.tischbein.gui.actions.UploadAction;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction.CloseActionListener;
import de.fhma.ss10.srn.tischbein.gui.actions.DeleteAction.DeleteActionListener;
import de.fhma.ss10.srn.tischbein.gui.actions.LogoutAction.LogoutActionListener;
import de.fhma.ss10.srn.tischbein.gui.forms.WorkForm;
import de.fhma.ss10.srn.tischbein.gui.frames.UploadFrame.UploadFrameListener;
import de.fhma.ss10.srn.tischbein.gui.launcher.Launcher;

/**
 * Arbeitsfenster.
 * 
 * @author Smolli
 */
public final class WorkFrame extends WorkForm implements CloseActionListener, LogoutActionListener,
        UploadFrameListener, DeleteActionListener {

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
                    return "Erlaubnis";

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
                    return WorkFrame.this.currentUser.getDescriptor().hasAccess(selectedUser,
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
        }

        @Override
        public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
            if (columnIndex != 0) {
                return;
            }

            User selectedUser = this.users.get(rowIndex);

            try {
                WorkFrame.this.currentUser.getDescriptor().setAccess(selectedUser, WorkFrame.this.selectedFile,
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
        /** Hält die verknüpfte JList. */
        private final JList list;

        /**
         * Standard-Ctor. Verknüpft das Model mit einer {@link JList}.
         * 
         * @param guiList
         *            Die {@link JList}, mit dem das Model verknüft ist.
         */
        public FilesSelectionModel(final JList guiList) {
            this.list = guiList;
            this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        @Override
        public void setSelectionInterval(final int first, final int last) {
            super.setSelectionInterval(first, last);

            System.out.println("" + first + ", " + last);

            WorkFrame.this.selectFile((FileItem) this.list.getModel().getElementAt(last));
        }
    }

    /** Serial UID. */
    private static final long serialVersionUID = -5369888389274792872L;
    /** Hält den eingeloggten Benutzer. */
    private final transient User currentUser;
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

        //        Database.getInstance().addChangeListener(this);

        this.setupActions();

        this.initLists();

        this.setTitle(Launcher.PRODUCT_NAME + " - " + user.getName());

        this.setVisible(true);
    }

    @Override
    public void close() {
        Database.getInstance().shutdown();

        this.dispose();
    }

    //    @Override
    //    public void dispose() {
    //        Database.getInstance().removeChangeListener(this);
    //    }

    @Override
    public User getCurrentUser() {
        return this.currentUser;
    }

    @Override
    public FileItem getSelectedFile() {
        return this.selectedFile;
    }

    @Override
    public void logout() {
        this.currentUser.lock();

        new LoginFrame();

        this.close();
    }

    @Override
    public void notifyChange() {
        this.initLists();

        this.userFilesList.repaint();
        this.fileView.setText("");
    }

    /**
     * Initialisiert die drei GUI-Listen.
     */
    private void initLists() {
        this.userFilesList.setSelectionModel(new FilesSelectionModel(this.userFilesList));
        this.userFilesList.setListData(this.currentUser.getDescriptor().getFileList());

        this.accessTable.setModel(new AccessTableModel());
        this.accessTable.setVisible(false);

        this.otherFilesList.setSelectionModel(new FilesSelectionModel(this.otherFilesList));
        this.otherFilesList.setListData(this.currentUser.getDescriptor().getAccessList());
    }

    /**
     * Wird aufgerufen, wenn der Benutzer entweder in die {@link WorkForm#userFilesList} oder
     * {@link WorkForm#otherFilesList} geklickt hat.
     * 
     * @param file
     *            Die Datei, die der Benutzer ausgewählt hat.
     */
    private void selectFile(final FileItem file) {
        try {
            this.selectedFile = file;

            this.setFileView(file);

            this.setAccessTable(file);

            System.out.println(WorkFrame.this.selectedFile + " wurde ausgewählt");
        } catch (Exception e) {
            GuiUtils.displayError("Datei kann nicht angezeigt werden!", e);
        }
    }

    /**
     * Setzt die Access-Table-Ansicht.
     * 
     * @param file
     *            Die Datei.
     */
    private void setAccessTable(final FileItem file) {
        if ((file != null) && (file.getOwner() == this.currentUser)) {
            this.accessTable.setVisible(true);
            this.accessTable.repaint();
        } else {
            this.accessTable.setVisible(false);
        }
    }

    /**
     * Setzt die FileView entsprechende der übergebenen Datei.
     * 
     * @param file
     *            Die Datei.
     * @throws FileItemException
     *             Wird geworfen, wenn die Datei nicht geladen werden kann.
     */
    private void setFileView(final FileItem file) throws FileItemException {
        if (file != null) {
            byte[] content = file.getContent();

            this.fileView.setText(new String(content));
            this.fileView.setCaretPosition(0);

            ((TitledBorder) this.viewPanel.getBorder()).setTitle(file.getName());
        } else {
            ((TitledBorder) this.viewPanel.getBorder()).setTitle("");
        }

        this.viewPanel.doLayout();
        this.viewPanel.repaint();
    }

    /**
     * Vergibt die Actions an die GUI-Elemente.
     */
    private void setupActions() {
        this.closeButton.setAction(new CloseAction(this));
        this.logoutButton.setAction(new LogoutAction(this));
        this.uploadButton.setAction(new UploadAction(this));
        this.deleteButton.setAction(new DeleteAction(this));
        //        this.newsessionButton.setAction(new NewSessionAction(this));
    }
}
