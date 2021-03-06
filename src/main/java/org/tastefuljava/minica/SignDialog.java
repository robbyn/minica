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
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.bouncycastle.operator.OperatorCreationException;

public class SignDialog extends JDialog {
    private final DateFormat dateFormat
            = new SimpleDateFormat("dd-MM-yyyy");
    private final KeyStore keystore;
    private boolean done;

    public SignDialog(Frame parent, KeyStore keystore, KeyStoreEntry current)
            throws KeyStoreException {
        super(parent, true);
        this.keystore = keystore;
        initComponents();
        init(keystore, current, parent);
    }

    private void init(KeyStore keystore1, KeyStoreEntry current, Frame parent) throws KeyStoreException {
        Util.clearWidthAll(this, JTextField.class);
        Util.clearWidthAll(this, JPasswordField.class);
        int textHeight = startDate.getPreferredSize().height;
        Util.adjustHeight(subjectPassword, textHeight);
        Util.adjustHeight(signerPassword, textHeight);
        // fill subject and signer comboboxes
        KeyStoreEntry[] entries = KeyStoreEntry.getAll(keystore1);
        Arrays.sort(entries, KeyStoreEntry.TYPE_ALIAS_ORDER);
        subject.setRenderer(new KeyStoreEntryRenderer());
        signer.setRenderer(new KeyStoreEntryRenderer());
        subject.removeAllItems();
        signer.removeAllItems();
        for (int i = 0; i < entries.length; ++i) {
            KeyStoreEntry entry = entries[i];
            subject.addItem(entry);
            if (entry.isKey()) {
                signer.addItem(entry);
            }
        }
        if (current != null) {
            subject.setSelectedItem(current);
        }
        Calendar cal = Calendar.getInstance();
        startDate.setText(dateFormat.format(cal.getTime()));
        cal.add(Calendar.YEAR, 1);
        endDate.setText(dateFormat.format(cal.getTime()));
        getRootPane().setDefaultButton(ok);
        pack();
        Rectangle rc = parent.getBounds();
        int x = Math.max(rc.x + (rc.width-getWidth())/2, 0);
        int y = Math.max(rc.y + (rc.height-getHeight())/2, 0);
        setLocation(x, y);
    }

