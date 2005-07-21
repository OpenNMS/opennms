//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class StreamUtils {

    /**
     * Convenience method for reading data from a <code>Reader</code> and then
     * immediately writing that data to a <code>Writer</code> with a default
     * buffer size of one kilobyte (1,024 chars).
     * 
     * @param in
     *            a data source
     * @param out
     *            a data sink
     */
    public static void streamToStream(Reader in, Writer out) throws IOException {
        streamToStream(in, out, 1024);
    }

    /**
     * Convenience method for reading data from a <code>Reader</code> and then
     * immediately writing that data to a <code>Writer</code>.
     * 
     * @param in
     *            a data source
     * @param out
     *            a data sink
     * @param bufferSize
     *            the size of the <code>char</code> buffer to use for each
     *            read/write
     */
    public static void streamToStream(Reader in, Writer out, int bufferSize) throws IOException {
        if (in == null || out == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
    
        if (bufferSize < 1) {
            throw new IllegalArgumentException("Cannot take negative buffer size.");
        }
    
        char[] b = new char[bufferSize];
        int length;
    
        while ((length = in.read(b)) != -1) {
            out.write(b, 0, length);
        }
    }

    /**
     * Convenience method for reading data from an <code>InputStream</code>
     * and then immediately writing that data to an <code>OutputStream</code>
     * with a default buffer size of one kilobyte (1,024 bytes).
     * 
     * @param in
     *            a data source
     * @param out
     *            a data sink
     */
    public static void streamToStream(InputStream in, OutputStream out) throws IOException {
        streamToStream(in, out, 1024);
    }

    /**
     * Convenience method for reading data from an <code>InputStream</code>
     * and then immediately writing that data to an <code>OutputStream</code>.
     * 
     * @param in
     *            a data source
     * @param out
     *            a data sink
     * @param bufferSize
     *            the size of the <code>byte</code> buffer to use for each
     *            read/write
     */
    public static void streamToStream(InputStream in, OutputStream out, int bufferSize) throws IOException {
        byte[] b = new byte[bufferSize];
        int length;
    
        while ((length = in.read(b)) != -1) {
            out.write(b, 0, length);
        }
    }

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
     * @throws IllegalArgumentException
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
        ArrayList arrayList = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(s);
    
        while (tokenizer.hasMoreTokens()) {
            arrayList.add(tokenizer.nextElement());
        }
    
        // put the strings in the arraylist into a string[]
        String[] list = (String[]) arrayList.toArray(new String[arrayList.size()]);
    
        // change all the delim characters back to spaces
        for (int i = 0; i < list.length; i++) {
            list[i] = list[i].replace(delim, ' ');
        }
    
        return list;
    }

}
