/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

/**
 * <p>MatchingSnmpEndPointValidationExpression class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;
import org.opennms.netmgt.snmp.SnmpValue;

@XmlRootElement(name="match-oid")
public class MatchingSnmpEndPointValidationExpression extends EndPointValidationExpressionImpl {
    @XmlAttribute(name="oid")
    private String m_oid = null;

    /**
     * <p>Constructor for MatchingSnmpEndPointValidationExpression.</p>
     */
    public MatchingSnmpEndPointValidationExpression() {
    }
    
    /**
     * <p>Constructor for MatchingSnmpEndPointValidationExpression.</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @param oid a {@link java.lang.String} object.
     */
    public MatchingSnmpEndPointValidationExpression(String regex, String oid) {
        setValue(regex);
        m_oid = oid;
    }

    /** {@inheritDoc} */
    @Override
    public void validate(EndPoint endPoint) throws EndPointStatusException {
        SnmpValue snmpValue = endPoint.get(m_oid);
        if(snmpValue == null) {
            throw new EndPointStatusException("unable to validate endpoint " + endPoint + ": could not retreive a value from SNMP agent that matches " + m_value);
        }
        String value = snmpValue.toString();
        if(value != null && value.matches(m_value)) {
            return;
        }
        throw new EndPointStatusException("unable to validate endpoint " + endPoint + ": " + m_value + " does not match value (" + value + ")");
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "match(" + m_value + ")";
    }
}
