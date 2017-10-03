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

import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import javax.security.auth.x500.X500Principal;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.operator.OperatorCreationException;

public class GenerateKeyDialog extends JDialog {
    private final DateFormat dateFormat
            = new SimpleDateFormat("dd-MM-yyyy");
    private boolean done;
    private PrivateKey privateKey;
    private Certificate[] chain;
    private final KeyStore keystore;

    public GenerateKeyDialog(Frame parent, KeyStore keystore)
            throws KeyStoreException {
        super(parent, true);
        this.keystore = keystore;
        initComponents();
        algorithm.removeAllItems();
        SignatureAlgorithm algs[] = SignatureAlgorithm.values();
        for (int i = 0; i < algs.length; ++i) {
            algorithm.addItem(algs[i]);
        }
        algorithm.setSelectedItem(SignatureAlgorithm.SHA256withRSA);
        signer.setRenderer(new KeyStoreEntryRenderer());
        signer.removeAllItems();
        KeyStoreEntry entries[] = KeyStoreEntry.getAll(keystore);
        Arrays.sort(entries, KeyStoreEntry.TYPE_ALIAS_ORDER);
        for (int i = 0; i < entries.length; ++i) {
            KeyStoreEntry entry = entries[i];
            if (entry.isKey()) {
                Certificate[] certs = keystore.getCertificateChain(
                        entry.getAlias());
                X509Certificate ic = (X509Certificate)certs[0];
                if (ic.getBasicConstraints() >= 0) {
                    signer.addItem(entry);
                }
            }
        }
        if (signer.getItemCount() == 0) {
            signedBy.setEnabled(false);
        }
        Util.clearWidthAll(getContentPane(), JTextField.class);
        Util.clearWidthAll(getContentPane(), JPasswordField.class);
        int textHeight = alias.getPreferredSize().height;
        Util.adjustHeight(password, textHeight);
        Util.adjustHeight(verification, textHeight);
        Calendar cal = Calendar.getInstance();
        startDate.setText(dateFormat.format(cal.getTime()));
        cal.add(Calendar.YEAR, 1);
        endDate.setText(dateFormat.format(cal.getTime()));
        getRootPane().setDefaultButton(ok);
        ec.removeAllItems();
        Set<String> curves = new TreeSet<>();
        for (Enumeration<String> enm = ECNamedCurveTable.getNames();
                enm.hasMoreElements(); ) {
            curves.add(enm.nextElement());
        }
        for (String curve: curves) {
            ec.addItem(curve);
        }
        pack();
        Rectangle rc = parent.getBounds();
        int x = Math.max(rc.x + (rc.width-getWidth())/2, 0);
        int y = Math.max(rc.y + (rc.height-getHeight())/2, 0);
        setLocation(x, y);
        setSignerState();
    }

    public boolean showDialog() {
        setVisible(true);
        return done;
    }

    public String getAlias() {
        return alias.getText();
    }

    public char[] getPassword() {
        return password.getPassword();
    }

    public PrivateKey getKey() {
        return privateKey;
    }

