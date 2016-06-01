/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import javax.swing.filechooser.FileSystemView;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public abstract class StringUtils {
    private static final boolean HEADLESS = Boolean.getBoolean("java.awt.headless");
    private static final Pattern WINDOWS_DRIVE = Pattern.compile("^[A-Za-z]\\:\\\\");

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
     * @deprecated Use createCommandArray(String s) instead.
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
        return createCommandArray(s);
    }

    /**
     * Convenience method for creating arrays of strings suitable for use as
     * command-line parameters when executing an external process.
     *
     * <p>
     * The default {@link Runtime#exec Runtime.exec} method will split a single
     * string based on spaces, but it does not respect spaces within quotation
     * marks, and it will leave the quotation marks in the resulting substrings.
     * This method solves those problems by preserving all in-quote spaces.
     * </p>
     *
     * <p>
     * <em>Caveat:</em> This method does not respect escaped quotes! It will
     * simply remove them and leave the stray escape characters.
     * </p>
     *
     * @param s
     *            the string to split
     * @return An array of strings split by spaces outside of quotes.
     * @throws java.lang.IllegalArgumentException
     *             If <code>s</code> is null.
     */
    public static String[] createCommandArray(String s) {
        return new CommandArrayGenerator(s).getCommandArray();
    }

    private static class CommandArrayGenerator {
        private final ArrayList<String> m_segments = new ArrayList<String>();
        private boolean m_isInQuotes = false;
        private StringBuffer m_segmentBuffer = new StringBuffer();

        public CommandArrayGenerator(String s) {
            if (s == null) {
                throw new IllegalArgumentException("Cannot take null parameters.");
            }

            // Visit the string char. by char. in order,
            // splitting it into segments
            for (char c : s.toCharArray()) {
                onChar(c);
            }

            // Make sure to keep any trailing characters
            resetSegment();
        }

        private void onChar(char c) {
            if (c == '"') {
                m_isInQuotes = !m_isInQuotes; // Toggle
            } else if (isWhitespace(c)) {
                if (!m_isInQuotes) {
                    // Reset the segment if we reach a whitespace
                    // character outside of quotes
                    resetSegment();
                } else if (c == ' ') {
                    // Preserve any spaces within a quoted segment
                    m_segmentBuffer.append(c);
                } else {
                    // Reset the segment if any other whitespace
                    // characters are reached in a quoted segment
                    // (do this in order to preserve existing behavior )
                    resetSegment();
                }
            } else {
                m_segmentBuffer.append(c);
            }
        }

        private void resetSegment() {
            // Reset the segment if the buffer is not empty
            if (m_segmentBuffer.length() > 0) {
                m_segments.add(m_segmentBuffer.toString());
                m_segmentBuffer = new StringBuffer();
            }
        }

        public String[] getCommandArray() {
            return m_segments.toArray(new String[m_segments.size()]);
        }

        private static boolean isWhitespace(char aChar) {
            switch(aChar) {
            case ' ':
            case '\t':
            case '\n':
            case '\r':
            case '\f':
                return true;
            default:
                return false;
            }
        }
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
    	if (HEADLESS) {
    		return WINDOWS_DRIVE.matcher(drive).matches();
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
