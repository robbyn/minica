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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

public class ImportDialog extends JDialog {
    private static final String PAGES[] = {"chooser-page", "list-page"};

    private static final int FIRST_PAGE = 0;
    private static final int LAST_PAGE = PAGES.length - 1;

    private static final int CHOOSER_PAGE = 0;
    private static final int LIST_PAGE = 1;

    private final KeyStore keystore;
    private int currentPage = -1;
    private boolean done;
    private KeyPair keys;
    private X509Certificate certs[];
    private final ImportTableModel model = new ImportTableModel();
    private final Configuration conf;

    public ImportDialog(JFrame parent, Configuration conf, KeyStore keystore) {
        super(parent, true);
        this.keystore = keystore;
        this.conf = conf;
        initComponents();
        init(conf);
    }

    private void init(Configuration conf1) {
        chooser.addChoosableFileFilter(Filters.PEM_FILEFILTER);
        chooser.addChoosableFileFilter(Filters.PKCS12_FILEFILTER);
        chooser.addChoosableFileFilter(Filters.JKS_FILEFILTER);
        chooser.addChoosableFileFilter(Filters.CERT_FILEFILTER);
        String s = conf1.getString("import.file", "");
        if (s.length() > 0) {
            chooser.setSelectedFile(new File(s));
        }
        table.setModel(model);
        table.setDefaultEditor(KeyStoreEntry.class, new KeyStoreEntryEditor());
        table.setDefaultRenderer(KeyStoreEntry.class, new KeyStoreEntryRenderer());
        pack();
        setCurrentPage(CHOOSER_PAGE);
    }

    public boolean doDialog() {
        setVisible(true);
        return done;
    }

    private void setButtonStatus() {
        switch (currentPage) {
            case CHOOSER_PAGE:
                back.setEnabled(false);
                next.setEnabled(chooser.getSelectedFile() != null);
                ok.setEnabled(false);
                cancel.setEnabled(true);
                break;
            case LIST_PAGE:
                back.setEnabled(true);
                next.setEnabled(false);
                ok.setEnabled(true);
                cancel.setEnabled(true);
                break;
        }
    }

