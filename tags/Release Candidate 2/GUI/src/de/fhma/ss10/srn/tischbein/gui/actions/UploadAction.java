package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.fhma.ss10.srn.tischbein.gui.frames.UploadFrame;
import de.fhma.ss10.srn.tischbein.gui.frames.WorkFrameBaseParent;

/**
 * Upload-Action.
 * 
 * @author Smolli
 */
public final class UploadAction extends AbstractAction {

    /** Serial UID. */
    private static final long serialVersionUID = 7077305932498378892L;
    /** Hält den Daten-Listener. */
    private final WorkFrameBaseParent parent;

    /**
     * Standard-Ctor. Erstellt die Upload-Action und speichert den Daten-Listener.
     * 
     * @param parentObject
     *            Der Datenlistener.
     */
    public UploadAction(final WorkFrameBaseParent parentObject) {
        super();

        this.parent = parentObject;
    }

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        new UploadFrame(this.parent);
    }

    @Override
    public Object getValue(final String key) {
        Object result;

        if (key.equals(Action.NAME)) {
            result = "Hochladen";
        } else {
            result = super.getValue(key);
        }

        return result;
    }

}
