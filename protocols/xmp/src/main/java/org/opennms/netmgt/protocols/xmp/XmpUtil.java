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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmpUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(XmpUtil.class);

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
    
    private static boolean valueMeetsCriteria(XmpVar replyVar, String valueOperator, String valueOperand, boolean caseSensitive)
            throws XmpUtilException {
        RE valueRegex = null;
        if (MATCHES.equals(valueOperator)) {
            try {
				valueRegex = new RE(valueOperand);
	            if (!caseSensitive) {
	            	valueRegex.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
	            }
            } catch (final RESyntaxException e) {
            	LOG.debug("Unable to initialize regular expression.", e);
            }
        }
        
        if ((valueRegex != null) && valueRegex.match(replyVar.getValue())) {
            LOG.debug("handleScalarQuery: Response value |{}| matches, returning true", replyVar.getValue());
            return true;
        } else if ((MATCHES.equals(valueOperator)) && ((valueRegex == null) || ("".equals(valueRegex)))) {
                LOG.debug("handleScalarQuery: Doing regex match but regex is null or empty, considering value |{}| a match!", replyVar.getValue());
            return true;
        } else if (! MATCHES.equals(valueOperator)) {
            if (valueOperand == null) {
                    LOG.debug("valueMeetsCriteria: operand is null, so any non-error reply will match");
                if (replyVar.getValue() != null) {
                        LOG.debug("valueMeetsCriteria: non-null reply value |{}| considered a match", replyVar.getValue());
                    return true;
                } else {
                        LOG.debug("valueMeetsCriteria: null reply NOT considered a match");
                    return false;
                }
            } else if (valueOperand.matches("^-?[0-9]+$")) {
                    LOG.debug("valueMeetsCriteria: operand {} looks like an integer, treating with BigInteger", valueOperand);
                BigInteger intOperand, intValue;
                try {
                    intValue = new BigInteger(replyVar.getValue());
                    intOperand = new BigInteger(valueOperand);
                } catch (NumberFormatException nfe) {
                    LOG.error("Failed to parse operand {} or observed value {} as a BigInteger, giving up", valueOperand, replyVar.getValue());
                    LOG.info(nfe.getMessage());
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
                    LOG.error("Unknown value operator '{}', giving up", valueOperator);
                    throw new XmpUtilException("Operator '" + valueOperator + "' unknown");
                }
            } else if (valueOperand.matches("^-?[0-9]+([,.]?[0-9]+)$")) {
                    LOG.debug("valueMeetsCriteria: operand {} looks like a float, treating with float", valueOperand);
                float floatValue, floatOperand;
                try {
                    floatValue = Float.parseFloat(valueOperand);
                    floatOperand = Float.parseFloat(valueOperand);
                } catch (NumberFormatException nfe) {
                    LOG.error("Failed to parse operand {} or observed value {} as a Float, giving up", valueOperand, replyVar.getValue());
                    LOG.info(nfe.getMessage());
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
                    LOG.error("Unknown value operator '{}', giving up", valueOperator);
                    throw new XmpUtilException("Value operator '" + valueOperator + "' unknown");
                }
            } else {
                    LOG.debug("valueMeetsCriteria: operand {} looks non-numeric, treating with String", valueOperand);
                if (!EQUALS.equals(valueOperator)) {
                    LOG.error("Value operator '{}' does not apply for non-numeric value operand '{}', giving up", valueOperator, valueOperand);
                    throw new XmpUtilException("Value operator '" + valueOperator + "' does not apply for non-numeric value operand '" + valueOperand + "'");
                }
                if (caseSensitive) {
				    return valueOperand.equals(replyVar.getValue());                        
				} else {
				    return valueOperand.equalsIgnoreCase(replyVar.getValue());
				}
            }
        } else {
        	if(LOG.isDebugEnabled()){
                LOG.debug("handleScalarQuery: Response value |{}| does not match for value operator |{}| and value operand |{}|, returning false", replyVar.getValue(), valueOperator, valueOperand);
                //FIXME do we really want to throw only when debugging is enabled?
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
     * @param caseSensitive a boolean.
     * @return a boolean.
     * @throws org.opennms.netmgt.protocols.xmp.XmpUtilException if any.
     */
    public static boolean handleScalarQuery(XmpSession session, String mib,
            String object, String valueOperator, String valueOperand, boolean caseSensitive) throws XmpUtilException {
        XmpMessage reply;
        XmpVar[] queryVars = new XmpVar[1];
        XmpVar[] replyVars;
        
        queryVars[0] = new XmpVar(mib, object, Xmp.SYNTAX_NULLSYNTAX);
        
        reply = session.queryVars(queryVars);
        if (reply == null) {
            LOG.warn("handleScalarQuery: query for object {} from MIB {} failed, {}", object, mib, Xmp.errorStatusToString(session.getErrorStatus()));
            return false;
        } else {
            LOG.debug("handleScalarQuery: query for object {} from MIB {} succeeded.", object, mib);
        }
        
        replyVars = reply.getMIBVars();
        if (replyVars[0].getMibName().equals(mib) && replyVars[0].getObjName().equals(object)) {
            return valueMeetsCriteria(replyVars[0], valueOperator, valueOperand, caseSensitive);
        } else {
            LOG.error("Observed MIB name ({}) or object name ({}) does not match specified MIB name ({}) or object name ({}), giving up", replyVars[0].getMibName(), replyVars[0].getObjName(), mib, object);
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
     * @param caseSensitive a boolean.
     * @return a boolean.
     * @throws org.opennms.netmgt.protocols.xmp.XmpUtilException if any.
     */
    public static boolean handleTableQuery(XmpSession session, String mib,
            String table, String object, String instance, RE instanceRegex, 
            String valueOperator, String valueOperand, int minMatches,
            int maxMatches, boolean maxMatchesUnbounded,
            boolean caseSensitive) throws XmpUtilException {
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
            LOG.warn("handleTableQuery: query for object {} from MIB {} failed, {}", object, mib, Xmp.errorStatusToString(session.getErrorStatus()));
            throw new XmpUtilException("XMP query failed (MIB " + mib + ", object " + object + "): " + Xmp.errorStatusToString(session.getErrorStatus()));
        }
        
        replyVars = reply.getMIBVars();
        LOG.debug("handleTableQuery: Got reply with {} variables", replyVars.length);
        
        
        /* Since we're constrained to a single object, we know that there's
         * exactly one column in the result set and so can use a Java 5
         * for() loop. If there were multiple columns, we'd have to break the
         * flat array into a two-dimensional matrix using a pair of old-style
         * for() loops.
         */
        for (XmpVar thisVar : replyVars) {
            String rowInstance = thisVar.getKey();
            if ((instanceRegex != null) && (!instanceRegex.match(rowInstance))) {
                
            	LOG.debug("handleTableQuery: instance {} does not match, skipping this row.", rowInstance);
                
                continue;  // to next var
            } else if (instanceRegex == null) {
            	LOG.debug("handleTableQuery: instance match not specified, evaluating value of instance {}", rowInstance);
            } else {
                    LOG.debug("handleTableQuery: instance {} matches, evaluating value", rowInstance);
            }
            if (valueMeetsCriteria(thisVar, valueOperator, valueOperand, caseSensitive)) {
                numMatches++;
            }
        }
        
        if (numMatches >= minMatches) {
                LOG.debug("handleTableQuery: Found {} matches, meets specified minimum of {}", numMatches, minMatches);
            if (maxMatchesUnbounded) {
                    LOG.debug("handleTableQuery: Maximum matches unbounded, returning true");
                return true;
            } else if (numMatches <= maxMatches) {
                    LOG.debug("handleTableQuery: Found {} matches, meets specified maximum of {}, returning true", numMatches, maxMatches);
                return true;
            } else {
                    LOG.debug("handleTableQuery: Found {} matches, exceeds specified maximum of {}, returning false", numMatches, maxMatches);
                throw new XmpUtilException("Found too many value matches (" + numMatches + " > " + maxMatches + ") for condition " + mib + "." + object + " " + valueOperator + " " + valueOperand);
            }
        } else {
                LOG.debug("Found only {} matches, too few to meet specified minimum of {}", numMatches, minMatches);
            throw new XmpUtilException("Found too few value matches (" + numMatches + " < " + minMatches + ") for condition " + mib + "." + object + " " + valueOperator + " " + valueOperand);
        }
    }
}
