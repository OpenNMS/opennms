/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

/************************************************************************
 * Change history
 *
 * 2013-04-18 Updated package names to match new XMP JAR (jeffg@opennms.org)
 *
 ************************************************************************/

package org.opennms.netmgt.protocols.xmp;

import java.math.BigInteger;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.krupczak.xmp.Xmp;
import org.krupczak.xmp.XmpMessage;
import org.krupczak.xmp.XmpSession;
import org.krupczak.xmp.XmpVar;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;

public class XmpUtil {
    /** Constant <code>LESS_THAN="<"</code> */
    public static final String LESS_THAN = "<";
    /** Constant <code>GREATER_THAN=">"</code> */
    public static final String GREATER_THAN = ">";
    /** Constant <code>LESS_THAN_EQUALS="<="</code> */
    public static final String LESS_THAN_EQUALS = "<=";
    /** Constant <code>GREATER_THAN_EQUALS=">="</code> */
    public static final String GREATER_THAN_EQUALS = ">=";
    /** Constant <code>EQUALS="="</code> */
    public static final String EQUALS = "=";
    /** Constant <code>NOT_EQUAL="!="</code> */
    public static final String NOT_EQUAL = "!=";
    /** Constant <code>MATCHES="~"</code> */
    public static final String MATCHES = "~";
    
