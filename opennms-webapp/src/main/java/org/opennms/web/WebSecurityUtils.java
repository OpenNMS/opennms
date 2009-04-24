/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 19, 2008
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class WebSecurityUtils {
	
	private final static Pattern ILLEGAL_IN_INTEGER = Pattern.compile("[^0-9+-]");
	
	private final static Pattern ILLEGAL_IN_FLOAT = Pattern.compile("[^0-9.+-]");
	
	private final static Pattern ILLEGAL_IN_COLUMN_NAME_PATTERN = Pattern.compile("[^A-Za-z0-9_]");
	
    private final static Pattern scriptPattern = Pattern.compile("script", Pattern.CASE_INSENSITIVE);

    public static String[] sanitizeString(String[] raw) {
        for (int i = 0; i < raw.length; i++) {
            raw[i] = sanitizeString(raw[i]);
        }
        return raw;
    }
    
    public static String sanitizeString(String raw)
    {
        if (raw==null || raw.length()==0) {
            return raw;
        }

        Matcher scriptMatcher = scriptPattern.matcher(raw);
        String next = scriptMatcher.replaceAll("&#x73;cript");
        return next.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    public static int[] safeParseInt(String[] dirty) throws NumberFormatException {
        final int[] clean = new int[dirty.length];
        String cleanString;
        for (int i = 0; i < dirty.length; i++) {
            cleanString = ILLEGAL_IN_INTEGER.matcher(dirty[i]).replaceAll("");
            clean[i] = Integer.parseInt(cleanString);
        }
        return clean;
    }

	public static int safeParseInt(String dirty) throws NumberFormatException {
		String clean = ILLEGAL_IN_INTEGER.matcher(dirty).replaceAll("");
		return Integer.parseInt(clean);
	}
	
	public static long safeParseLong(String dirty) throws NumberFormatException {
		String clean = ILLEGAL_IN_INTEGER.matcher(dirty).replaceAll("");
		return Long.parseLong(clean);
	}
	
	public static float safeParseFloat(String dirty) throws NumberFormatException {
		String clean = ILLEGAL_IN_FLOAT.matcher(dirty).replaceAll("");
		return Float.parseFloat(clean);
	}
	
	public static double safeParseDouble(String dirty) throws NumberFormatException {
		String clean = ILLEGAL_IN_FLOAT.matcher(dirty).replaceAll("");
		return Double.parseDouble(clean);
	}
	
    public static String sanitizeDbColumnName(String dirty) {
        return ILLEGAL_IN_COLUMN_NAME_PATTERN.matcher(dirty).replaceAll("");
    }

}