    private void setCurrentPage(int newPage) {
        switch (currentPage) {
            case CHOOSER_PAGE:
                if (chooser.getSelectedFile() != null) {
                    try {
                        if (chooser.getFileFilter() == Filters.PEM_FILEFILTER) {
                            loadPem(chooser.getSelectedFile());
                        } else if (chooser.getFileFilter()
                                == Filters.PKCS12_FILEFILTER) {
                            loadPkcs12(chooser.getSelectedFile());
                        } else if (chooser.getFileFilter()
                                == Filters.JKS_FILEFILTER) {
                            loadJks(chooser.getSelectedFile());
                        } else if (chooser.getFileFilter()
                                == Filters.CERT_FILEFILTER) {
                            loadCert(chooser.getSelectedFile());
                        }
                        fillTable();
                    } catch (IOException | GeneralSecurityException e) {
                        JOptionPane.showMessageDialog(this,
                                "Error while importing file "
                                + chooser.getSelectedFile(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                break;
        }
        currentPage = newPage;
        CardLayout layout = (CardLayout) cardPanel.getLayout();
        layout.show(cardPanel, PAGES[currentPage]);
        setButtonStatus();
    }

    private void nextPage() {
        if (currentPage < LAST_PAGE) {
            setCurrentPage(currentPage + 1);
        }
    }

    private void prevPage() {
        if (currentPage > FIRST_PAGE) {
            setCurrentPage(currentPage - 1);
        }
    }

    private void loadPem(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            PEMParser in = new PEMParser(reader);
            Object obj = in.readObject();
            if (obj == null) {
                // Some PEM files have garbarge at the top
                for (int i = 0; i < 9 && obj == null; ++i) {
                    obj = in.readObject();
                }
            }
            keys = null;
            JcaX509CertificateConverter cconv = new JcaX509CertificateConverter();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            if (obj instanceof PEMEncryptedKeyPair) {
                // Encrypted key - we will use provided password
                PasswordDialog dlg = new PasswordDialog(this,
                        "Enter password for" + file);
                char[] pwd = dlg.getPassword();
                if (pwd != null) {
                    PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(pwd);
                    keys = converter.getKeyPair(((PEMEncryptedKeyPair) obj).decryptKeyPair(decProv));
                }
                obj = in.readObject();
            } else if (obj instanceof PEMKeyPair) {
                // Unencrypted key - no password needed
                keys = converter.getKeyPair((PEMKeyPair) obj);
                obj = in.readObject();
            }
            List<X509Certificate> list = new ArrayList<>();
            while (obj != null) {
                if (obj instanceof X509CertificateHolder) {
                    list.add(cconv.getCertificate((X509CertificateHolder) obj));
                }
                obj = in.readObject();
            }
            certs = list.toArray(new X509Certificate[list.size()]);
        } catch (CertificateException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    private void loadPkcs12(File file)
            throws IOException, GeneralSecurityException {
        loadKeystore(file, "PKCS12");
    }

    private void loadJks(File file)
            throws IOException, GeneralSecurityException {
        loadKeystore(file, "JKS");
    }

    private void loadCert(File file)
            throws IOException, GeneralSecurityException {
        InputStream in = new FileInputStream(file);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            @SuppressWarnings("unchecked")
            Collection<X509Certificate> col
                    = (Collection<X509Certificate>) cf.generateCertificates(in);
            certs = col.toArray(new X509Certificate[col.size()]);
        } finally {
            in.close();
        }
    }

    private void loadKeystore(File file, String type)
            throws IOException, GeneralSecurityException {
        PasswordDialog dlg = new PasswordDialog(this,
                "Enter password for" + file);
        char pwd[] = dlg.getPassword();
        if (pwd == null) {
            return;
        } else if (pwd.length == 0) {
            pwd = null;
        }
        InputStream in = new FileInputStream(file);
        try {
            List<X509Certificate> certificates = new ArrayList<>();
            KeyStore store = KeyStore.getInstance(type);
            store.load(in, pwd);
            for (Enumeration<String> enm = store.aliases();
                    enm.hasMoreElements();) {
                String alias = enm.nextElement();
                if (store.entryInstanceOf(alias,
                        KeyStore.PrivateKeyEntry.class)) {
                    keys = new KeyPair(store.getCertificate(alias).getPublicKey(),
                            (PrivateKey) store.getKey(alias, pwd));
                    Certificate chain[] = store.getCertificateChain(alias);
                    if (chain != null) {
                        for (Certificate cert : chain) {
                            if (cert instanceof X509Certificate) {
                                certificates.add((X509Certificate) cert);
                            }
                        }
                    }
                } else if (store.entryInstanceOf(alias,
                        KeyStore.SecretKeyEntry.class)) {
                    System.out.println(alias + " is secret key");
                } else if (store.entryInstanceOf(alias,
                        KeyStore.TrustedCertificateEntry.class)) {
                    Certificate cert = store.getCertificate(alias);
                    if (cert != null) {
                        if (cert instanceof X509Certificate) {
                            certificates.add((X509Certificate) cert);
                        }
                    }
                } else {
                    System.out.println(alias + " is unknown entry type");
                }
            }
            certs = certificates.toArray(
                    new X509Certificate[certificates.size()]);
        } finally {
            in.close();
        }
    }

    private void fillTable() throws IOException, KeyStoreException {
        model.clear();
        if (keys != null) {
            X509Certificate cert = findCert(keys.getPublic());
            if (cert == null) {
                throw new IOException("Key has no certificate");
            }
            String alias = keystore.getCertificateAlias(cert);
            if (alias == null) {
                alias = "New key";
            }
            model.add(new PrivateKeyEntry(alias, keys.getPrivate(),
                    findChain(cert)));
        }
        int st = keys == null ? 0 : 1;
        for (int i = st; i < certs.length; ++i) {
            X509Certificate cert = certs[i];
            String alias = keystore.getCertificateAlias(cert);
            if (alias == null) {
                alias = cert.getSubjectDN().getName();
                model.add(new CertificateEntry(alias, cert));
            }
        }
    }

    private X509Certificate findCert(Principal subject) {
        for (int i = 0; i < certs.length; ++i) {
            X509Certificate cert = certs[i];
            if (subject.equals(cert.getSubjectDN())) {
                return cert;
            }
        }
        return null;
    }

    private X509Certificate findCert(PublicKey key) {
        for (int i = 0; i < certs.length; ++i) {
            X509Certificate cert = certs[i];
            if (key.equals(cert.getPublicKey())) {
                return cert;
            }
        }
        return null;
    }

    private X509Certificate[] findChain(X509Certificate cert) {
        List<X509Certificate> list = new ArrayList<X509Certificate>();
        list.add(cert);
        while (!cert.getSubjectDN().equals(cert.getIssuerDN())) {
            cert = findCert(cert.getIssuerDN());
            if (cert == null) {
                break;
            }
            list.add(cert);
        }
        return list.toArray(new X509Certificate[list.size()]);
    }

    private void doImport() throws KeyStoreException {
        TableCellEditor editor = table.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }
        for (int i = model.getRowCount(); --i >= 0;) {
            KeyStoreEntry entry = model.getEntry(i);
            entry.addTo(keystore, password.getPassword());
        }
        done = true;
        conf.setString("import.file",
                chooser.getSelectedFile().getAbsolutePath());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cardPanel = new javax.swing.JPanel();
        chooserPanel = new javax.swing.JPanel();
        chooser = new javax.swing.JFileChooser();
        listPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        listScroll = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        passwordPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        bottomPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        back = new javax.swing.JButton();
        next = new javax.swing.JButton();
        ok = new javax.swing.JButton();
        cancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        cardPanel.setLayout(new java.awt.CardLayout());

        chooserPanel.setLayout(new java.awt.GridBagLayout());

        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setControlButtonsAreShown(false);
        chooser.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                chooserPropertyChange(evt);
            }
        });
        chooserPanel.add(chooser, new java.awt.GridBagConstraints());

        cardPanel.add(chooserPanel, "chooser-page");

        listPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Objects to import:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        listPanel.add(jLabel1, gridBagConstraints);

        listScroll.setPreferredSize(new java.awt.Dimension(454, 250));

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        listScroll.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 11);
        listPanel.add(listScroll, gridBagConstraints);

