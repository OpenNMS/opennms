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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.poller.monitors;

import java.math.BigInteger;
import java.util.Map;
import java.util.regex.Pattern;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * <p>Abstract SnmpMonitorStrategy class.</p>
 *
 * @author david
 * @version $Id: $
 */
abstract public class SnmpMonitorStrategy extends IPv4Monitor {

    /**
     * Constant for less-than operand
     */
    protected static final String LESS_THAN = "<";
    /** Constant <code>GREATER_THAN=">"</code> */
    protected static final String GREATER_THAN = ">";
    /** Constant <code>LESS_THAN_EQUALS="<="</code> */
    protected static final String LESS_THAN_EQUALS = "<=";
    /** Constant <code>GREATER_THAN_EQUALS=">="</code> */
    protected static final String GREATER_THAN_EQUALS = ">=";
    /** Constant <code>EQUALS="="</code> */
    protected static final String EQUALS = "=";
    /** Constant <code>NOT_EQUAL="!="</code> */
    protected static final String NOT_EQUAL = "!=";
    /** Constant <code>MATCHES="~"</code> */
    protected static final String MATCHES = "~";
    
    /*
     * TODO: Use it or loose it.
     * Commented out because it is not currently used in this monitor
     */
    //private Category log = ThreadCategory.getInstance(getClass());

    /** {@inheritDoc} */
    abstract public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) ;
    
    
    /**
     * Verifies that the result of the SNMP query meets the criteria specified
     * by the operator and the operand from the configuration file.
     *
     * @param result a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     * @param operator a {@link java.lang.String} object.
     * @param operand a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean meetsCriteria(SnmpValue result, String operator, String operand) {

        Boolean retVal = null;
        
        retVal = isCriteriaNull(result, operator, operand);
        
        if (retVal == null) {
            String value = result.toString();
            retVal = checkStringCriteria(operator, operand, value);
            
            if (retVal == null) {
                
                BigInteger val = BigInteger.valueOf(result.toLong());
                
                BigInteger intOperand = new BigInteger(operand);
                if (LESS_THAN.equals(operator)) {
                    return val.compareTo(intOperand) < 0;
                } else if (LESS_THAN_EQUALS.equals(operator)) {
                    return val.compareTo(intOperand) <= 0;
                } else if (GREATER_THAN.equals(operator)) {
                    return val.compareTo(intOperand) > 0;
                } else if (GREATER_THAN_EQUALS.equals(operator)) {
                    return val.compareTo(intOperand) >= 0;
                } else {
                    throw new IllegalArgumentException("operator " + operator + " is unknown");
                }
            }
        } else if (retVal.booleanValue()) {
            return true;
        }
        
        return retVal.booleanValue();
    }

    /**
     * @param operator
     * @param operand
     * @param retVal
     * @param value
     * @return
     */
    private Boolean checkStringCriteria(final String operator, String operand, String value) {
        Boolean retVal = null;
        
        if (value == null) {
            value = "";
        } else if (value.startsWith(".")) {
            value = value.substring(1);
        }
        
        // Bug 2178 -- if this is a regex match, a leading "." in the operand
        // should not be stripped
        if (operand.startsWith(".") && !MATCHES.equals(operator)) {
            operand = operand.substring(1);
        }
        
        if (EQUALS.equals(operator))
            retVal = Boolean.valueOf(operand.equals(value));
        else if (NOT_EQUAL.equals(operator))
            retVal = Boolean.valueOf(!operand.equals(value));
        else if (MATCHES.equals(operator))
            retVal = Boolean.valueOf(Pattern.compile(operand).matcher(value).find());
        return retVal;
    }

    /**
     * @param result
     * @param operator
     * @param operand
     * @param retVal
     * @return
     */
    private Boolean isCriteriaNull(Object result, String operator, String operand) {
        
        if (result == null)
            return Boolean.FALSE;
        if (operator == null || operand == null) {
            return Boolean.TRUE;
        } else {
            return null;
        }
    }

}
