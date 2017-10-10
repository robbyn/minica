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

import java.awt.Rectangle;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.bouncycastle.util.Arrays;

public class ChangePasswordDialog extends JDialog {
    private final KeyStore keystore;
    private final String alias;
    boolean done;

    public ChangePasswordDialog(JFrame parent, KeyStore keystore, 
            String alias) {
        super(parent, true);
        this.keystore = keystore;
        this.alias = alias;
        init();
    }

    private void init() {
        initComponents();
        getRootPane().setDefaultButton(ok);
        Rectangle rc = getParent().getBounds();
        int x = Math.max(rc.x + (rc.width-getWidth())/2, 0);
        int y = Math.max(rc.y + (rc.height-getHeight())/2, 0);
        setLocation(x, y);
        password.requestFocus();
    }

    public boolean doDialog() {
        setVisible(true);
        return done;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        newPassword = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        verification = new javax.swing.JPasswordField();
        bottomPanel = new javax.swing.JPanel();
        ok = new javax.swing.JButton();
        cancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Please enter the password");
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        getContentPane().add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        getContentPane().add(password, gridBagConstraints);

        jLabel2.setText("New password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        getContentPane().add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        getContentPane().add(newPassword, gridBagConstraints);

        jLabel3.setText("Verification:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        getContentPane().add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        getContentPane().add(verification, gridBagConstraints);

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
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(17, 12, 11, 11);
        getContentPane().add(bottomPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        done = false;
        dispose();
    }//GEN-LAST:event_cancelActionPerformed

    private void okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okActionPerformed
        try {
            char[] pwd = password.getPassword();
            char[] newPwd = newPassword.getPassword();
            if (!Arrays.areEqual(newPwd, verification.getPassword())) {
                JOptionPane.showMessageDialog(this, "Passwords do not match",
                        "Error", JOptionPane.ERROR_MESSAGE);
                newPassword.requestFocus();
                return;                
            }
            Key privateKey = keystore.getKey(alias, pwd);
            Certificate[] certs
                    = keystore.getCertificateChain(alias);
            keystore.setKeyEntry(alias, privateKey, newPwd, certs);
            done = true;
            dispose();
        } catch (KeyStoreException | NoSuchAlgorithmException
                | UnrecoverableKeyException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Could change password", JOptionPane.ERROR_MESSAGE);
            password.requestFocus();
        }
    }//GEN-LAST:event_okActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton cancel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPasswordField newPassword;
    private javax.swing.JButton ok;
    private javax.swing.JPasswordField password;
    private javax.swing.JPasswordField verification;
    // End of variables declaration//GEN-END:variables
}
