package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.frames.UploadFrame;

/**
 * Upload-Action.
 * 
 * @author Smolli
 */
public final class UploadAction extends AbstractAction {

	/** Serial UID. */
	private static final long serialVersionUID = 7077305932498378892L;

	@Override
	/*
	 * Neues UploadFrame erstellen
	 */
	public void actionPerformed(final ActionEvent arg0) {
		UploadFrame frame = new UploadFrame();

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
