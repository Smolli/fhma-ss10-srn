package de.fhma.ss10.srn.tischbein.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.crypto.SecretKey;
import javax.swing.JFileChooser;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.crypto.AesCrypto;
import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;
import de.fhma.ss10.srn.tischbein.gui.forms.UploadForm;

public class UploadFrame extends UploadForm implements ActionListener {

    public interface UploadFrameListener {

        void notifyChange();

    }

    // User Objekt ist n�tig zum Speichern der Dateien in der DB
    User user;
    private SecretKey key;
    private FileItem item;
    private final UploadFrameListener listener;

    public UploadFrame(final UploadFrameListener listener, final User user) {
        this.user = user;

        this.searchButton.addActionListener(this);
        this.abbortButton.addActionListener(this);
        this.saveButton.addActionListener(this);

        this.listener = listener;

        try {
            this.key = AesCrypto.generateKey();

            this.secretField.setText(Utils.toHexLine(this.key.getEncoded()));
        } catch (CryptoException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(final ActionEvent button) {
        /*
         * Beim Klicken auf den searchbutton �ffnet sich ein neues Filechooser fenster in dem die Datei ausgew�hlt
         * werden kann (nur Textdateien)
         */
        if (button.getSource() == this.searchButton) {
            // Create a file chooser
            final JFileChooser fc = new JFileChooser();

            // In response to a button click:
            fc.showOpenDialog(this.searchButton);

            // Datei Objekt "laden"
            File file = fc.getSelectedFile();

            // Namensfeld auf Pfad der Datei setzen
            String filename = file.getPath();

            try {
                this.filenameField.setText(filename);

                this.item = FileItem.create(this.user, filename, this.key);

                this.hashField.setText(Utils.toHexLine(this.item.getHash()));
            } catch (Exception e) {
                GuiUtils.displayError("Kann Datei nicht auswählen!", e);

                this.filenameField.setText("");
                this.hashField.setText("");
            }

        } else if (button.getSource() == this.saveButton) {
            // L�d den Pfad aus dem filenameField
            File file = new File(this.filenameField.getText());

            // 1. Wir schnappen uns den aktuell eingeloggten user
            // 2. Danach h�ngen wir die zuvor ausgew�hlte Datei dem User an
            try {
                System.out.print("User: " + this.user.getName() + " --> ");
                System.out.print("File: " + file.getPath());
                //                Database.getInstance().getUser(this.user.getName()).addFile(file.getPath());

                this.item.encrypt();

                Database.getInstance().addFileItem(this.item);

                // Hier m�ssen wir auf die Dateien zugreifen !!!
                //				System.out.println(Database.getInstance().getUser(user.getName()).getFileListObject());

                this.listener.notifyChange();

                this.dispose();
            } catch (Exception e) {
                GuiUtils.displayError("Kann Datei nicht hochladen!", e);
            }

        } else if (button.getSource() == this.abbortButton) {
            // dispose = l�scht alle Elemente der UploadForm
            // und gibt den belegten Speicher wieder frei.
            // Fenster baut sich wieder ab.
            this.dispose();
        }
    }

}
