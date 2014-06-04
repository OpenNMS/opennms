/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public abstract class StringUtils {
    final static boolean s_headless = Boolean.getBoolean("java.awt.headless");
    final static Pattern s_windowsDrive = Pattern.compile("^[A-Za-z]\\:\\\\");

    /**
     * Convenience method for creating arrays of strings suitable for use as
     * command-line parameters when executing an external process.
     *
     * <p>
     * The default {@link Runtime#exec Runtime.exec}method will split a single
     * string based on spaces, but it does not respect spaces within quotation
     * marks, and it will leave the quotation marks in the resulting substrings.
     * This method solves those problems by replacing all in-quote spaces with
     * the given delimiter, removes the quotes, and then splits the resulting
     * string by the remaining out-of-quote spaces. It then goes through each
     * substring and replaces the delimiters with spaces.
     * </p>
     *
     * <p>
     * <em>Caveat:</em> This method does not respect escaped quotes! It will
     * simply remove them and leave the stray escape characters.
     * </p>
     *
     * @param s
     *            the string to split
     * @param delim
     *            a char that does not already exist in <code>s</code>
     * @return An array of strings split by spaces outside of quotes.
     * @throws java.lang.IllegalArgumentException
     *             If <code>s</code> is null or if <code>delim</code>
     *             already exists in <code>s</code>.
     */
    public static String[] createCommandArray(String s, char delim) {
        if (s == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
    
        if (s.indexOf(delim) != -1) {
            throw new IllegalArgumentException("String parameter cannot already contain delimiter character: " + delim);
        }
    
        char[] chars = s.toCharArray();
        boolean inquote = false;
        StringBuffer buffer = new StringBuffer();
    
        // append each char to a StringBuffer, but
        // leave out quote chars and replace spaces
        // inside quotes with the delim char
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '"') {
                inquote = (inquote) ? false : true;
            } else if (inquote && chars[i] == ' ') {
                buffer.append(delim);
            } else {
                buffer.append(chars[i]);
            }
        }
    
        s = buffer.toString();
    
        // split the new string by the whitespaces that were not in quotes
        ArrayList<String> arrayList = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(s);
    
        while (tokenizer.hasMoreTokens()) {
            arrayList.add(tokenizer.nextElement().toString());
        }
    
        // put the strings in the arraylist into a string[]
        String[] list = arrayList.toArray(new String[arrayList.size()]);
    
        // change all the delim characters back to spaces
        for (int i = 0; i < list.length; i++) {
            list[i] = list[i].replace(delim, ' ');
        }
    
        return list;
    }

    /**
     * <p>truncate</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param length a int.
     * @return a {@link java.lang.String} object.
     */
    public static String truncate(String name, int length) {
        if (name.length() <= length) return name;
        return name.substring(0, length);
    }

    public static boolean isLocalWindowsPath(final String path) {
    	if (File.separatorChar != '\\') return false;
    	if (path.length() < 3) return false;

    	final char colon = path.charAt(1);
    	final char slash = path.charAt(2);
    	
    	if (colon != ':') return false;
    	if (slash != '\\' && slash != '/') return false;

    	final String drive = path.substring(0, 3);
    	if (s_headless) {
    		return s_windowsDrive.matcher(drive).matches();
    	} else {
    		final File file = new File(drive);
        	return FileSystemView.getFileSystemView().isFileSystemRoot(file);
    	}
    }

    /**
     * Uses the Xalan javax.transform classes to indent an XML string properly
     * so that it is easier to read.
     */
    public static String prettyXml(String xml) throws UnsupportedEncodingException, TransformerException {
        StringWriter out = new StringWriter();

        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer  = transFactory.newTransformer();

        // Set options on the transformer so that it will indent the XML properly
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StreamResult result = new StreamResult(out);
        Source source = new StreamSource(new ByteArrayInputStream(xml.getBytes("UTF-8")));

        // Run the transformer to put the XML into the StringWriter
        transformer.transform(source, result);

        return out.toString().trim();
    }
}