        passwordPanel.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        passwordPanel.add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 120;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        passwordPanel.add(password, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        listPanel.add(passwordPanel, gridBagConstraints);

        cardPanel.add(listPanel, "list-page");

        getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);

        bottomPanel.setLayout(new java.awt.BorderLayout());

        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(17, 12, 11, 11));
        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        back.setText("< Back");
        back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backActionPerformed(evt);
            }
        });
        buttonPanel.add(back);

        next.setText("Next >");
        next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextActionPerformed(evt);
            }
        });
        buttonPanel.add(next);

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

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        dispose();
    }//GEN-LAST:event_cancelActionPerformed

    private void okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okActionPerformed
        try {
            doImport();
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error while importing data",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }//GEN-LAST:event_okActionPerformed

    private void nextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextActionPerformed
        nextPage();
    }//GEN-LAST:event_nextActionPerformed

    private void backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backActionPerformed
        prevPage();
    }//GEN-LAST:event_backActionPerformed

    private void chooserPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_chooserPropertyChange
        if (evt.getPropertyName().equals("SelectedFileChangedProperty")) {
            setButtonStatus();
        }
    }//GEN-LAST:event_chooserPropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton back;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancel;
    private javax.swing.JPanel cardPanel;
    private javax.swing.JFileChooser chooser;
    private javax.swing.JPanel chooserPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel listPanel;
    private javax.swing.JScrollPane listScroll;
    private javax.swing.JButton next;
    private javax.swing.JButton ok;
    private javax.swing.JPasswordField password;
    private javax.swing.JPanel passwordPanel;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
