package de.fhma.ss10.srn.tischbein.gui.frames;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.fhma.ss10.srn.tischbein.core.db.dbms.Database;
import de.fhma.ss10.srn.tischbein.core.db.user.User;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;

/**
 * Spezialisiertes {@link TableModel} für die Rechtevergabe der Dateien.
 * 
 * @author Smolli
 */
final class AccessTableModel implements TableModel {

    // DONE: eingeloggte Benutzer sollen mit fetter Schrift dargestellt werden.

    //    /**
    //     * Eltern-Interface.
    //     */
    //    public interface AccessTableModelParent {
    //
    //        /**
    //         * Gibt den aktuell eingeloggten Benutzer zurück.
    //         * 
    //         * @return Den Benutzer.
    //         */
    //        User getCurrentUser();
    //
    //        /**
    //         * Gibt die aktuell ausgewählte Datei zurück.
    //         * 
    //         * @return Die Datei.
    //         */
    //        FileItem getSelectedFile();
    //
    //    }

    /** Hält die Liste der angezeigten Benutzer. */
    private List<User> users;
    /** Hält das Eltern-Frame. */
    private final WorkFrameBaseParent parent;
    /** Hält alle Model Listener. */
    private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();

    /**
     * Standard-Ctor.
     * 
     * @param parentFrame
     *            Das Frame, mit dem das Model verbunden ist.
     */
    public AccessTableModel(final WorkFrameBaseParent parentFrame) {
        this.parent = parentFrame;

        this.updateData();
    }

    @Override
    public void addTableModelListener(final TableModelListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        Class<?> result = null;

        switch (columnIndex) {
            case 0:
                result = Boolean.class;
                break;

            case 1:
                result = String.class;
                break;

            default:
                break;
        }

        return result;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        String result = null;

        switch (columnIndex) {
            case 0:
                result = "Erlaubnis";
                break;

            case 1:
                result = "Benutzername";
                break;

            default:
                break;
        }

        return result;
    }

    @Override
    public int getRowCount() {
        return this.users.size();
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final User selectedUser = this.users.get(rowIndex);
        Object result = null;

        switch (columnIndex) {
            case 0:
                result = this.parent.getCurrentUser().getDescriptor().hasAccess(selectedUser,
                        this.parent.getSelectedFile());
                break;

            case 1:
                result = selectedUser.getName();
                break;

            default:
                break;
        }

        return result;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        boolean result;

        switch (columnIndex) {
            case 0:
                result = true;
                break;

            case 1:
                result = false;
                break;

            default:
                result = false;
                break;
        }

        return result;
    }

    @Override
    public void removeTableModelListener(final TableModelListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
        if (columnIndex != 0) {
            return;
        }

        final User selectedUser = this.users.get(rowIndex);

        try {
            this.parent.getCurrentUser().getDescriptor().setAccess(selectedUser, this.parent.getSelectedFile(),
                    (Boolean) value);
        } catch (final Exception e) {
            GuiUtils.displayError("Konnte Recht nicht speichern!", e);
        }
    }

    /**
     * Aktualisiert die Daten der Tabelle.
     */
    public void updateData() {
        this.users = Database.getInstance().getUsers(this.parent.getCurrentUser());

        this.fireChangeEvent();
    }

    /**
     * Wird gerufen, wenn sich etwas an den Daten der Tabelle geändert hat.
     */
    private void fireChangeEvent() {
        final TableModelEvent event = new TableModelEvent(this);

        for (final TableModelListener listener : this.listeners) {
            listener.tableChanged(event);
        }
    }

}