    private static boolean valueMeetsCriteria(XmpVar replyVar, String valueOperator, String valueOperand, ThreadCategory log, boolean caseSensitive)
            throws XmpUtilException {
        RE valueRegex = null;
        if (MATCHES.equals(valueOperator)) {
            try {
				valueRegex = new RE(valueOperand);
	            if (!caseSensitive) {
	            	valueRegex.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
	            }
            } catch (final RESyntaxException e) {
            	LogUtils.debugf(XmpUtil.class, e, "Unable to initialize regular expression.");
            }
        }
        
        if ((valueRegex != null) && valueRegex.match(replyVar.getValue())) {
            if (log.isDebugEnabled()) {
                log.debug("handleScalarQuery: Response value |" + replyVar.getValue() + "| matches, returning true");
            }
            return true;
        } else if ((MATCHES.equals(valueOperator)) && ((valueRegex == null) || ("".equals(valueRegex)))) {
            if (log.isDebugEnabled()) {
                log.debug("handleScalarQuery: Doing regex match but regex is null or empty, considering value |" + replyVar.getValue() + "| a match!");
            }
            return true;
        } else if (! MATCHES.equals(valueOperator)) {
            if (valueOperand == null) {
                if (log.isDebugEnabled()) {
                    log.debug("valueMeetsCriteria: operand is null, so any non-error reply will match");
                }
                if (replyVar.getValue() != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("valueMeetsCriteria: non-null reply value |" + replyVar.getValue() + "| considered a match");
                    }
                    return true;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("valueMeetsCriteria: null reply NOT considered a match");
                    }
                    return false;
                }
            } else if (valueOperand.matches("^-?[0-9]+$")) {
                if (log.isDebugEnabled()) {
                    log.debug("valueMeetsCriteria: operand " + valueOperand + " looks like an integer, treating with BigInteger");
                }
                BigInteger intOperand, intValue;
                try {
                    intValue = new BigInteger(replyVar.getValue());
                    intOperand = new BigInteger(valueOperand);
                } catch (NumberFormatException nfe) {
                    log.error("Failed to parse operand " + valueOperand + " or observed value " + replyVar.getValue() + " as a BigInteger, giving up");
                    log.info(nfe.getMessage());
                    throw new XmpUtilException("Operand '" + valueOperand + "' or observed value '" + replyVar.getValue() + "' is a malformed integer");
                }
                if (LESS_THAN.equals(valueOperator)) {
                    return (intValue.compareTo(intOperand) < 0);
                } else if (GREATER_THAN.equals(valueOperator)) {
                    return (intValue.compareTo(intOperand) > 0);
                } else if (LESS_THAN_EQUALS.equals(valueOperator)) {
                    return (intValue.compareTo(intOperand) <= 0);
                } else if (GREATER_THAN_EQUALS.equals(valueOperator)) { 
                    return (intValue.compareTo(intOperand) >= 0);
                } else if (EQUALS.equals(valueOperator)) {
                    return (intValue.compareTo(intOperand) == 0);
                } else if (NOT_EQUAL.equals(valueOperator)) {
                    return (intValue.compareTo(intOperand) != 0);
                } else {
                    log.error("Unknown value operator '" + valueOperator + "', giving up");
                    throw new XmpUtilException("Operator '" + valueOperator + "' unknown");
                }
            } else if (valueOperand.matches("^-?[0-9]+([,.]?[0-9]+)$")) {
                if (log.isDebugEnabled())
                    log.debug("valueMeetsCriteria: operand " + valueOperand + " looks like a float, treating with float");
                float floatValue, floatOperand;
                try {
                    floatValue = Float.parseFloat(valueOperand);
                    floatOperand = Float.parseFloat(valueOperand);
                } catch (NumberFormatException nfe) {
                    log.error("Failed to parse operand " + valueOperand + " or observed value " + replyVar.getValue() + " as a Float, giving up");
                    log.info(nfe.getMessage());
                    throw new XmpUtilException("Operand '" + valueOperand + "' or observed value '" + replyVar.getValue() + "' is a malformed floating-point number");
                }
                if (LESS_THAN.equals(valueOperator)) {
                    return (floatValue < floatOperand);
                } else if (GREATER_THAN.equals(valueOperator)) {
                    return (floatValue > floatOperand);
                } else if (LESS_THAN_EQUALS.equals(valueOperator)) {
                    return (floatValue <= floatOperand);
                } else if (GREATER_THAN_EQUALS.equals(valueOperator)) {
                    return (floatValue >= floatOperand);
                } else if (EQUALS.equals(valueOperator)) {
                    return (floatValue == floatOperand);
                } else if (NOT_EQUAL.equals(valueOperator)) {
                    return (floatValue != floatOperand);
                } else {
                    log.error("Unknown value operator '" + valueOperator + "', giving up");
                    throw new XmpUtilException("Value operator '" + valueOperator + "' unknown");
                }
            } else {
                if (log.isDebugEnabled())
                    log.debug("valueMeetsCriteria: operand " + valueOperand + " looks non-numeric, treating with String");
                if (!EQUALS.equals(valueOperator)) {
                    log.error("Value operator '" + valueOperator + "' does not apply for non-numeric value operand '" + valueOperand + "', giving up");
                    throw new XmpUtilException("Value operator '" + valueOperator + "' does not apply for non-numeric value operand '" + valueOperand + "'");
                }
                if (caseSensitive) {
				    return valueOperand.equals(replyVar.getValue());                        
				} else {
				    return valueOperand.equalsIgnoreCase(replyVar.getValue());
				}
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("handleScalarQuery: Response value |" + replyVar.getValue() + "| does not match for value operator |" + valueOperator +"| and value operand |" + valueOperand + "|, returning false");
                throw new XmpUtilException("Response value '" + replyVar.getValue() + "' does not match for value operator '" + valueOperator +"' and value operand '" + valueOperand +"'");
            }
        }
        return false;
    }

    /**
     * <p>handleScalarQuery</p>
     *
     * @param session a {@link org.krupczak.xmp.XmpSession} object.
     * @param mib a {@link java.lang.String} object.
     * @param object a {@link java.lang.String} object.
     * @param valueOperator a {@link java.lang.String} object.
     * @param valueOperand a {@link java.lang.String} object.
     * @param log a {@link org.opennms.core.utils.ThreadCategory} object.
     * @param caseSensitive a boolean.
     * @return a boolean.
     * @throws org.opennms.netmgt.protocols.xmp.XmpUtilException if any.
     */
    public static boolean handleScalarQuery(XmpSession session, String mib,
            String object, String valueOperator, String valueOperand, ThreadCategory log, boolean caseSensitive) throws XmpUtilException {
        XmpMessage reply;
        XmpVar[] queryVars = new XmpVar[1];
        XmpVar[] replyVars;
        
        queryVars[0] = new XmpVar(mib, object, Xmp.SYNTAX_NULLSYNTAX);
        
        reply = session.queryVars(queryVars);
        if (reply == null) {
            log.warn("handleScalarQuery: query for object " + object + " from MIB " + mib + " failed, " + Xmp.errorStatusToString(session.getErrorStatus()));
            return false;
        } else if (log.isDebugEnabled()) {
            log.debug("handleScalarQuery: query for object " + object + " from MIB " + mib + " succeeded.");
        }
        
        replyVars = reply.getMIBVars();
        if (replyVars[0].getMibName().equals(mib) && replyVars[0].getObjName().equals(object)) {
            return valueMeetsCriteria(replyVars[0], valueOperator, valueOperand, log, caseSensitive);
        } else {
            log.error("Observed MIB name (" + replyVars[0].getMibName() + ") or object name (" + replyVars[0].getObjName() + ") does not match specified MIB name (" + mib + ") or object name (" + object + "), giving up");
            throw new XmpUtilException("Received unexpected response (MIB: " + replyVars[0].getMibName() + " Object: " + replyVars[0].getObjName());
        }
    }

    /**
     * <p>handleTableQuery</p>
     *
     * @param session a {@link org.krupczak.xmp.XmpSession} object.
     * @param mib a {@link java.lang.String} object.
     * @param table a {@link java.lang.String} object.
     * @param object a {@link java.lang.String} object.
     * @param instance a {@link java.lang.String} object.
     * @param instanceRegex a {@link org.apache.regexp.RE} object.
     * @param valueOperator a {@link java.lang.String} object.
     * @param valueOperand a {@link java.lang.String} object.
     * @param minMatches a int.
     * @param maxMatches a int.
     * @param maxMatchesUnbounded a boolean.
     * @param log a {@link org.opennms.core.utils.ThreadCategory} object.
     * @param caseSensitive a boolean.
     * @return a boolean.
     * @throws org.opennms.netmgt.protocols.xmp.XmpUtilException if any.
     */
    public static boolean handleTableQuery(XmpSession session, String mib,
            String table, String object, String instance, RE instanceRegex, 
            String valueOperator, String valueOperand, int minMatches,
            int maxMatches, boolean maxMatchesUnbounded,
            ThreadCategory log, boolean caseSensitive) throws XmpUtilException {
        XmpMessage reply;
        String[] tableInfo = new String[3];
        XmpVar[] queryVars = new XmpVar[1];
        XmpVar[] replyVars;
        int numMatches = 0;
        
        queryVars[0] = new XmpVar(mib, object, Xmp.SYNTAX_NULLSYNTAX);
        
        tableInfo[0] = mib;
        tableInfo[1] = object;
        tableInfo[2] = instance;
        reply = session.queryTableVars(tableInfo, 0, queryVars);
        
        if (reply == null) {
            log.warn("handleTableQuery: query for object " + object + " from MIB " + mib + " failed, " + Xmp.errorStatusToString(session.getErrorStatus()));
            throw new XmpUtilException("XMP query failed (MIB " + mib + ", object " + object + "): " + Xmp.errorStatusToString(session.getErrorStatus()));
        }
        
        replyVars = reply.getMIBVars();
        if (log.isDebugEnabled()) {
            log.debug("handleTableQuery: Got reply with " + replyVars.length + " variables"); 
        }
        
        
        /* Since we're constrained to a single object, we know that there's
         * exactly one column in the result set and so can use a Java 5
         * for() loop. If there were multiple columns, we'd have to break the
         * flat array into a two-dimensional matrix using a pair of old-style
         * for() loops.
         */
        for (XmpVar thisVar : replyVars) {
            String rowInstance = thisVar.getKey();
            if ((instanceRegex != null) && (!instanceRegex.match(rowInstance))) {
                if (log.isDebugEnabled()) {
                    log.debug("handleTableQuery: instance " + rowInstance + " does not match, skipping this row.");
                }
                continue;  // to next var
            } else if (instanceRegex == null) {
                if (log.isDebugEnabled()) {
                    log.debug("handleTableQuery: instance match not specified, evaluating value of instance " + rowInstance);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("handleTableQuery: instance " + rowInstance + " matches, evaluating value");
                }
            }
            if (valueMeetsCriteria(thisVar, valueOperator, valueOperand, log, caseSensitive)) {
                numMatches++;
            }
        }
        
        if (numMatches >= minMatches) {
            if (log.isDebugEnabled()) {
                log.debug("handleTableQuery: Found " + numMatches + " matches, meets specified minimum of " + minMatches);
            }
            if (maxMatchesUnbounded) {
                if (log.isDebugEnabled()) {
                    log.debug("handleTableQuery: Maximum matches unbounded, returning true");
                }
                return true;
            } else if (numMatches <= maxMatches) {
                if (log.isDebugEnabled()) {
                    log.debug("handleTableQuery: Found " + numMatches + " matches, meets specified maximum of " + maxMatches + ", returning true");
                }
                return true;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("handleTableQuery: Found " + numMatches + " matches, exceeds specified maximum of " + maxMatches + ", returning false");
                }
                throw new XmpUtilException("Found too many value matches (" + numMatches + " > " + maxMatches + ") for condition " + mib + "." + object + " " + valueOperator + " " + valueOperand);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Found only " + numMatches + " matches, too few to meet specified minimum of " + minMatches);
            }
            throw new XmpUtilException("Found too few value matches (" + numMatches + " < " + minMatches + ") for condition " + mib + "." + object + " " + valueOperator + " " + valueOperand);
        }
    }
}
