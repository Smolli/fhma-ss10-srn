package de.fhma.ss10.srn.tischbein.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.actions.SearchAction;
import de.fhma.ss10.srn.tischbein.gui.forms.UploadForm;

public class UploadFrame extends UploadForm implements ActionListener {

	private User user;

	public UploadFrame() {
		this.searchButton.addActionListener(this);
		this.abbortButton.addActionListener(this);
		this.saveButton.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent button) {
		if (button.getSource() == searchButton) {

		} else if (button.getSource() == saveButton) {

		} else if (button.getSource() == abbortButton) {

		}
	}

}
