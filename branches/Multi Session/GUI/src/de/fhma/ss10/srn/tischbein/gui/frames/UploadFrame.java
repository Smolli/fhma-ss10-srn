package de.fhma.ss10.srn.tischbein.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.crypto.SecretKey;
import javax.swing.JFileChooser;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.crypto.AesCrypto;
import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.db.dbms.Database;
import de.fhma.ss10.srn.tischbein.core.db.fileitem.FileItem;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;
import de.fhma.ss10.srn.tischbein.gui.forms.UploadForm;

/**
 * Upload-Frame zum Hochladen einer Datei.
 * 
 * @author Smolli
 */
public final class UploadFrame extends UploadForm implements ActionListener {

    /** Serial UID. */
    private static final long serialVersionUID = 544157435480035025L;

    /** Hält den Datenlistener. */
    private final WorkFrameBaseParent parent;
    /** Hält den generierten Schlüssel. */
    private SecretKey key;
    /** Hält das ausgewählte, neue {@link FileItem}. */
    private transient FileItem item = null;

    /**
     * Ctor.
     * 
     * @param parentObject
     *            Der Datenlistener.
     */
    public UploadFrame(final WorkFrameBaseParent parentObject) {
        super();

        this.searchButton.addActionListener(this);
        this.abbortButton.addActionListener(this);
        this.saveButton.addActionListener(this);

        this.parent = parentObject;

        try {
            this.key = AesCrypto.generateKey();

            this.secretField.setText(Utils.toHexLine(this.key.getEncoded()));
        } catch (final CryptoException e) {
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

            this.dispose();
        } catch (final Exception e) {
            GuiUtils.displayError("Kann Datei nicht hochladen!", e);
        }
    }

    /**
     * Wird aufgerufen, wenn der 'Durchsuchen...'-Button gedrückt wurde.
     */
    private void searchButtonPressed() {
        final JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            // den Block nur ausführen, wenn der Benutzer auf 'Öffnen' geklickt hat
            final String filename = chooser.getSelectedFile().getPath();

            try {
                this.filenameField.setText(filename);

                this.item = FileItem.create(this.parent.getCurrentUser(), filename, this.key);

                this.hashField.setText(Utils.toHexLine(this.item.getHash()));
            } catch (final Exception e) {
                GuiUtils.displayError("Kann Datei nicht auswählen!", e);

                this.filenameField.setText("");
                this.hashField.setText("");
                this.item = null; // NOPMD by smolli on 30.05.10 22:03
            }
        }
    }

}
