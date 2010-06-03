package de.fhma.ss10.srn.tischbein.gui.frames;

import java.text.MessageFormat;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.core.db.fileitem.FileItem;

/**
 * Spezialisiertes {@link ListSelectionModel} für die Datei-Liste.
 * 
 * @author Smolli
 */
final class FilesSelectionModel extends DefaultListSelectionModel {

    /** Serial UID. */
    private static final long serialVersionUID = 9031173846551914083L;
    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(FilesSelectionModel.class);
    /** Hält die verknüpfte JList. */
    private final transient JList list;
    /** Hält das ELtern-Frame. */
    private final transient WorkFrameBaseParent parent;

    /**
     * Standard-Ctor. Verknüpft das Model mit einer {@link JList}.
     * 
     * @param parentForm
     *            Das Eltern-Frame.
     * @param guiList
     *            Die {@link JList}, mit dem das Model verknüft ist.
     */
    public FilesSelectionModel(final WorkFrameBaseParent parentForm, final JList guiList) {
        super();

        this.list = guiList;
        this.parent = parentForm;
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    public void setSelectionInterval(final int first, final int last) {
        super.setSelectionInterval(first, last);

        FilesSelectionModel.LOG.trace(MessageFormat.format("{0}, {1}", first, last));

        this.parent.selectFile((FileItem) this.list.getModel().getElementAt(last), this.list, last);
    }
}
