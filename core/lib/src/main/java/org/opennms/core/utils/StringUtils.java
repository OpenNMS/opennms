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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StringUtils {

    private static final Logger LOG = LoggerFactory.getLogger(StringUtils.class);

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
        private final ArrayList<String> m_segments = new ArrayList<>();
        private boolean m_isInQuotes = false;
        private StringBuilder m_segmentBuffer = new StringBuilder();

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
                m_segmentBuffer = new StringBuilder();
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
    public static String prettyXml(String xml) throws TransformerException {
        StringWriter out = new StringWriter();

        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer  = transFactory.newTransformer();

        // Set options on the transformer so that it will indent the XML properly
        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StreamResult result = new StreamResult(out);
        Source source = new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        // Run the transformer to put the XML into the StringWriter
        transformer.transform(source, result);

        return out.toString().trim();
    }
    
    public static String iso8601LocalOffsetString(Date d) {
        return iso8601OffsetString(d, ZoneId.systemDefault(), null);
    }
    
    public static String iso8601OffsetString(Date d, ZoneId zone, ChronoUnit truncateTo) {
        ZonedDateTime zdt = ((d).toInstant())
                .atZone(zone);
        if(truncateTo != null) {
            zdt = zdt.truncatedTo(truncateTo);
        }
        return zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static String stripExtraQuotes(String string) {
        return string.replaceAll("^\"(.*)\"$", "$1");
    }

    /**
     * This is an optimized version of:
     *    return a != null && a.trim().equals(b)
     *
     * that avoids creating a trimmed substring of A before
     * comparison.
     *
     * Instead A and B are compared in place.
     *
     * @param a string to trim before comparing
     * @param b string to compare
     * @return <code>true</code> if A equals B, after A is trimmed
     */
    public static boolean equalsTrimmed(String a, String b) {
        if (a == null) {
            return false;
        }

        int alen = a.length();
        final int blen = b.length();

        // Fail fast: If B is longer than A, B cannot be a substring of A
        if (blen > alen) {
            return false;
        }

        // Find the index of the first non-whitespace character in A
        int i = 0;
        while ((i < alen) && (a.charAt(i) <= ' ')) {
            i++;
        }

        // Match the subsequent characters in A to those in B
        int j = 0;
        while ((i < alen && j < blen)) {
            if (a.charAt(i) != b.charAt(j)) {
                return false;
            }
            i++;
            j++;
        }

        // If we've reached the end of A, then we have a match
        if (i == alen) {
            return true;
        }

        // "Trim" the whitespace characters off the end of A 
        while ((i < alen) && (a.charAt(alen - 1) <= ' ')) {
            alen--;
        }

        // If only whitespace characters remained on A, then we have a match
        if (alen - i == 0) {
            return true;
        }

        // There are extra characters at the tail of A, that don't show up in B
        return false;
    }

    public static boolean isEmpty(final String text) {
        return text == null || text.trim().length() == 0;
    }

    public static boolean hasText(final String text) {
        return text != null && text.trim().length() > 0;
    }

    /**
     * <p>NMS-9091: This method calls {@link Date#toString()} but then calls
     * {@link Date#setTime(long)} so that internally, the {@link Date#cdate}
     * field is deallocated. This saves significant heap space for {@link Date} 
     * instances that are stored in long-lived collections.</p>
     * 
     * <ul>
     * <li>java.util.Date with only fastTime: 24 bytes</li>
     * <li>java.util.Date with fastTime and cdate: 120 bytes</li>
     * </ul>
     * 
     * @param date
     * @return Value of date.toString()
     */
    public static String toStringEfficiently(final Date date) {
        final long time = date.getTime();
        final String retval = date.toString();
        date.setTime(time);
        return retval;
    }

    public static Integer parseDecimalInt(String value) {
        return parseDecimalInt(value, true);
    }

    /**
     * This is a quick and dirty parser for String representations
     * of decimal integers. It should be up to 2X faster than
     * {@link Integer#parseInt(String)}.
     * 
     * @param value Positive or negative decimal string value
     * @return Integer representing the string value
     */
    public static Integer parseDecimalInt(String value, boolean throwExceptions) {
        final int length = value.length();

        if (value == null || length < 1) {
            if (throwExceptions) {
                throw new NumberFormatException("Null or empty value");
            } else {
                return null;
            }
        }

        try {
            int sign = -1;
            int i = 0;

            if (value.charAt(0) == '-') {
                if (length == 1) {
                    if (throwExceptions) {
                        throw new NumberFormatException("No digits in value: " + value);
                    } else {
                        return null;
                    }
                }
                sign = 1;
                i = 1;
            }

            int retval = 0;
            int oldValue;
            int digit;

            for (; i < length; i++) {
                oldValue = retval;
                final char current = value.charAt(i);
                digit = (current - '0');
                if (digit < 0 || digit > 9) {
                    if (throwExceptions) {
                        throw new NumberFormatException("Invalid digit: " + current);
                    } else {
                        return null;
                    }
                }
                retval = (retval * 10) - digit;
                // If the negative value overflows to positive, then throw an exception
                if (retval > oldValue) {
                    if (throwExceptions) {
                        throw new NumberFormatException(sign == -1 ? "Overflow" : "Underflow");
                    } else {
                        return null;
                    }
                }
            }
            return sign * retval;
        } catch (Exception e) {
            if (throwExceptions) {
                NumberFormatException nfe = new NumberFormatException("Could not parse integer value: " + value);
                nfe.initCause(e);
                throw nfe;
            } else {
                return null;
            }
        }
    }
}
