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
// Modifications:
//
// 2007 Jun 23: Use Java 5 generics. - dj@opennms.org
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
package org.opennms.core.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Category;

public class StringUtils {

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

    public static String[] tokenizeWithQuotingAndEscapes(String line, String delims, boolean processQuoted) {
        return tokenizeWithQuotingAndEscapes(line, delims, processQuoted, "");
    }
    
    /**
     * Tokenize a {@link String} into an array of {@link String}s.
     * @param line
     *          the string to tokenize
     * @param delims
     *          a string containing zero or more characters to treat as a delimiter
     * @param processQuoted
     *          whether or not to process escaped values inside quotes
     * @param tokens
     *          custom escaped tokens to pass through, escaped.  For example, if tokens contains "lsg", then \l, \s, and \g
     *          will be passed through unescaped.
     * @return
     */
    public static String[] tokenizeWithQuotingAndEscapes(String line, String delims, boolean processQuoted, String tokens) {
        Category log = ThreadCategory.getInstance(StringUtils.class);
        List<String> tokenList = new LinkedList<String>();
    
        StringBuffer currToken = new StringBuffer();
        boolean quoting = false;
        boolean escaping = false;
        boolean debugTokens = Boolean.getBoolean("org.opennms.netmgt.rrd.debugTokens");
    
        if (debugTokens)
            log.debug("tokenize: line=" + line + " delims=" + delims);
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (debugTokens)
                log.debug("tokenize: checking char: " + ch);
            if (escaping) {
                if (ch == 'n') {
                    currToken.append(escapeIfNotPathSepInDEF(ch, '\n', currToken));
                } else if (ch == 'r') {
                    currToken.append(escapeIfNotPathSepInDEF(ch, '\r', currToken));
                } else if (ch == 't') {
                    currToken.append(escapeIfNotPathSepInDEF(ch, '\t', currToken));
                } else {
                    if (tokens.indexOf(ch) >= 0) {
                        currToken.append('\\').append(ch);
                    } else if (currToken.toString().startsWith("DEF:")) {
                        currToken.append('\\').append(ch);
                    } else {
                        // silently pass through the character *without* the \ in front of it
                        currToken.append(ch);
                    }
                }
                escaping = false;
                if (debugTokens)
                    log.debug("tokenize: escaped. appended to " + currToken);
            } else if (ch == '\\') {
                if (debugTokens)
                    log.debug("tokenize: found a backslash... escaping currToken = " + currToken);
                if (quoting && !processQuoted)
                    currToken.append(ch);
                else
                    escaping = true;
            } else if (ch == '\"') {
                if (!processQuoted)
                    currToken.append(ch);
                if (quoting) {
                    if (debugTokens)
                        log.debug("tokenize: found a quote ending quotation currToken = " + currToken);
                    quoting = false;
                } else {
                    if (debugTokens)
                        log.debug("tokenize: found a quote beginning quotation  currToken =" + currToken);
                    quoting = true;
                }
            } else if (!quoting && delims.indexOf(ch) >= 0) {
                if (debugTokens)
                    log.debug("tokenize: found a token: " + ch + " ending token [" + currToken + "] and starting a new one");
                tokenList.add(currToken.toString());
                currToken = new StringBuffer();
            } else {
                if (debugTokens)
                    log.debug("tokenize: appending " + ch + " to token: " + currToken);
                currToken.append(ch);
            }
    
        }
    
        if (escaping || quoting) {
            if (debugTokens)
                log.debug("tokenize: ended string but escaping = " + escaping + " and quoting = " + quoting);
            throw new IllegalArgumentException("unable to tokenize string " + line + " with token chars " + delims);
        }
    
        if (debugTokens)
            log.debug("tokenize: reached end of string.  completing token " + currToken);
        tokenList.add(currToken.toString());
    
        return (String[]) tokenList.toArray(new String[tokenList.size()]);
    }

    public static String truncate(String name, int length) {
        if (name.length() <= length) return name;
        return name.substring(0, length);
    }
    
    public static char[] escapeIfNotPathSepInDEF(char encountered, char escaped, StringBuffer currToken) {
    	if ( ('\\' != File.separatorChar) || (! currToken.toString().startsWith("DEF:")) ) {
    		return new char[] { escaped };
    	} else {
    		return new char[] { '\\', encountered };
    	}
    }

}
