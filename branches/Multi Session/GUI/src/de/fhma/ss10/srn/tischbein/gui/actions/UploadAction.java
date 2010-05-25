package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.fhma.ss10.srn.tischbein.gui.frames.UploadFrame;
import de.fhma.ss10.srn.tischbein.gui.frames.UploadFrame.UploadFrameListener;

/**
 * Upload-Action.
 * 
 * @author Smolli
 */
public final class UploadAction extends AbstractAction {

    /** Serial UID. */
    private static final long serialVersionUID = 7077305932498378892L;
    /** Hält den Daten-Listener. */
    private final UploadFrameListener listener;

    /**
     * Standard-Ctor. Erstellt die Upload-Action und speichert den Daten-Listener.
     * 
     * @param listenerObject
     *            Der Datenlistener.
     */
    public UploadAction(final UploadFrameListener listenerObject) {
        this.listener = listenerObject;
    }

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        new UploadFrame(this.listener);
    }

    @Override
    public Object getValue(final String key) {
        if (key.equals(Action.NAME)) {
            return "Hochladen";
        } else {
            return super.getValue(key);
        }
    }

}
