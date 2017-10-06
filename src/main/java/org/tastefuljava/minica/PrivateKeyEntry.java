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
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class PrivateKeyEntry extends KeyStoreEntry {
    private final PrivateKey key;
    private final X509Certificate chain[];

    public PrivateKeyEntry(String alias, PrivateKey key,
            X509Certificate chain[]) {
        super(alias, true);
        this.key = key;
        this.chain = chain;
    }

    public PrivateKey getKey() {
        return key;
    }

    public X509Certificate[] getChain() {
        return chain;
    }

    @Override
    public KeyStoreEntry copy(String newName) {
        return new PrivateKeyEntry(newName, key, chain);
    }

    @Override
    public void addTo(KeyStore ks, char[] password) throws KeyStoreException {
        ks.setKeyEntry(getAlias(), key,
                        password, chain);
    }
}
