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

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration implements Serializable {
    private static final Logger LOG
            = Logger.getLogger(Configuration.class.getName());
    private static final long serialVersionUID = 3257852077869839415L;

    private static final DecimalFormat NUMBER_FORMAT;

    private Properties props = new Properties();

    public Configuration() {
    }

    public Configuration(Properties props) {
        this.props.putAll(props);
    }

    public Configuration(Configuration conf) {
        this(conf.props);
    }

    public String[] getNames() {
        return (String[])props.keySet().toArray(new String[props.size()]);
    }

    public void load(File file) throws IOException {
        props = new Properties();
        if (file.exists()) {
            InputStream in = new FileInputStream(file);
            try {
                props.load(in);
            } finally {
                in.close();
            }
        }
    }


    public void load(String fileName) throws IOException {
        load(new File(fileName));
    }


    public void store(File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            props.store(out, "configuration");
        } finally {
            out.close();
        }
    }


    public void store(String fileName) throws IOException {
        store(new File(fileName));
    }


    public boolean getBoolean(String name, boolean def) {
        String value = getString(name, null);
        return isBlank(value) ? def : value.equals("true");
    }


    public char getChar(String name, char def) {
        String value = getString(name, null);
        return isBlank(value) ? def : value.charAt(0);
    }


    public short getShort(String name, short def) {
        try {
            String value = getString(name, null);
            return isBlank(value) ? def : Short.parseShort(value);
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Invalid number", e);
            return def;
        }
    }


    public int getInt(String name, int def) {
        try {
            String value = getString(name, null);
            return isBlank(value) ? def : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Invalid number", e);
            return def;
        }
    }


    public long getLong(String name, long def) {
        try {
            String value = getString(name, null);
            return isBlank(value) ? def : Long.parseLong(value);
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Invalid number", e);
            return def;
        }
    }


    public float getFloat(String name, float def) {
        try {
            String value = getString(name, null);
            return isBlank(value) ? def : Float.parseFloat(value);
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Invalid number", e);
            return def;
        }
    }


    public double getDouble(String name, double def) {
        String value = getString(name, null);
        return isBlank(value) ? def : str2dbl(value);
    }


    public String getString(String name, String def) {
        return props.getProperty(name, def);
    }


    public Color getColor(String name, Color def) {
        int rgb = getInt(name, def.getRGB());
        return new Color(rgb);
    }


    public void setBoolean(String name, boolean val) {
        setString(name, val ? "true" : "false");
    }


    public void setChar(String name, char val) {
        setString(name, new String(new char[] {val}));
    }


    public void setShort(String name, short val) {
        setString(name, Short.toString(val));
    }


    public void setInt(String name, int val) {
        setString(name, Integer.toString(val));
    }


    public void setLong(String name, long val) {
        setString(name, Long.toString(val));
    }


    public void setFloat(String name, float val) {
        setString(name, NUMBER_FORMAT.format(val));
    }


    public void setDouble(String name, double val) {
        setString(name, NUMBER_FORMAT.format(val));
    }


    public void setString(String name, String val) {
        if (val != null) {
            props.setProperty(name, val);
        } else {
            props.remove(name);
        }
    }


    public void setColor(String name, Color val) {
        setInt(name, val.getRGB());
    }

    public void putAll(Configuration conf) {
        props.putAll(conf.props);
    }

    public static double str2dbl(String s) {
        if (isBlank(s)) {
            return 0;
        } else {
            try {
                return NUMBER_FORMAT.parse(s).doubleValue();
            } catch (ParseException e) {
                throw new NumberFormatException("Invalid number " + s);
            }
        }
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        NUMBER_FORMAT = new DecimalFormat("0.####");
        NUMBER_FORMAT.setDecimalFormatSymbols(symbols);
    }
}
