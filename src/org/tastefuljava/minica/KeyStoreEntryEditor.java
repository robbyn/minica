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
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;


public class KeyStoreEntryEditor extends AbstractCellEditor 
        implements TableCellEditor {
    private JTextField editor = new JTextField();
    private KeyStoreEntry entry;

    public KeyStoreEntryEditor() {
    }

    public Object getCellEditorValue() {
        return entry == null ? null : entry.copy(editor.getText());
    }

    public Component getTableCellEditorComponent(JTable table, Object value, 
            boolean isSelected, int row, int column) {
        entry = (KeyStoreEntry)value;
        editor.setText(entry == null ? "" : entry.getAlias());
        return editor;
    }
}
