package org.opennms.web;

import java.util.regex.Pattern;

public class WebSecurityUtils {
	
	private final static Pattern ILLEGAL_IN_INTEGER = Pattern.compile("[^0-9]");
	
	private final static Pattern ILLEGAL_IN_FLOAT = Pattern.compile("[^0-9.]");
	
	private final static Pattern ILLEGAL_IN_COLUMN_NAME_PATTERN = Pattern.compile("[^A-Za-z0-9_]");
	
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
