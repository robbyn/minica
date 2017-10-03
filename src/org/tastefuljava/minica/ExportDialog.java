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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.Arrays;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;

/**
 *
 * @author  PERRYM
 */
public class ExportDialog extends JDialog {
    private final KeyStore keystore;
    private final Configuration conf;
    private boolean done;

    public ExportDialog(Frame parent, Configuration conf, KeyStore keystore,
            KeyStoreEntry current) throws KeyStoreException, IOException {
        super(parent, true);
        this.conf = conf;
        this.keystore = keystore;
        initComponents();
        String s = conf.getString("export.file", "");
        File dir = s.length() == 0
                ? new File(".") : new File(s).getParentFile();
        String name = current.getAlias();
        String format = conf.getString("export.format", "pem");
        switch (format) {
            case "pem":
                pem.setSelected(true);
                name += ".pem";
                break;
            case "pkcs12":
            case "p12":
                pkcs12.setSelected(true);
                name += ".p12";
                break;
            case "jks":
                jks.setSelected(true);
                name += ".jks";
                break;
            default:
                der.setSelected(true);
                name += ".der";
                break;
        }
        File f = new File(dir, name);
        file.setText(f.getCanonicalPath());
        file.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setButtonStatus();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                setButtonStatus();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                setButtonStatus();
            }
        });
        KeyStoreEntry entries[] = KeyStoreEntry.getAll(keystore);
        Arrays.sort(entries, KeyStoreEntry.TYPE_ALIAS_ORDER);
        alias.setRenderer(new KeyStoreEntryRenderer());
        alias.removeAllItems();
        for (int i = 0; i < entries.length; ++i) {
            KeyStoreEntry entry = entries[i];
            alias.addItem(entry);
        }
        alias.setSelectedItem(current);
        init(parent);
    }

    private void init(Frame parent) {
        getRootPane().setDefaultButton(ok);
        pack();
        setButtonStatus();
        Rectangle rc = parent.getBounds();
        int x = Math.max(rc.x + (rc.width-getWidth())/2, 0);
        int y = Math.max(rc.y + (rc.height-getHeight())/2, 0);
        setLocation(x, y);
    }

    public boolean doDialog() {
        setVisible(true);
        return done;
    }

    private void setButtonStatus() {
        ok.setEnabled(file.getText().trim().length() > 0);
        cancel.setEnabled(true);
    }

    private void doExport() {
        KeyStoreEntry entry = (KeyStoreEntry)alias.getSelectedItem();
        File outFile = new File(file.getText());
        try (OutputStream out = new FileOutputStream(outFile)) {
            char pwd[] = {};
            Key key = null;
            if (exportKey.isSelected()) {
                pwd = password.getPassword();
                key = keystore.getKey(entry.getAlias(), pwd);
                pwd = outPassword.getPassword();
                char ver[] = verification.getPassword();
                if (!new String(pwd).equals(new String(ver))) {
                    throw new Exception("Output password and verification "
                            + "are different");
                }
            }
            Certificate cert = null;
            Certificate chain[] = null;
            if (exportChain.isSelected()) {
                chain = keystore.getCertificateChain(
                        entry.getAlias());
            } else if (exportCert.isSelected()) {
                cert = keystore.getCertificate(
                        entry.getAlias());
            }

            if (pem.isSelected()) {
                exportPem(out, key, cert, chain, pwd);
                conf.setString("export.format", "pem");
            } else if (pkcs12.isSelected()) {
                exportPkcs12(out, entry.getAlias(), key, cert, chain, pwd);
                conf.setString("export.format", "pkcs12");
            } else if (jks.isSelected()) {
                exportJks(out, entry.getAlias(), key, cert, chain, pwd);
                conf.setString("export.format", "jks");
            } else if (der.isSelected()) {
                exportDer(out, entry.getAlias(), key, cert, chain, pwd);
                conf.setString("export.format", "der");
            }
            conf.setString("export.file", file.getText());
            done = true;
        } catch (Exception e) {
            done = false;
            JOptionPane.showMessageDialog(getParent(),
                    "Error while exporting to " + outFile, "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPem(OutputStream stream, Key key,
            Certificate cert, Certificate chain[], char pwd[])
            throws Exception {
        PEMWriter out = new PEMWriter(new OutputStreamWriter(stream, "UTF-8"));
        if (key != null) {
            if (pwd.length > 0)  {
                JcePEMEncryptorBuilder builder = new JcePEMEncryptorBuilder("DES-EDE3-CBC");
                builder.setSecureRandom(SecureRandom.getInstance("SHA1PRNG"));
                PEMEncryptor pemEncryptor = builder.build(pwd);
                out.writeObject(key, pemEncryptor);
            } else {
                out.writeObject(key);
            }
        }
        if (chain != null) {
            for (int i = 0; i < chain.length; ++i) {
                out.writeObject(chain[i]);
            }
        } else if (cert != null) {
            out.writeObject(cert);
        }
        out.flush();
    }

    private void exportPkcs12(OutputStream out, String alias, Key key,
            Certificate cert, Certificate chain[], char pwd[])
            throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12", "BC");
        store.load(null, null);
        if (key != null) {
            for (int i = 1; i < chain.length; ++i) {
                store.setCertificateEntry("issuer" + i, chain[i]);
            }
            store.setKeyEntry(alias, key, pwd, chain);
        } else if (chain != null) {
            for (int i = 0; i < chain.length; ++i) {
                String al = i == 0 ? alias : "issuer" + i;
                store.setCertificateEntry(al, chain[i]);
            }
        } else {
            store.setCertificateEntry(alias, cert);
        }
        store.store(out, pwd);
    }

    private void exportJks(OutputStream out, String alias, Key key,
            Certificate cert, Certificate[] chain, char[] pwd)
            throws Exception {
        KeyStore store = KeyStore.getInstance("JKS");
        store.load(null, null);
        if (key != null) {
            for (int i = 1; i < chain.length; ++i) {
                store.setCertificateEntry("issuer" + i, chain[i]);
            }
            store.setKeyEntry(alias, key, pwd, chain);
        } else if (chain != null) {
            for (int i = 0; i < chain.length; ++i) {
                String al = i == 0 ? alias : "issuer" + i;
                store.setCertificateEntry(al, chain[i]);
            }
        } else {
            store.setCertificateEntry(alias, cert);
        }
        store.store(out, pwd);
    }

    private void exportDer(OutputStream out, String alias, Key key,
            Certificate cert, Certificate[] chain, char[] pwd)
            throws Exception {
        if (chain != null) {
            for (Certificate cer: chain) {
                out.write(cer.getEncoded());
            }
        } else if (cert != null) {
            out.write(cert.getEncoded());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        formatGroup = new javax.swing.ButtonGroup();
        bottomPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        ok = new javax.swing.JButton();
        cancel = new javax.swing.JButton();
        optionPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        file = new javax.swing.JTextField();
        browse = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        pem = new javax.swing.JRadioButton();
        pkcs12 = new javax.swing.JRadioButton();
        jks = new javax.swing.JRadioButton();
        der = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        alias = new javax.swing.JComboBox();
        exportKey = new javax.swing.JCheckBox();
        exportCert = new javax.swing.JCheckBox();
        exportChain = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        outPassword = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();
        verification = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Export key and/or certificate");
        setResizable(false);

        bottomPanel.setLayout(new java.awt.BorderLayout());

        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(17, 12, 11, 11));
        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        ok.setText("OK");
        ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okActionPerformed(evt);
            }
        });
        buttonPanel.add(ok);

        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });
        buttonPanel.add(cancel);

        bottomPanel.add(buttonPanel, java.awt.BorderLayout.EAST);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        optionPanel.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("File:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        optionPanel.add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 180;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 5);
        optionPanel.add(file, gridBagConstraints);

        browse.setText("Browse...");
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        optionPanel.add(browse, gridBagConstraints);

        jLabel6.setText("Format:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        optionPanel.add(jLabel6, gridBagConstraints);

        formatGroup.add(pem);
        pem.setSelected(true);
        pem.setText("PEM (OpenSSL, OpenSSH)");
        pem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pemActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        optionPanel.add(pem, gridBagConstraints);

        formatGroup.add(pkcs12);
        pkcs12.setText("PKCS12");
        pkcs12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pkcs12ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        optionPanel.add(pkcs12, gridBagConstraints);

        formatGroup.add(jks);
        jks.setText("JKS");
        jks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jksActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        optionPanel.add(jks, gridBagConstraints);

        formatGroup.add(der);
        der.setText("DER");
        der.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                derActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        optionPanel.add(der, gridBagConstraints);

        jLabel1.setText("Alias:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        optionPanel.add(jLabel1, gridBagConstraints);

        alias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aliasActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        optionPanel.add(alias, gridBagConstraints);

        exportKey.setText("Export key");
        exportKey.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exportKey.setEnabled(false);
        exportKey.setMargin(new java.awt.Insets(0, 0, 0, 0));
        exportKey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportKeyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        optionPanel.add(exportKey, gridBagConstraints);

        exportCert.setSelected(true);
        exportCert.setText("Export certificate");
        exportCert.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exportCert.setMargin(new java.awt.Insets(0, 0, 0, 0));
        exportCert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportCertActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        optionPanel.add(exportCert, gridBagConstraints);

        exportChain.setText("Export whole chain");
        exportChain.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exportChain.setMargin(new java.awt.Insets(0, 0, 0, 0));
        exportChain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportChainActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        optionPanel.add(exportChain, gridBagConstraints);

        jLabel3.setText("Password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        optionPanel.add(jLabel3, gridBagConstraints);

        password.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        optionPanel.add(password, gridBagConstraints);

        jLabel4.setText("Output password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        optionPanel.add(jLabel4, gridBagConstraints);

        outPassword.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        optionPanel.add(outPassword, gridBagConstraints);

        jLabel5.setText("Verification:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        optionPanel.add(jLabel5, gridBagConstraints);

        verification.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        optionPanel.add(verification, gridBagConstraints);

        getContentPane().add(optionPanel, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(Filters.PEM_FILEFILTER);
        chooser.addChoosableFileFilter(Filters.PKCS12_FILEFILTER);
        chooser.addChoosableFileFilter(Filters.JKS_FILEFILTER);
        chooser.addChoosableFileFilter(Filters.CERT_FILEFILTER);
        if (pem.isSelected()) {
            chooser.setFileFilter(Filters.PEM_FILEFILTER);
        } else if (pkcs12.isSelected()) {
            chooser.setFileFilter(Filters.PKCS12_FILEFILTER);
        } else if (jks.isSelected()) {
            chooser.setFileFilter(Filters.JKS_FILEFILTER);
        } else if (der.isSelected()) {
            chooser.setFileFilter(Filters.CERT_FILEFILTER);
        }
        chooser.setDialogTitle("Export to file");
        String s = file.getText().trim();
        if (s.length() > 0) {
            chooser.setSelectedFile(new File(s));
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(file)) {
            file.setText(chooser.getSelectedFile().getAbsolutePath());
            if (chooser.getFileFilter() == Filters.PEM_FILEFILTER) {
                pem.setSelected(true);
            } else if (chooser.getFileFilter() == Filters.PKCS12_FILEFILTER) {
                pkcs12.setSelected(true);
            } else if (chooser.getFileFilter() == Filters.JKS_FILEFILTER) {
                jks.setSelected(true);
            } else if (chooser.getFileFilter() == Filters.CERT_FILEFILTER) {
                der.setSelected(true);
            }
            setButtonStatus();
        }
    }//GEN-LAST:event_browseActionPerformed

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        dispose();
    }//GEN-LAST:event_cancelActionPerformed

    private void okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okActionPerformed
        try {
            doExport();
            if (done) {
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error while importing data",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_okActionPerformed

    private void aliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aliasActionPerformed
        KeyStoreEntry entry = (KeyStoreEntry)alias.getSelectedItem();
        boolean isKey = entry != null && entry.isKey();
        if (!isKey) {
            exportKey.setSelected(false);
        }
        exportKey.setEnabled(isKey);
    }//GEN-LAST:event_aliasActionPerformed

    private void exportChainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportChainActionPerformed
        if (exportChain.isSelected()) {
            exportCert.setSelected(true);
        }
    }//GEN-LAST:event_exportChainActionPerformed

    private void exportCertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportCertActionPerformed
        if (!exportCert.isSelected()) {
            exportChain.setSelected(false);
        }
    }//GEN-LAST:event_exportCertActionPerformed

    private void exportKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportKeyActionPerformed
        password.setEditable(exportKey.isSelected());
        outPassword.setEditable(exportKey.isSelected());
        verification.setEditable(exportKey.isSelected());
    }//GEN-LAST:event_exportKeyActionPerformed

    private void pemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pemActionPerformed
        formatChanged("pem");
    }//GEN-LAST:event_pemActionPerformed

    private void pkcs12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pkcs12ActionPerformed
        formatChanged("p12");
    }//GEN-LAST:event_pkcs12ActionPerformed

    private void jksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jksActionPerformed
        formatChanged("jks");
    }//GEN-LAST:event_jksActionPerformed

    private void derActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_derActionPerformed
        formatChanged("der");
    }//GEN-LAST:event_derActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox alias;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton browse;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancel;
    private javax.swing.JRadioButton der;
    private javax.swing.JCheckBox exportCert;
    private javax.swing.JCheckBox exportChain;
    private javax.swing.JCheckBox exportKey;
    private javax.swing.JTextField file;
    private javax.swing.ButtonGroup formatGroup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JRadioButton jks;
    private javax.swing.JButton ok;
    private javax.swing.JPanel optionPanel;
    private javax.swing.JPasswordField outPassword;
    private javax.swing.JPasswordField password;
    private javax.swing.JRadioButton pem;
    private javax.swing.JRadioButton pkcs12;
    private javax.swing.JPasswordField verification;
    // End of variables declaration//GEN-END:variables

    private void formatChanged(String fmt) {
        File f = new File(file.getText());
        String name = f.getName();
        int ix = name.lastIndexOf('.');
        if (ix >= 0) {
            name = name.substring(0, ix+1);
        }
        name += fmt;
        file.setText(new File(f.getParentFile(), name).getAbsolutePath());
    }
}