    public boolean doDialog() {
        setVisible(true);
        return done;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        subject = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        subjectPassword = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        signer = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        signerPassword = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();
        algorithm = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        startDate = new javax.swing.JTextField();
        endDate = new javax.swing.JTextField();
        bottomPanel = new javax.swing.JPanel();
        ok = new javax.swing.JButton();
        cancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Sign a certificate");
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Subject:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        getContentPane().add(jLabel1, gridBagConstraints);

        subject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subjectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        getContentPane().add(subject, gridBagConstraints);

        jLabel3.setText("Subject password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        getContentPane().add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        getContentPane().add(subjectPassword, gridBagConstraints);

        jLabel2.setText("Signer:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        getContentPane().add(jLabel2, gridBagConstraints);

        signer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        getContentPane().add(signer, gridBagConstraints);

        jLabel4.setText("Signer password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        getContentPane().add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        getContentPane().add(signerPassword, gridBagConstraints);

        jLabel5.setText("Signature algorithm:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        getContentPane().add(jLabel5, gridBagConstraints);

        algorithm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algorithmActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        getContentPane().add(algorithm, gridBagConstraints);

        jLabel12.setText("Start/end date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        getContentPane().add(jLabel12, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        getContentPane().add(startDate, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        getContentPane().add(endDate, gridBagConstraints);

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
        KeyStoreEntry subj = (KeyStoreEntry)subject.getSelectedItem();
        char subjpwd[] = null;
        PrivateKey subjkey = null;
        X509Certificate cert;
        try {
            if (subj.isKey()) {
                subjpwd = subjectPassword.getPassword();
                subjkey = (PrivateKey)keystore.getKey(subj.getAlias(), subjpwd);
            }
            cert = (X509Certificate)keystore.getCertificate(
                    subj.getAlias());
        } catch (GeneralSecurityException e) {
            JOptionPane.showMessageDialog(this, "Could not retrieve the "
                    + "subject's key. Please check the subject password",
                    "Error", JOptionPane.ERROR_MESSAGE);
            subjectPassword.requestFocus();
            return;
        }

        X509CertificateBuilder gen = new X509CertificateBuilder(cert);

        try {
            gen.setStart(dateFormat.parse(startDate.getText()));
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid start date",
                    "Error", JOptionPane.ERROR_MESSAGE);
            startDate.requestFocus();
            return;
        }

        try {
            gen.setEnd(dateFormat.parse(endDate.getText()));
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid end date",
                    "Error", JOptionPane.ERROR_MESSAGE);
            endDate.requestFocus();
            return;
        }

        KeyStoreEntry sgnr = (KeyStoreEntry)signer.getSelectedItem();
        char sgnrpwd[] = signerPassword.getPassword();
        SignatureAlgorithm sigalg = (SignatureAlgorithm)algorithm.getSelectedItem();

        Certificate sgnrchain[];
        PrivateKey sgnrkey;
        try {
            sgnrchain = keystore.getCertificateChain(sgnr.getAlias());
            sgnrkey = (PrivateKey)keystore.getKey(sgnr.getAlias(), sgnrpwd);
         } catch (GeneralSecurityException e) {
            JOptionPane.showMessageDialog(this, "Keystore error", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        X509Certificate ic = (X509Certificate)sgnrchain[0];
        gen.setSignatureAlgorithm(sigalg.name());
        gen.setIssuer(ic.getSubjectX500Principal(), sgnrkey);

        try {
            cert = gen.build();
        } catch (IOException | InvalidAlgorithmParameterException
                | NoSuchAlgorithmException | NoSuchProviderException
                | CertificateException | OperatorCreationException e) {
            JOptionPane.showMessageDialog(this, "Signature error",
                    e.getMessage(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (!subj.isKey()) {
                keystore.setCertificateEntry(subj.getAlias(), cert);
            } else {
                Certificate certs[] = new Certificate[sgnrchain.length+1];
                for (int i = 0; i < sgnrchain.length; ++i) {
                    certs[i+1] = sgnrchain[i];
                }
                certs[0] = cert;
                keystore.setKeyEntry(subj.getAlias(), subjkey, subjpwd, certs);
            }
        } catch (KeyStoreException e) {
            JOptionPane.showMessageDialog(this, "Keystore error", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        done = true;
        dispose();
    }//GEN-LAST:event_okActionPerformed

    private void signerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signerActionPerformed
        KeyStoreEntry entry = (KeyStoreEntry)signer.getSelectedItem();
        if (entry != null) {
            try {
                Certificate cert = keystore.getCertificate(entry.getAlias());
                String crypto = cert.getPublicKey().getAlgorithm();
                SignatureAlgorithm algs[] = SignatureAlgorithm.getForCrypto(crypto);
                algorithm.removeAllItems();
                for (int i = 0; i < algs.length; ++i) {
                    algorithm.addItem(algs[i]);
                }
            } catch (KeyStoreException e) {
                JOptionPane.showMessageDialog(this,
                        "Error while reading the certificate", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_signerActionPerformed

    private void subjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subjectActionPerformed
        KeyStoreEntry entry = (KeyStoreEntry)subject.getSelectedItem();
        subjectPassword.setEditable(entry != null && entry.isKey());
    }//GEN-LAST:event_subjectActionPerformed

    private void algorithmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algorithmActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_algorithmActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algorithm;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton cancel;
    private javax.swing.JTextField endDate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JButton ok;
    private javax.swing.JComboBox signer;
    private javax.swing.JPasswordField signerPassword;
    private javax.swing.JTextField startDate;
    private javax.swing.JComboBox subject;
    private javax.swing.JPasswordField subjectPassword;
    // End of variables declaration//GEN-END:variables
    
}
