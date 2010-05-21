package de.fhma.ss10.srn.tischbein.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.forms.UploadForm;

public class UploadFrame extends UploadForm implements ActionListener {

	// User Objekt ist nötig zum Speichern der Dateien in der DB
	User user;

	public UploadFrame(User user) {
		this.user = user;
		this.searchButton.addActionListener(this);
		this.abbortButton.addActionListener(this);
		this.saveButton.addActionListener(this);

	}

	@Override
	public void actionPerformed(ActionEvent button) {
		/*
		 * Beim Klicken auf den searchbutton öffnet sich ein neues Filechooser
		 * fenster in dem die Datei ausgewählt werden kann (nur Textdateien)
		 */
		if (button.getSource() == searchButton) {

			// Create a file chooser
			final JFileChooser fc = new JFileChooser();
			// In response to a button click:
			fc.showOpenDialog(searchButton);

			// Datei Objekt "laden"
			File file = fc.getSelectedFile();
			// Namensfeld auf Pfad der Datei setzen
			this.filenameField.setText(file.getPath());

		} else if (button.getSource() == saveButton) {

			// Läd den Pfad aus dem filenameField
			File file = new File(this.filenameField.getText());

		} else if (button.getSource() == abbortButton) {

		}
	}

}
