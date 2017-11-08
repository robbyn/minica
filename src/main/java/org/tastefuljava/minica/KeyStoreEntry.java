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

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class KeyStoreEntry {
    public static final Comparator<KeyStoreEntry> TYPE_ALIAS_ORDER;
    public static final Comparator<KeyStoreEntry> ALIAS_ORDER;
    private static final Pattern RDN_PATTERN
            = Pattern.compile("^\\s*([^\\s,=]+)\\s*=\\s*([^,]*)\\s*(?:,(.*))?$");

    private final String alias;
    private final boolean key;

    public KeyStoreEntry(String alias, boolean key) {
        this.alias = alias;
        this.key = key;
    }

    public String subjectName(KeyStore keystore)
            throws KeyStoreException {
        X509Certificate cert = (X509Certificate)keystore.getCertificate(alias);
        String name = cert.getSubjectX500Principal().getName();
        while (name != null) {
            Matcher matcher = RDN_PATTERN.matcher(name);
            if (!matcher.matches()) {
                break;
            }
            String key = matcher.group(1);
            String value = matcher.group(2);
            name = matcher.group(3);
            if (key.equalsIgnoreCase("cn")) {
                return value;
            }
        }
        return alias;
    }

    public X509Certificate getCertificate(KeyStore keystore)
            throws KeyStoreException {
        return (X509Certificate) keystore.getCertificate(alias);
    }

    public PrivateKey getPrivateKey(KeyStore keystore, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException {
        return (PrivateKey)keystore.getKey(alias, password);
    }

    public static KeyStoreEntry[] getAll(KeyStore keystore)
            throws KeyStoreException {
        return getAll(keystore, true, true);
    }

    public static KeyStoreEntry[] getAllKeys(KeyStore keystore)
            throws KeyStoreException {
        return getAll(keystore, true, false);
    }

    public static KeyStoreEntry[] getAllCerts(KeyStore keystore)
            throws KeyStoreException {
        return getAll(keystore, false, true);
    }

    private static KeyStoreEntry[] getAll(KeyStore keystore, boolean keys,
            boolean certs) throws KeyStoreException {
        List<KeyStoreEntry> entries = new ArrayList<>();
        for (Enumeration enm = keystore.aliases(); enm.hasMoreElements(); ) {
            String alias = (String)enm.nextElement();
            boolean key = keystore.isKeyEntry(alias);
            if (key ? keys : certs) {
                entries.add(new KeyStoreEntry(alias, key));
            }
        }
        return entries.toArray(new KeyStoreEntry[entries.size()]);
    }

    public String getAlias() {
        return alias;
    }

    public boolean isKey() {
        return key;
    }

    public KeyStoreEntry copy(String newName) {
        throw new UnsupportedOperationException("clone not implemented");
    }

    public void addTo(KeyStore ks, char[] password) throws KeyStoreException {
        throw new UnsupportedOperationException("addTo not implemented");
    }

    @Override
    public int hashCode() {
        return alias.hashCode()*2 + (key ? 1 : 0);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other == null) {
            return false;
        } else if (other.getClass() != getClass()) {
            return false;
        } else {
            KeyStoreEntry oe = (KeyStoreEntry)other;
            return key == oe.key && alias.equalsIgnoreCase(oe.alias);
        }
    }

    static {
        TYPE_ALIAS_ORDER = (KeyStoreEntry e1, KeyStoreEntry e2) -> {
            if (e1 == e2) {
                return 0;
            } else if (e1 == null) {
                return -1;
            } else if (e2 == null) {
                return 1;
            } else {
                if (e1.isKey() && !e2.isKey()) {
                    return -1;
                } else if (!e1.isKey() && e2.isKey()) {
                    return 1;
                } else {
                    return e1.getAlias().compareToIgnoreCase(e2.getAlias());
                }
            }
        };

        ALIAS_ORDER = (KeyStoreEntry e1, KeyStoreEntry e2) -> {
            if (e1 == e2) {
                return 0;
            } else if (e1 == null) {
                return -1;
            } else if (e2 == null) {
                return 1;
            } else {
                return e1.getAlias().compareToIgnoreCase(e2.getAlias());
            }
        };
    }
}
