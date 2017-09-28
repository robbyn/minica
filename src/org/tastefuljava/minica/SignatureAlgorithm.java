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

public enum SignatureAlgorithm {
    SHA1withRSA("SHA1", "RSA"),
    MD2withRSA("MD2", "RSA"),
    MD5withRSA("MD5", "RSA"),
    SHA1withDSA("SHA1", "DSA"),
    SHA256withRSA("SHA256", "RSA"),
    SHA256withECDSA("SHA256", "ECDSA");

    private String digest;
    private String crypto;

    private SignatureAlgorithm(String digest, String crypto) {
        this.digest = digest;
        this.crypto = crypto;
    }

    public static SignatureAlgorithm[] getForCrypto(String crypto) {
        List<SignatureAlgorithm> list = new ArrayList<SignatureAlgorithm>();
        for (SignatureAlgorithm alg: values()) {
            if (crypto.equalsIgnoreCase(alg.crypto)) {
                list.add(alg);
            }
        }
        return list.toArray(
                new SignatureAlgorithm[list.size()]);
    }

    @Override
    public String toString() {
        return digest + "/" + crypto;
    }

    public String getDigest() {
        return digest;
    }

    public String getCrypto() {
        return crypto;
    }
}
