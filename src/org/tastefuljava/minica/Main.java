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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Main {
    private static final Logger LOG
            = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            for (Provider p: Security.getProviders()) {
                System.out.println(p.getName());
                for (Service s: p.getServices()) {
                    System.out.println("    "
                            + s.getType() + ": "+ s.getAlgorithm());
                }
            }
            new MainFrame().setVisible(true);
        } catch (IOException | GeneralSecurityException e) {
            LOG.log(Level.SEVERE, "Exception in main", e);
        }
    }
}
