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

import javax.security.auth.x500.X500Principal;

public class X500PrincipalBuilder {
    private final StringBuilder buf = new StringBuilder();

    public void add(String attr, String value) {
        if (buf.length() > 0) {
            buf.append(',');
        }
        buf.append(attr);
        buf.append('=');
        boolean wasSpace = false;
        int start = buf.length();
        for (char c: value.toCharArray()) {
            if (wasSpace) {
                buf.append(' ');
                wasSpace = false;
            }
            switch (c) {
                case ' ':
                    if (buf.length() == start) {
                        buf.append('\\');
                    } else {
                        wasSpace = true;
                        continue;
                    }
                    break;
                case '#':
                    if (buf.length() == start) {
                        buf.append('\\');
                    }
                    break;
                case ',':
                case '+':
                case '"':
                case '\\':
                case '<':
                case '>':
                case ';':
                    buf.append('\\');
            }
            buf.append(c);
        }
    }

    public X500Principal build() {
        return new X500Principal(buf.toString());
    }
}
