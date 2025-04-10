/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.utils;

import java.util.regex.Pattern;

import org.owasp.encoder.Encode;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 * <p>WebSecurityUtils class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 */
public abstract class WebSecurityUtils {
	
	private static final Pattern ILLEGAL_IN_INTEGER = Pattern.compile("[^0-9+-]");
	
	private static final Pattern ILLEGAL_IN_FLOAT = Pattern.compile("[^0-9.Ee+-]");
	
	private static final Pattern ILLEGAL_IN_COLUMN_NAME_PATTERN = Pattern.compile("[^A-Za-z0-9_]");

	private static final PolicyFactory s_sanitizer = Sanitizers
			// Allows common formatting elements including <b>, <i>, etc.
			.FORMATTING
			// Allows common block elements including <p>, <h1>, etc.
			.and(Sanitizers.BLOCKS)
			// Allows <img> elements from HTTP, HTTPS, and relative sources.
			.and(Sanitizers.IMAGES)
			// Allows HTTP, HTTPS, MAILTO, and relative links.
			.and(Sanitizers.LINKS)
			// Allows certain safe CSS properties in style="..." attributes.
			.and(Sanitizers.STYLES)
			// Allows common table elements.
			.and(Sanitizers.TABLES);

    /**
     * <p>sanitizeString</p>
     *
     * @param raw an array of {@link java.lang.String} objects.
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[] sanitizeString(String[] raw) {
        for (int i = 0; i < raw.length; i++) {
            raw[i] = sanitizeString(raw[i]);
        }
        return raw;
    }
    
    /**
     * <p>sanitizeString</p>
     *
     * @param raw a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String sanitizeString(String raw) {
        return sanitizeString(raw, false);
    }
    
    /**
     * <p>sanitizeString</p>
     *
     * @param raw a {@link java.lang.String} object.
     * @param allowHTML a boolean.
     * @return a {@link java.lang.String} object.
     */
    public static String sanitizeString(String raw, boolean allowHTML)
    {
        if (raw==null || raw.length()==0) {
            return raw;
        }
        String next;

        if (allowHTML) {
			next = s_sanitizer.sanitize(raw);
        } else {
            next = Encode.forHtml(raw);
        }
        return next;
    }

    /**
     * <p>safeParseInt</p>
     *
     * @param dirty an array of {@link java.lang.String} objects.
     * @return an array of int.
     * @throws java.lang.NumberFormatException if any.
     */
    public static int[] safeParseInt(String[] dirty) throws NumberFormatException {
        final int[] clean = new int[dirty.length];
        String cleanString;
        for (int i = 0; i < dirty.length; i++) {
            cleanString = ILLEGAL_IN_INTEGER.matcher(dirty[i]).replaceAll("");
            clean[i] = Integer.parseInt(cleanString);
        }
        return clean;
    }

	public static long[] safeParseLong(String[] dirty) throws NumberFormatException {
		final long[] clean = new long[dirty.length];
		String cleanString;
		for (int i = 0; i < dirty.length; i++) {
			cleanString = ILLEGAL_IN_INTEGER.matcher(dirty[i]).replaceAll("");
			clean[i] = Long.parseLong(cleanString);
		}
		return clean;
	}

	/**
	 * <p>safeParseInt</p>
	 *
	 * @param dirty a {@link java.lang.String} object.
	 * @return a int.
	 * @throws java.lang.NumberFormatException if any.
	 */
	public static int safeParseInt(String dirty) throws NumberFormatException {
		if (dirty == null) {
			throw new NumberFormatException("String value of integer was null");
		}
		String clean = ILLEGAL_IN_INTEGER.matcher(dirty).replaceAll("");
		return Integer.parseInt(clean);
	}
	
	/**
	 * <p>safeParseLong</p>
	 *
	 * @param dirty a {@link java.lang.String} object.
	 * @return a long.
	 * @throws java.lang.NumberFormatException if any.
	 */
	public static long safeParseLong(String dirty) throws NumberFormatException {
		if (dirty == null) {
			throw new NumberFormatException("String value of long integer was null");
		}
		String clean = ILLEGAL_IN_INTEGER.matcher(dirty).replaceAll("");
		return Long.parseLong(clean);
	}
	
	/**
	 * <p>safeParseFloat</p>
	 *
	 * @param dirty a {@link java.lang.String} object.
	 * @return a float.
	 * @throws java.lang.NumberFormatException if any.
	 */
	public static float safeParseFloat(String dirty) throws NumberFormatException {
		String clean = ILLEGAL_IN_FLOAT.matcher(dirty).replaceAll("");
		return Float.parseFloat(clean);
	}
	
	/**
	 * <p>safeParseDouble</p>
	 *
	 * @param dirty a {@link java.lang.String} object.
	 * @return a double.
	 * @throws java.lang.NumberFormatException if any.
	 */
	public static double safeParseDouble(String dirty) throws NumberFormatException {
		String clean = ILLEGAL_IN_FLOAT.matcher(dirty).replaceAll("");
		return Double.parseDouble(clean);
	}
	
    /**
     * <p>sanitizeDbColumnName</p>
     *
     * @param dirty a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String sanitizeDbColumnName(String dirty) {
        return ILLEGAL_IN_COLUMN_NAME_PATTERN.matcher(dirty).replaceAll("");
    }

}
