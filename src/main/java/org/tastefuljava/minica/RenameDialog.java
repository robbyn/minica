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

import java.awt.Frame;
import java.awt.Rectangle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class RenameDialog extends javax.swing.JDialog {
    private final String oldName;
    private boolean done;

    public RenameDialog(Frame parent, String name) {
        super(parent, true);
        this.oldName = name;
        initComponents();
        init(name);
    }

    public static String doDialog(Frame parent, String name) {
        RenameDialog dlg = new RenameDialog(parent, name);
        return dlg.doDialog();
    }

    public String doDialog() {
        setVisible(true);
        return done ? newName.getText() : null;
    }

    private void init(String name) {
        newName.setText(name);
        newName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                newNameChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                newNameChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                newNameChanged();
            }
        });
        getRootPane().setDefaultButton(ok);
        Rectangle rc = getParent().getBounds();
        int x = Math.max(rc.x + (rc.width-getWidth())/2, 0);
        int y = Math.max(rc.y + (rc.height-getHeight())/2, 0);
        setLocation(x, y);
        ok.setEnabled(false);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        newName = new javax.swing.JTextField();
        bottomPanel = new javax.swing.JPanel();
        ok = new javax.swing.JButton();
        cancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("New name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        getContentPane().add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription("");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        getContentPane().add(newName, gridBagConstraints);

        bottomPanel.setLayout(new java.awt.GridBagLayout());

        ok.setText("OK");
        ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        bottomPanel.add(ok, gridBagConstraints);

        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });
        bottomPanel.add(cancel, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(17, 12, 11, 11);
        getContentPane().add(bottomPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okActionPerformed
        done = true;
        dispose();
    }//GEN-LAST:event_okActionPerformed

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        done = false;
        dispose();
    }//GEN-LAST:event_cancelActionPerformed

    private void newNameChanged() {
        String name = newName.getText();
        ok.setEnabled(name != null && name.length() > 0
                && !name.equals(oldName));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton cancel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField newName;
    private javax.swing.JButton ok;
    // End of variables declaration//GEN-END:variables

    private void setStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
