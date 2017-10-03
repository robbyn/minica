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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.bouncycastle.asn1.x500.X500Name;

public class MainFrame extends javax.swing.JFrame {
    private static final DateFormat dateFormat
            = new SimpleDateFormat("dd-MM-yyyy");
    private KeyStore keystore;
    private File keystoreFile;
    private boolean changed;
    private final File confFile = new File(System.getProperty("user.home"),
            "minica.properties");
    private final Configuration conf = new Configuration(confFile);

    public MainFrame() throws IOException, GeneralSecurityException {
        initComponents();
        init();
    }

    private void init() throws IOException, GeneralSecurityException {
        list.setCellRenderer(new KeyStoreEntryRenderer());
        keystore = KeyStore.getInstance("JKS");
        keystore.load(null, null);
        keystoreChanged();
        conf.load();
        refreshInfo();
        setBounds(conf.getInt("frame.x", getX()),
                conf.getInt("frame.y", getY()),
                conf.getInt("frame.width", getWidth()),
                conf.getInt("frame.height", getHeight()));
    }

    private void keystoreChanged() {
        try {
            newButton.setEnabled(true);
            openButton.setEnabled(true);
            saveButton.setEnabled(true);
            DefaultListModel model = new DefaultListModel();
            if (keystore != null) {
                KeyStoreEntry entries[] = KeyStoreEntry.getAll(keystore);
                Arrays.sort(entries, KeyStoreEntry.TYPE_ALIAS_ORDER);
                for (int i = 0; i < entries.length; ++i) {
                    model.addElement(entries[i]);
                }
            }
            list.setModel(model);
            if (keystoreFile == null) {
                setTitle("Untitled");
            } else {
                setTitle(keystoreFile.getPath());
            }
            changed = false;
        } catch (KeyStoreException e) {
            JOptionPane.showMessageDialog(this, "Error while reading keystore "
                    + keystoreFile, "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String formatDN(String dn) {
        dn = new X500Name(dn).toString();
        StringBuilder buf = new StringBuilder();
        int st = 0;
        for (int ix = dn.indexOf(','); ix >= 0; ix = dn.indexOf(',', st)) {
            buf.append(dn.substring(st, ix+1).trim());
            buf.append('\n');
            st = ix+1;
        }
        buf.append(dn.substring(st).trim());
        return buf.toString();
    }

    private void refreshInfo() {
        Object entries[] = list.getSelectedValues();
        X509Certificate cert = null;
        if (entries.length == 1) {
            KeyStoreEntry entry = (KeyStoreEntry)entries[0];
            try {
                cert = (X509Certificate)keystore.getCertificate(
                        entry.getAlias());
            } catch (KeyStoreException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid certificate in keystore", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        delete.setEnabled(entries.length > 0);
        if (cert == null) {
            serialNumber.setText("");
            serialNumberHex.setText("");
            subject.setText("");
            issuer.setText("");
            startDate.setText("-");
            endDate.setText("-");
            sshEncode.setEnabled(false);
        } else {
            serialNumber.setText(cert.getSerialNumber().toString());
            serialNumberHex.setText(cert.getSerialNumber().toString(16));
            subject.setText(formatDN(cert.getSubjectDN().getName()));
            issuer.setText(formatDN(cert.getIssuerDN().getName()));
            startDate.setText(cert.getNotBefore() == null ? "-"
                    : dateFormat.format(cert.getNotBefore()));
            endDate.setText(cert.getNotAfter() == null ? "-"
                    : dateFormat.format(cert.getNotAfter()));
            PublicKey pubKey = cert.getPublicKey();
            sshEncode.setEnabled(pubKey instanceof RSAPublicKey
                    || pubKey instanceof DSAPublicKey);
        }
        subject.setRows(subject.getLineCount());
        issuer.setRows(issuer.getLineCount());
    }

    private boolean save(boolean chooseFile) {
        File file = keystoreFile;
        if (chooseFile || file == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public String getDescription() {
                    return "Keystore files (*.jks)";
                }

                @Override
                public boolean accept(File file) {
                    return file.isDirectory() || file.isFile()
                            && file.getName().toLowerCase().endsWith(".jks");
                }
            });
            if (keystoreFile != null) {
                chooser.setSelectedFile(keystoreFile);
            }
            File dir = new File(conf.getString("keystore.dir",
                    System.getProperty("user.home")));
            if (dir.isDirectory()) {
                chooser.setCurrentDirectory(dir);
            }
            chooser.setDialogTitle("Save keystore");
            if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(this)) {
                file = chooser.getSelectedFile();
                if (file.getName().toLowerCase().indexOf('.') < 0) {
                    file = new File(file.getParentFile(),
                            file.getName() + ".jks");
                    conf.setString("keystore.dir",
                            file.getParentFile().getAbsolutePath());
                }
            } else {
                return false;
            }
        }
        PasswordDialog dlg = new PasswordDialog(this, "Enter password for "
                + file.getName());
        char pwd[] = dlg.getPassword();
        if (pwd != null) {
            try {
                if (file.isFile()) {
                    File backup = new File(file.getParentFile(),
                            file.getName() + ".bak");
                    if (backup.isFile()) {
                        backup.delete();
                    }
                    file.renameTo(backup);
                }
                try (OutputStream out = new FileOutputStream(file)) {
                    keystore.store(out, pwd);
                }
                keystoreFile = file;
                keystoreChanged();
                return true;
            } catch (IOException | GeneralSecurityException e) {
                JOptionPane.showMessageDialog(this,
                        "Error while saving the keystore " + keystoreFile,
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        infoPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        serialNumber = new javax.swing.JLabel();
        serialNumberHex = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        subject = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        issuer = new javax.swing.JTextArea();
        jLabel12 = new javax.swing.JLabel();
        startDate = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        endDate = new javax.swing.JLabel();
        listScroll = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        toolbar = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        genKeyButton = new javax.swing.JButton();
        signButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newStoreItem = new javax.swing.JMenuItem();
        openStoreItem = new javax.swing.JMenuItem();
        saveStoreItem = new javax.swing.JMenuItem();
        saveStoreAsItem = new javax.swing.JMenuItem();
        certMenu = new javax.swing.JMenu();
        genKeyItem = new javax.swing.JMenuItem();
        signItem = new javax.swing.JMenuItem();
        impor = new javax.swing.JMenuItem();
        export = new javax.swing.JMenuItem();
        delete = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        sshEncode = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        infoPanel.setLayout(new java.awt.GridBagLayout());

        jLabel4.setText("Serial number:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        infoPanel.add(jLabel4, gridBagConstraints);

        serialNumber.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        infoPanel.add(serialNumber, gridBagConstraints);

        serialNumberHex.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        infoPanel.add(serialNumberHex, gridBagConstraints);

        jLabel1.setText("Subject:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        infoPanel.add(jLabel1, gridBagConstraints);

        subject.setEditable(false);
        subject.setColumns(32);
        subject.setRows(5);
        subject.setMinimumSize(new java.awt.Dimension(256, 90));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        infoPanel.add(subject, gridBagConstraints);

        jLabel2.setText("Issuer:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        infoPanel.add(jLabel2, gridBagConstraints);

        issuer.setColumns(32);
        issuer.setEditable(false);
        issuer.setRows(5);
        issuer.setMinimumSize(new java.awt.Dimension(256, 90));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        infoPanel.add(issuer, gridBagConstraints);

        jLabel12.setText("Start date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        infoPanel.add(jLabel12, gridBagConstraints);

        startDate.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        infoPanel.add(startDate, gridBagConstraints);

        jLabel3.setText("End date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 11, 11);
        infoPanel.add(jLabel3, gridBagConstraints);

        endDate.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 11, 11);
        infoPanel.add(endDate, gridBagConstraints);

        getContentPane().add(infoPanel, java.awt.BorderLayout.CENTER);

        listScroll.setMinimumSize(new java.awt.Dimension(240, 24));
        listScroll.setPreferredSize(new java.awt.Dimension(240, 24));

        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listValueChanged(evt);
            }
        });
        listScroll.setViewportView(list);

        getContentPane().add(listScroll, java.awt.BorderLayout.WEST);

        statusPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

        statusLabel.setText("Status zone");
        statusPanel.add(statusLabel);

        getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);

        toolbar.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        newButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/new-ln.png"))); // NOI18N
        newButton.setToolTipText("New keystore");
        newButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/images/new-ld.png"))); // NOI18N
        newButton.setFocusable(false);
        newButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        newButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/new-lh.png"))); // NOI18N
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });
        jPanel1.add(newButton, new java.awt.GridBagConstraints());

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-ln.png"))); // NOI18N
        openButton.setToolTipText("Open keystore...");
        openButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-ld.png"))); // NOI18N
        openButton.setFocusable(false);
        openButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        openButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-lh.png"))); // NOI18N
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });
        jPanel1.add(openButton, new java.awt.GridBagConstraints());

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save-ln.png"))); // NOI18N
        saveButton.setToolTipText("Save keystore");
        saveButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save-ld.png"))); // NOI18N
        saveButton.setFocusable(false);
        saveButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        saveButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save-lh.png"))); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        jPanel1.add(saveButton, gridBagConstraints);

        genKeyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/addkey-ln.png"))); // NOI18N
        genKeyButton.setToolTipText("Generate key...");
        genKeyButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/images/addkey-ld.png"))); // NOI18N
        genKeyButton.setFocusable(false);
        genKeyButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        genKeyButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/addkey-lh.png"))); // NOI18N
        genKeyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genKeyButtonActionPerformed(evt);
            }
        });
        jPanel1.add(genKeyButton, new java.awt.GridBagConstraints());

        signButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sign-ln.png"))); // NOI18N
        signButton.setToolTipText("Sign certificate...");
        signButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sign-ld.png"))); // NOI18N
        signButton.setFocusable(false);
        signButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        signButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sign-lh.png"))); // NOI18N
        signButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signButtonActionPerformed(evt);
            }
        });
        jPanel1.add(signButton, new java.awt.GridBagConstraints());

        importButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/import-ln.png"))); // NOI18N
        importButton.setToolTipText("Import...");
        importButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/images/import-ld.png"))); // NOI18N
        importButton.setFocusable(false);
        importButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        importButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/import-lh.png"))); // NOI18N
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });
        jPanel1.add(importButton, new java.awt.GridBagConstraints());

        exportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/export-ln.png"))); // NOI18N
        exportButton.setToolTipText("Export...");
        exportButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/images/export-ld.png"))); // NOI18N
        exportButton.setFocusable(false);
        exportButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        exportButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/export-lh.png"))); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        jPanel1.add(exportButton, new java.awt.GridBagConstraints());

        toolbar.add(jPanel1, java.awt.BorderLayout.WEST);

        getContentPane().add(toolbar, java.awt.BorderLayout.NORTH);

        fileMenu.setText("File");

        newStoreItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/new-sn.png"))); // NOI18N
        newStoreItem.setText("New keystore");
        newStoreItem.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/images/new-sd.png"))); // NOI18N
        newStoreItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newStoreItemActionPerformed(evt);
            }
        });
        fileMenu.add(newStoreItem);

        openStoreItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-sn.png"))); // NOI18N
        openStoreItem.setText("Open keystore...");
        openStoreItem.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-sd.png"))); // NOI18N
        openStoreItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openStoreItemActionPerformed(evt);
            }
        });
        fileMenu.add(openStoreItem);

        saveStoreItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save-sn.png"))); // NOI18N
        saveStoreItem.setText("Save keystore");
        saveStoreItem.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save-sd.png"))); // NOI18N
        saveStoreItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveStoreItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveStoreItem);

        saveStoreAsItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/blank-s.png"))); // NOI18N
        saveStoreAsItem.setText("Save keystore as...");
        saveStoreAsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveStoreAsItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveStoreAsItem);

        menuBar.add(fileMenu);

        certMenu.setText("Certificates");

        genKeyItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/addkey-sn.png"))); // NOI18N
        genKeyItem.setText("Generate key...");
        genKeyItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genKeyItemActionPerformed(evt);
            }
        });
        certMenu.add(genKeyItem);

        signItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sign-sn.png"))); // NOI18N
        signItem.setText("Sign certificate...");
        signItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signItemActionPerformed(evt);
            }
        });
        certMenu.add(signItem);

        impor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/import-sn.png"))); // NOI18N
        impor.setText("Import...");
        impor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imporActionPerformed(evt);
            }
        });
        certMenu.add(impor);

        export.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/export-sn.png"))); // NOI18N
        export.setText("Export...");
        export.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportActionPerformed(evt);
            }
        });
        certMenu.add(export);

        delete.setText("Delete...");
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteActionPerformed(evt);
            }
        });
        certMenu.add(delete);
        certMenu.add(jSeparator1);

        sshEncode.setText("SSH encoding...");
        sshEncode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sshEncodeActionPerformed(evt);
            }
        });
        certMenu.add(sshEncode);

        menuBar.add(certMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        conf.setInt("frame.x", getX());
        conf.setInt("frame.y", getY());
        conf.setInt("frame.width", getWidth());
        conf.setInt("frame.height", getHeight());
        try {
            conf.store();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save configuration "
                    + confFile, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_formWindowClosing

    private void listValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listValueChanged
        refreshInfo();
    }//GEN-LAST:event_listValueChanged

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        exportActionPerformed(evt);
    }//GEN-LAST:event_exportButtonActionPerformed

    private void imporActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imporActionPerformed
        try {
            ImportDialog dlg = new ImportDialog(this, conf, keystore);
            if (dlg.doDialog()) {
                conf.store();
                keystoreChanged();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error during import",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_imporActionPerformed

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        imporActionPerformed(evt);
    }//GEN-LAST:event_importButtonActionPerformed

    private void signButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signButtonActionPerformed
        signItemActionPerformed(evt);
    }//GEN-LAST:event_signButtonActionPerformed

    private void genKeyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genKeyButtonActionPerformed
        genKeyItemActionPerformed(evt);
    }//GEN-LAST:event_genKeyButtonActionPerformed

    private void exportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportActionPerformed
        try {
            if (KeyStoreEntry.getAll(keystore).length == 0) {
                JOptionPane.showMessageDialog(this,
                        "The keystore is empty", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                ExportDialog dlg = new ExportDialog(this, conf, keystore,
                        (KeyStoreEntry)list.getSelectedValue());
                if (dlg.doDialog()) {
                    conf.store();
                }
            }
        } catch (IOException | KeyStoreException e) {
            JOptionPane.showMessageDialog(this, "Error during export",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_exportActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        saveStoreItemActionPerformed(evt);
    }//GEN-LAST:event_saveButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        newStoreItemActionPerformed(evt);
    }//GEN-LAST:event_newButtonActionPerformed

    private void signItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signItemActionPerformed
        try {
            if (KeyStoreEntry.getAllKeys(keystore).length == 0) {
                JOptionPane.showMessageDialog(this,
                        "There are no keys in the keystore", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                SignDialog dlg = new SignDialog(this, keystore,
                        (KeyStoreEntry)list.getSelectedValue());
                if (dlg.doDialog()) {
                    changed = true;
                    refreshInfo();
                }
            }
        } catch (KeyStoreException e) {
            JOptionPane.showMessageDialog(this, "Could not add key to the keystore",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_signItemActionPerformed

    private void genKeyItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genKeyItemActionPerformed
        try {
            GenerateKeyDialog dlg = new GenerateKeyDialog(this, conf, keystore);
            if (dlg.showDialog()) {
                keystore.setKeyEntry(dlg.getAlias(), dlg.getKey(),
                        dlg.getPassword(), dlg.getChain());
                changed = true;
                KeyStoreEntry entry = new KeyStoreEntry(dlg.getAlias(), true);
                DefaultListModel model = (DefaultListModel)list.getModel();
                model.addElement(entry);
                list.setSelectedValue(entry, true);
            }
        } catch (KeyStoreException e) {
            JOptionPane.showMessageDialog(this, "Could not add key to the keystore",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_genKeyItemActionPerformed

    private void saveStoreAsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveStoreAsItemActionPerformed
        save(true);
    }//GEN-LAST:event_saveStoreAsItemActionPerformed

    private void saveStoreItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveStoreItemActionPerformed
        save(false);
    }//GEN-LAST:event_saveStoreItemActionPerformed

    private void openStoreItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openStoreItemActionPerformed
        if (changed && !save(false)) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return "Keystore files (*.jks)";
            }

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.isFile()
                        && file.getName().toLowerCase().endsWith(".jks");
            }
        });
        if (keystoreFile != null) {
            chooser.setSelectedFile(keystoreFile);
        }
        File dir = new File(conf.getString("keystore.dir",
                System.getProperty("user.home")));
        if (dir.isDirectory()) {
            chooser.setCurrentDirectory(dir);
        }
        chooser.setDialogTitle("Open keystore");
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File file = chooser.getSelectedFile();
            try {
                String type = "JKS";
                if (file.getName().toLowerCase().endsWith(".p12")) {
                    type = "PKCS12";
                }
                KeyStore ks = KeyStore.getInstance(type);
                try (InputStream in = new FileInputStream(file)) {
                    ks.load(in, null);
                }
                keystore = ks;
                keystoreFile = file;
                conf.setString("keystore.dir",
                        file.getParentFile().getAbsolutePath());
                keystoreChanged();
            } catch (IOException | GeneralSecurityException e) {
                JOptionPane.showMessageDialog(this, "Could not open file "
                        + chooser.getSelectedFile(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_openStoreItemActionPerformed

    private void newStoreItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newStoreItemActionPerformed
        if (changed && !save(false)) {
            return;
        }
        try {
            keystoreFile = null;
            keystore = KeyStore.getInstance("JKS");
            keystore.load(null, null);
            keystoreChanged();
        } catch (IOException | GeneralSecurityException e) {
            JOptionPane.showMessageDialog(this, "Could create keystore",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_newStoreItemActionPerformed

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        openStoreItemActionPerformed(evt);
    }//GEN-LAST:event_openButtonActionPerformed

    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionPerformed
        try {
            KeyStoreEntry entry = (KeyStoreEntry)list.getSelectedValue();
            if (entry != null) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Delete entry " + entry.getAlias() + "?",
                        "Delete confirmation", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    keystore.deleteEntry(entry.getAlias());
                    changed = true;
                    DefaultListModel model = (DefaultListModel)list.getModel();
                    model.remove(list.getSelectedIndex());
                }
            }
        } catch (GeneralSecurityException e) {
            JOptionPane.showMessageDialog(this, "Could create keystore",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_deleteActionPerformed

    private void sshEncodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sshEncodeActionPerformed
        try {
            KeyStoreEntry entry = (KeyStoreEntry)list.getSelectedValue();
            Certificate cert = keystore.getCertificate(
                                entry.getAlias());
            if (cert != null) {
                PublicKey pub = cert.getPublicKey();
                String encoded = SSHEncoding.encode(pub, entry.getAlias());
                SSHEncodingDialog.doDialog(this, encoded);
            }
        } catch (GeneralSecurityException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid certificate in keystore", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_sshEncodeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu certMenu;
    private javax.swing.JMenuItem delete;
    private javax.swing.JLabel endDate;
    private javax.swing.JMenuItem export;
    private javax.swing.JButton exportButton;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton genKeyButton;
    private javax.swing.JMenuItem genKeyItem;
    private javax.swing.JMenuItem impor;
    private javax.swing.JButton importButton;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JTextArea issuer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JList list;
    private javax.swing.JScrollPane listScroll;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton newButton;
    private javax.swing.JMenuItem newStoreItem;
    private javax.swing.JButton openButton;
    private javax.swing.JMenuItem openStoreItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JMenuItem saveStoreAsItem;
    private javax.swing.JMenuItem saveStoreItem;
    private javax.swing.JLabel serialNumber;
    private javax.swing.JLabel serialNumberHex;
    private javax.swing.JButton signButton;
    private javax.swing.JMenuItem signItem;
    private javax.swing.JMenuItem sshEncode;
    private javax.swing.JLabel startDate;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTextArea subject;
    private javax.swing.JPanel toolbar;
    // End of variables declaration//GEN-END:variables
    
}
