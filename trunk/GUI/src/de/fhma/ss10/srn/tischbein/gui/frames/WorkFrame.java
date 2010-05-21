package de.fhma.ss10.srn.tischbein.gui.frames;

import java.util.Vector;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.FileItem;
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
    private FileItem selectedFile = null;

    /**
     * Erstellt ein neues Arbeitsfenster mit dem übergebenen Benutzer.
     * 
     * @param newUser
     *            Der Benutzer.
     */
    public WorkFrame(final User newUser) {
        this.user = newUser;
        this.closeButton.setAction(new CloseAction(this));
        this.logoutButton.setAction(new LogoutAction(this));
        this.uploadButton.setAction(new UploadAction(newUser));
        this.deleteButton.setAction(new DeleteAction());

        this.myFilesLlist.setListData(this.user.getFileListObject().getFileList());

        this.setVisible(true);

        this.userTable.setModel(this.generateUserModel());
        //        this.otherFilesList.setListData(this.user.getFileListObject().getAccessList());

        this.myFilesLlist.setSelectionModel(new DefaultListSelectionModel() {
            {
                this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            }

            @Override
            public void setSelectionInterval(final int first, final int last) {
                super.setSelectionInterval(first, last);

                WorkFrame.this.selectedFile = WorkFrame.this.user.getFileListObject().getFileList().get(last);

                System.out.println(WorkFrame.this.selectedFile + " wurde ausgewählt");
            }

        });
    }

    @Override
    public void close() {
        Database.getInstance().shutdown();

        this.dispose();
    }

    @Override
    public void logout() {
        this.user.lock();

        new LoginFrame();

        this.close();
    }

    private TableModel generateUserModel() {

        return new TableModel() {
            Vector<User> users;

            {
                this.users = Database.getInstance().getUsers(WorkFrame.this.user);
            }

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
                User user = this.users.get(rowIndex);

                switch (columnIndex) {
                    case 0:
                        return WorkFrame.this.user.getFileListObject().hasAccess(user, WorkFrame.this.selectedFile);

                    case 1:
                        return user.getName();

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
            public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
                // TODO Auto-generated method stub

            }
        };
    }
}
