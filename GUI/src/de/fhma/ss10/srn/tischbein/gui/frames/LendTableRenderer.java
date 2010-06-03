package de.fhma.ss10.srn.tischbein.gui.frames;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.core.db.dbms.Database;

/**
 * Spezialisierter {@link TableCellRenderer}, der es ermöglicht, dass alle eingeloggten Benutzer in fetter Schrift
 * erscheinen.
 * 
 * @author Smolli
 */
public class LendTableRenderer implements TableCellRenderer {

    @Override
    public final Component getTableCellRendererComponent(final JTable table, final Object value,
            final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        Component result = null;

        switch (column) {
            case 0:
                result = this.createCheckbox((Boolean) value);
                break;

            case 1:
                result = this.createLabel((String) value);
                break;

            default:
                throw new RuntimeException("Aliens! Ich sehe Aliens!"); // NOPMD by smolli on 03.06.10 10:00
        }

        return result;
    }

    /**
     * Erstellt eine {@link JCheckBox}.
     * 
     * @param value
     *            Der Zustand der Checkbox.
     * @return Gibt die Generierte {@link JCheckBox} zurück.
     */
    private JCheckBox createCheckbox(final boolean value) {
        return new JCheckBox("", value);
    }

    /**
     * Erzeugt ein {@link JLabel}.
     * 
     * @param value
     *            Der Text des Labels.
     * @return Gibt das {@link JLabel} zurück.
     */
    private JLabel createLabel(final String value) {
        JLabel result = null;

        try {
            result = new JLabel(value);

            if (Database.getInstance().isUserLoggedIn(value)) {
                result.setFont(result.getFont().deriveFont(Font.BOLD));
            }
        } catch (final Exception e) {
            Logger.getLogger(LendTableRenderer.class).error("Kann die Zelle nicht rendern!", e);
        }

        return result;
    }
}
