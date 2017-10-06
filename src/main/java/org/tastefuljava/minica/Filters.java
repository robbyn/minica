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
import javax.swing.filechooser.FileFilter;

class Filters {
    static final FileFilter PEM_FILEFILTER;
    static final FileFilter PKCS12_FILEFILTER;
    static final FileFilter JKS_FILEFILTER;
    static final FileFilter CERT_FILEFILTER;

    static {
        PEM_FILEFILTER = new FileFilter() {
            @Override
            public String getDescription() {
                return "OpenSSL/OpenSSH files (*.pem)";
            }

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.isFile()
                        && file.getName().toLowerCase().endsWith(".pem");
            }
        };

        PKCS12_FILEFILTER = new FileFilter() {
            @Override
            public String getDescription() {
                return "PKCS12 files (*.p12)";
            }

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.isFile()
                        && file.getName().toLowerCase().endsWith(".p12");
            }
        };

        JKS_FILEFILTER = new FileFilter() {
            @Override
            public String getDescription() {
                return "JKS files (*.jks)";
            }

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.isFile()
                        && file.getName().toLowerCase().endsWith(".jks");
            }
        };

        CERT_FILEFILTER = new FileFilter() {
            @Override
            public String getDescription() {
                return "X509 certificate files (*.crt,*.cer,*.der)";
            }

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else if (!file.isFile()) {
                    return false;
                }
                String name = file.getName().toLowerCase();
                return name.endsWith(".crt") || name.endsWith(".cer")
                        || name.endsWith(".der");
            }
        };
    }
}
