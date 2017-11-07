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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JComponent;

public class Util {
    
    /** Private constructor to disallow instanciation */
    private Util() {
    }

    public static void adjustHeight(JComponent comp, int height) {
        Dimension size = comp.getMinimumSize();
        size.height = height;
        comp.setMinimumSize(size);
        size = comp.getPreferredSize();
        size.height = height;
        comp.setPreferredSize(size);        
    }

    public static void clearWidth(JComponent comp) {
        Dimension size = comp.getMinimumSize();
        size.width = 0;
        comp.setMinimumSize(size);
        size = comp.getPreferredSize();
        size.width = 0;
        comp.setPreferredSize(size);
    }

    public static void clearWidthAll(Container cont, Class clazz) {
        Component children[] = cont.getComponents();
        for (int i = 0; i < children.length; ++i) {
            Component child = children[i];
            if (clazz.isInstance(child)) {
                clearWidth((JComponent)child);
            }
            if (child instanceof Container) {
                clearWidthAll((Container)child, clazz);
            }
        }
    }

    public static String cleanupName(String s) {
        StringBuilder buf = new StringBuilder();
        for (char c: s.toCharArray()) {
            if (Character.isLetter(c) || Character.isDigit(c)
                    || c == '$' || c == '.' || c == '_' || c == '-') {
                buf.append(c);
            }
        }
        return buf.toString();
    }
}
