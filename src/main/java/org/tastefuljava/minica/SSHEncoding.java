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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.util.encoders.Base64;

public class SSHEncoding {
    private static final Logger LOG
            = Logger.getLogger(SSHEncoding.class.getName());

    /**
     * Private constructor to prevent instantiation
     */
    private SSHEncoding() {
    }

    public static String encode(PublicKey key, String comment) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String type;
            if (key instanceof RSAPublicKey) {
                type = "ssh-rsa";
                writeBytes(type.getBytes("US-ASCII"), baos);
                writeRSAKey((RSAPublicKey)key, baos);
            } else if (key instanceof DSAPublicKey) {
                type = "ssh-dss";
                writeBytes(type.getBytes("US-ASCII"), baos);
                writeDSAKey((DSAPublicKey)key, baos);
            } else {
                throw new RuntimeException("Unsupported key type");
            }
            StringBuilder buf = new StringBuilder();
            byte[] bytes = Base64.encode(baos.toByteArray());
            buf.append(type);
            buf.append(' ');
            buf.append(new String(bytes,"US-ASCII"));
            if (comment != null) {
                buf.append(' ');
                buf.append(comment);
            }
            return buf.toString();
        } catch (IOException ex) {
            // very unlikely
            LOG.log(Level.SEVERE, "Error encoding key", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private static void writeDSAKey(DSAPublicKey key, OutputStream os)
            throws IOException {
        writeBytes(key.getParams().getP().toByteArray(), os);
        writeBytes(key.getParams().getQ().toByteArray(), os);
        writeBytes(key.getParams().getG().toByteArray(), os);
        writeBytes(key.getY().toByteArray(), os);
    }

    private static void writeRSAKey(RSAPublicKey key, OutputStream os)
            throws IOException {
        writeBytes(key.getPublicExponent().toByteArray(), os);
        writeBytes(key.getModulus().toByteArray(), os);
    }

    private static void writeBytes(byte[] str, OutputStream os)
            throws IOException {
        for (int shift = 24; shift >= 0; shift -= 8) {
            os.write((str.length >>> shift) & 0xFF);
        }
        os.write(str);
    }
}
