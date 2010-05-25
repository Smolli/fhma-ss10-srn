package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.fhma.ss10.srn.tischbein.core.db.User;
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

    //User der aktuellen Session
    private transient final User user;
    private final UploadFrameListener listener;

    //Neues UploadActionObjekt mit dem aktuellen Benutzer
    public UploadAction(final UploadFrameListener listener, final User newUser) {
        this.user = newUser;
        this.listener = listener;
    }

    @Override
    /*
     * Neues UploadFrame erstellen
     */
    public void actionPerformed(final ActionEvent arg0) {
        UploadFrame frame = new UploadFrame(this.listener, this.user);
        frame.setVisible(true);
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
