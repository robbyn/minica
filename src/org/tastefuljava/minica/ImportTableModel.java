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

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;


public class ImportTableModel extends AbstractTableModel {
    private final List<KeyStoreEntry> entries = new ArrayList<>();

    public ImportTableModel() {
    }

    public void clear() {
        entries.clear();
    }

    public KeyStoreEntry getEntry(int ix) {
        return entries.get(ix);
    }

    public void add(KeyStoreEntry entry) {
        int ix = getRowCount();
        entries.add(entry);
        fireTableRowsInserted(ix, ix);
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return rowIndex >= 0 && rowIndex < getRowCount() && columnIndex == 0;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return KeyStoreEntry.class;
            default:
                return null;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return entries.get(rowIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < getRowCount()
                && aValue instanceof KeyStoreEntry) {
            entries.set(rowIndex, (KeyStoreEntry)aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
