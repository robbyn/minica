/*
    Minica, a very simple certificate authority
    Copyright (C) 2011  Maurice Perry <maurice@perry.ch>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.tastefuljava.minica;

import java.awt.Component;
import java.awt.SystemColor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;


public class KeyStoreEntryRenderer
        implements ListCellRenderer, TableCellRenderer {
    private static Icon KEY_ICON = new ImageIcon(
            KeyStoreEntryRenderer.class.getResource("/images/key-sn.png"));
    private static Icon CERT_ICON = new ImageIcon(
            KeyStoreEntryRenderer.class.getResource("/images/cert-sn.png"));

    private JLabel label = new JLabel();

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean hasFocus) {
        return getCellRenderer(list, value, isSelected, hasFocus);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        return getCellRenderer(table, value, isSelected, hasFocus);
    }

    private Component getCellRenderer(JComponent comp, Object value,
            boolean isSelected, boolean cellHasFocus) {
        if (value instanceof KeyStoreEntry) {
            KeyStoreEntry entry = (KeyStoreEntry)value;
            label.setText(entry.getAlias());
            label.setIcon(entry.isKey() ? KEY_ICON : CERT_ICON);
        } else {
            label.setText("");
            label.setIcon(null);
        }
        if (isSelected) {
            label.setBackground(SystemColor.activeCaption);
            label.setForeground(SystemColor.activeCaptionText);
        } else {
            label.setBackground(comp.getBackground());
            label.setForeground(comp.getForeground());
        }
        label.setEnabled(comp.isEnabled());
        label.setFont(comp.getFont());
        label.setOpaque(true);
        return label;
    }
}