    public Certificate[] getChain() {
        return chain;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        signerGroup = new javax.swing.ButtonGroup();
        leftPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        alias = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        verification = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();
        serialNumber = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        algorithm = new javax.swing.JComboBox();
        algorithmParams = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        keySize = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        ec = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        startDate = new javax.swing.JTextField();
        endDate = new javax.swing.JTextField();
        autoSigned = new javax.swing.JRadioButton();
        signedBy = new javax.swing.JRadioButton();
        signer = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        signerPassword = new javax.swing.JPasswordField();
        jLabel15 = new javax.swing.JLabel();
        signatureAlg = new javax.swing.JComboBox();
        rightPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        commonName = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        organisationUnit = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        organisationUnit2 = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        organisationUnit3 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        organisation = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        locality = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        state = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        country = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        ca = new javax.swing.JCheckBox();
        bottomPanel = new javax.swing.JPanel();
        ok = new javax.swing.JButton();
        cancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Generate key...");
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        leftPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Alias:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        leftPanel.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        leftPanel.add(alias, gridBagConstraints);

        jLabel2.setText("Password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        leftPanel.add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        leftPanel.add(password, gridBagConstraints);

        jLabel3.setText("Verification:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        leftPanel.add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        leftPanel.add(verification, gridBagConstraints);

        jLabel5.setText("Serial number:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        leftPanel.add(jLabel5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        leftPanel.add(serialNumber, gridBagConstraints);

        jLabel4.setText("Algorithm:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        leftPanel.add(jLabel4, gridBagConstraints);

        algorithm.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "RSA", "DSA" }));
        algorithm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algorithmActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        leftPanel.add(algorithm, gridBagConstraints);

        algorithmParams.setLayout(new java.awt.CardLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel18.setText("Key size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        jPanel2.add(jLabel18, gridBagConstraints);

        keySize.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        keySize.setText("2048");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 40;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(keySize, gridBagConstraints);

        algorithmParams.add(jPanel2, "default");

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel19.setText("Elliptic curve:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        jPanel1.add(jLabel19, gridBagConstraints);

        ec.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        ec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ecActionPerformed(evt);
            }
        });
        jPanel1.add(ec, new java.awt.GridBagConstraints());

        algorithmParams.add(jPanel1, "EC");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 11, 11);
        leftPanel.add(algorithmParams, gridBagConstraints);

        jLabel12.setText("Start/end date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        leftPanel.add(jLabel12, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        leftPanel.add(startDate, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        leftPanel.add(endDate, gridBagConstraints);

        signerGroup.add(autoSigned);
        autoSigned.setSelected(true);
        autoSigned.setText("Autosigned");
        autoSigned.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoSignedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        leftPanel.add(autoSigned, gridBagConstraints);

        signerGroup.add(signedBy);
        signedBy.setText("Signed by:");
        signedBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signedByActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 11);
        leftPanel.add(signedBy, gridBagConstraints);

        signer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        leftPanel.add(signer, gridBagConstraints);

        jLabel14.setText("Password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        leftPanel.add(jLabel14, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        leftPanel.add(signerPassword, gridBagConstraints);

        jLabel15.setText("Signature algorithme:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        leftPanel.add(jLabel15, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        leftPanel.add(signatureAlg, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        getContentPane().add(leftPanel, gridBagConstraints);

        rightPanel.setLayout(new java.awt.GridBagLayout());

        jLabel6.setText("Common name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        rightPanel.add(jLabel6, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        rightPanel.add(commonName, gridBagConstraints);

        jLabel7.setText("Organizational unit:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        rightPanel.add(jLabel7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        rightPanel.add(organisationUnit, gridBagConstraints);

        jLabel13.setText("Organizational unit 2:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        rightPanel.add(jLabel13, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        rightPanel.add(organisationUnit2, gridBagConstraints);

        jLabel17.setText("Organizational unit 3:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        rightPanel.add(jLabel17, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        rightPanel.add(organisationUnit3, gridBagConstraints);

        jLabel8.setText("Organization:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        rightPanel.add(jLabel8, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        rightPanel.add(organisation, gridBagConstraints);

        jLabel9.setText("Locality:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        rightPanel.add(jLabel9, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        rightPanel.add(locality, gridBagConstraints);

        jLabel10.setText("State:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        rightPanel.add(jLabel10, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        rightPanel.add(state, gridBagConstraints);

        jLabel11.setText("Country:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        rightPanel.add(jLabel11, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        rightPanel.add(country, gridBagConstraints);

        jLabel16.setText("CA");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 11, 11);
        rightPanel.add(jLabel16, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 11, 11);
        rightPanel.add(ca, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        getContentPane().add(rightPanel, gridBagConstraints);

        bottomPanel.setLayout(new java.awt.GridBagLayout());

        ok.setText("OK");
        ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(17, 12, 11, 5);
        bottomPanel.add(ok, gridBagConstraints);

        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 11, 11);
        bottomPanel.add(cancel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(bottomPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        done = false;
        dispose();
    }//GEN-LAST:event_cancelActionPerformed

    private void okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okActionPerformed
        if (alias.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "The alias is missing",
                    "Error", JOptionPane.ERROR_MESSAGE);
            alias.requestFocus();
            return;
        }
        String pwd = new String(password.getPassword());
        String ver = new String(verification.getPassword());
        if (!pwd.equals(ver)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match",
                    "Error", JOptionPane.ERROR_MESSAGE);
            password.requestFocus();
            return;
        }
        X500PrincipalBuilder nb = new X500PrincipalBuilder();
        addField("C", country, nb);
        addField("ST", state, nb);
        addField("L", locality, nb);
        addField("O", organisation, nb);
        addField("OU", organisationUnit3, nb);
        addField("OU", organisationUnit2, nb);
        addField("OU", organisationUnit, nb);
        addField("CN", commonName, nb);
        X500Principal principal = nb.build();
        System.out.println("principal: " + principal);
        BigInteger sn;
        try {
            sn = new BigInteger(serialNumber.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid serial number",
                    "Error", JOptionPane.ERROR_MESSAGE);
            serialNumber.requestFocus();
            return;
        }

        X509CertificateBuilder gen = new X509CertificateBuilder(sn, principal);
        SignatureAlgorithm alg = (SignatureAlgorithm)algorithm.getSelectedItem();
        int ksize;
        try {
            ksize = Integer.parseInt(keySize.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid key size",
                    "Error", JOptionPane.ERROR_MESSAGE);
            keySize.requestFocus();
            return;
        }
        if ("ECDSA".equals(alg.getCrypto())) {
            String curve = (String)ec.getSelectedItem();
            if (curve != null) {
                ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(curve);
                gen.setAlgorithm(alg.getCrypto(), ecSpec);
            }
        } else {
            gen.setAlgorithm(alg.getCrypto(), ksize);
        }
        gen.setSignatureAlgorithm(alg.name());

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

        Certificate sgnrchain[] = null;
        if (signedBy.isSelected() && signer.getSelectedItem() != null
                && signatureAlg.getSelectedItem() != null) {
            KeyStoreEntry sgnr = (KeyStoreEntry)signer.getSelectedItem();
            char sgnrpwd[] = signerPassword.getPassword();

            try {
                sgnrchain = keystore.getCertificateChain(sgnr.getAlias());
                X509Certificate ic = (X509Certificate)sgnrchain[0];
                PrivateKey issuerKey = (PrivateKey)keystore.getKey(
                        sgnr.getAlias(), sgnrpwd);
                SignatureAlgorithm issuerAlg
                        = (SignatureAlgorithm)signatureAlg.getSelectedItem();
                gen.setSignatureAlgorithm(alg.name());
                gen.setIssuer(ic.getSubjectX500Principal(), issuerKey);
             } catch (RuntimeException | KeyStoreException
                     | NoSuchAlgorithmException
                     | UnrecoverableKeyException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (ca.isSelected()) {
            gen.setBasicConstraints(0);
        } else {
            gen.setBasicConstraints(-1);
        }

        try {
            Certificate certificate = gen.build();
            privateKey = gen.getPrivateKey();
            if (sgnrchain == null) {
                chain = new Certificate[] {certificate};
            } else {
                chain = new Certificate[sgnrchain.length+1];
                chain[0] = certificate;
                int ix = 0;
                for (Certificate c: sgnrchain) {
                    chain[++ix] = c;
                }
            }
        } catch (RuntimeException | OperatorCreationException
                | CertificateException | IOException
                | NoSuchAlgorithmException | NoSuchProviderException
                | InvalidAlgorithmParameterException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Signature Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        done = true;
        dispose();
    }//GEN-LAST:event_okActionPerformed

    private void autoSignedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoSignedActionPerformed
        setSignerState();
    }//GEN-LAST:event_autoSignedActionPerformed

    private void signedByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signedByActionPerformed
        setSignerState();
    }//GEN-LAST:event_signedByActionPerformed

    private void signerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signerActionPerformed
        signatureAlg.removeAllItems();
        if (signer.getSelectedItem() != null) {
            KeyStoreEntry entry = (KeyStoreEntry)signer.getSelectedItem();
            if (entry != null) {
                try {
                    Certificate cert = keystore.getCertificate(entry.getAlias());
                    String crypto = cert.getPublicKey().getAlgorithm();
                    SignatureAlgorithm[] algs = SignatureAlgorithm.getForCrypto(crypto);
                    for (int i = 0; i < algs.length; ++i) {
                        signatureAlg.addItem(algs[i]);
                    }
                } catch (KeyStoreException ex) {
                    JOptionPane.showMessageDialog(this, "Keystore error", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_signerActionPerformed

    private void algorithmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algorithmActionPerformed
        SignatureAlgorithm alg = (SignatureAlgorithm)algorithm.getSelectedItem();
        CardLayout layout = (CardLayout)algorithmParams.getLayout();
        if (alg != null && "ECDSA".equals(alg.getCrypto())) {
            layout.show(algorithmParams, "EC");
        } else {
            layout.show(algorithmParams, "default");
        }
    }//GEN-LAST:event_algorithmActionPerformed

    private void ecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ecActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ecActionPerformed

    private static void addField(String name, JTextField field,
            X500PrincipalBuilder nb) {
        String s = field.getText().trim();
        if (s.length() > 0) {
            nb.add(name, s);
        }
    }

    private void setSignerState() {
        signer.setEnabled(signedBy.isSelected());
        signerPassword.setEnabled(signedBy.isSelected());
        signatureAlg.setEnabled(signedBy.isSelected());
        if (signedBy.isSelected()) {
            signerActionPerformed(null);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algorithm;
    private javax.swing.JPanel algorithmParams;
    private javax.swing.JTextField alias;
    private javax.swing.JRadioButton autoSigned;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JCheckBox ca;
    private javax.swing.JButton cancel;
    private javax.swing.JTextField commonName;
    private javax.swing.JTextField country;
    private javax.swing.JComboBox<String> ec;
    private javax.swing.JTextField endDate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField keySize;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JTextField locality;
    private javax.swing.JButton ok;
    private javax.swing.JTextField organisation;
    private javax.swing.JTextField organisationUnit;
    private javax.swing.JTextField organisationUnit2;
    private javax.swing.JTextField organisationUnit3;
    private javax.swing.JPasswordField password;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JTextField serialNumber;
    private javax.swing.JComboBox signatureAlg;
    private javax.swing.JRadioButton signedBy;
    private javax.swing.JComboBox signer;
    private javax.swing.ButtonGroup signerGroup;
    private javax.swing.JPasswordField signerPassword;
    private javax.swing.JTextField startDate;
    private javax.swing.JTextField state;
    private javax.swing.JPasswordField verification;
    // End of variables declaration//GEN-END:variables
}
