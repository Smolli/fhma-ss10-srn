package de.fhma.ss10.srn.tischbein.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

/**
 * Upload-Frame zum Hochladen einer Datei.
 * 
 * @author Smolli
 */
public final class UploadFrame extends UploadForm implements ActionListener {

    /**
     * Hilfslistener um an die Formulardaten ran zu kommen.
     * 
     * @author Smolli
     */
    public interface UploadFrameListener {

        /**
         * Gibt den aktuell eingeloggten Benutzer zurück.
         * 
         * @return Der {@link User}.
         */
        User getCurrentUser();

        /**
         * Wird aufgerufen, wenn sich an den Dateilisten etwas geändert hat.
         */
        void notifyChange();

    }

    /** Serial UID. */
    private static final long serialVersionUID = 544157435480035025L;

    /** Hält den Datenlistener. */
    private final UploadFrameListener listener;
    /** Hält den generierten Schlüssel. */
    private SecretKey key;
    /** Hält das ausgewählte, neue {@link FileItem}. */
    private transient FileItem item = null;

    /**
     * Ctor.
     * 
     * @param listenerObject
     *            Der Datenlistener.
     */
    public UploadFrame(final UploadFrameListener listenerObject) {
        this.searchButton.addActionListener(this);
        this.abbortButton.addActionListener(this);
        this.saveButton.addActionListener(this);

        this.listener = listenerObject;

        try {
            this.key = AesCrypto.generateKey();

            this.secretField.setText(Utils.toHexLine(this.key.getEncoded()));
        } catch (CryptoException e) {
            GuiUtils.displayError("Some strange behaviour occured!", e);
        }

        this.setVisible(true);
    }

    @Override
    public void actionPerformed(final ActionEvent button) {
        if (button.getSource() == this.searchButton) {
            this.searchButtonPressed();
        } else if (button.getSource() == this.saveButton) {
            this.saveButtonPressed();
        } else if (button.getSource() == this.abbortButton) {
            this.abbortButtonPressed();
        }
    }

    /**
     * Wird gerufen, wenn der 'Abbrechen'-Button gedrückt wurde.
     */
    private void abbortButtonPressed() {
        this.dispose();
    }

    /**
     * Wird audgerufen, wenn der 'Speichern'-Button gedrückt wurde.
     */
    private void saveButtonPressed() {
        try {
            if (this.item == null) {
                return;
            }

            this.item.encrypt();

            Database.getInstance().addFileItem(this.item);

            this.listener.notifyChange();

            this.dispose();
        } catch (Exception e) {
            GuiUtils.displayError("Kann Datei nicht hochladen!", e);
        }
    }

    /**
     * Wird aufgerufen, wenn der 'Durchsuchen...'-Button gedrückt wurde.
     */
    private void searchButtonPressed() {
        JFileChooser fc = new JFileChooser();

        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            // den Block nur ausführen, wenn der Benutzer auf 'Öffnen' geklickt hat
            String filename = fc.getSelectedFile().getPath();

            try {
                this.filenameField.setText(filename);

                this.item = FileItem.create(this.listener.getCurrentUser(), filename, this.key);

                this.hashField.setText(Utils.toHexLine(this.item.getHash()));
            } catch (Exception e) {
                GuiUtils.displayError("Kann Datei nicht auswählen!", e);

                this.filenameField.setText("");
                this.hashField.setText("");
                this.item = null;
            }
        }
    }

}
