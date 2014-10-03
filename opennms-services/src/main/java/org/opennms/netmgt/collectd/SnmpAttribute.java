/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.support.AbstractCollectionAttribute;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SnmpAttribute class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SnmpAttribute extends AbstractCollectionAttribute {
    
    public static final Logger LOG = LoggerFactory.getLogger(SnmpAttribute.class);

    private SnmpValue m_val;

    /**
     * <p>Constructor for SnmpAttribute.</p>
     *
     * @param resource a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
     * @param type a {@link org.opennms.netmgt.collectd.SnmpAttributeType} object.
     * @param val a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpAttribute(CollectionResource resource, SnmpAttributeType type, SnmpValue val) {
        super(type, resource);
        m_val = val;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SnmpAttribute) {
            SnmpAttribute attr = (SnmpAttribute) obj;
            return (m_resource.equals(attr.m_resource) && m_attribType.equals(attr.m_attribType));
        }
        return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() {
        return (m_resource.hashCode() ^ m_attribType.hashCode());
    }

    /**
     * <p>getValue</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue getValue() {
        return m_val;
    }

    /** {@inheritDoc} */
    @Override
    public void storeAttribute(Persister persister) {
        getAttributeType().storeAttribute(this, persister);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return getResource()+"."+getAttributeType()+" = "+getValue();
    }

    @Override
    public String getMetricIdentifier() {
        String instance = m_resource.getInstance();
        SnmpAttributeType type = (SnmpAttributeType)m_attribType;
        if (instance == null) {
            instance = type.getInstance();
        }
        return "SNMP_"+SnmpObjId.get(type.getSnmpObjId(), instance);
    }
    
    /**
     * <p>getNumericValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getNumericValue() {
        if (getValue() == null) {
            LOG.debug("No data collected for attribute {}. Skipping", this);
            return null;
        } else if (getValue().isNumeric()) {
            return Long.toString(getValue().toLong());
        } else {
            // Check to see if this is a 63-bit counter packed into an octetstring
            Long value = SnmpUtils.getProtoCounter63Value(getValue());
            if (value != null) {
                return value.toString();
            }

            try {
                return Double.valueOf(getValue().toString()).toString();
            } catch(NumberFormatException e) {
                LOG.trace("Unable to process data received for attribute {} maybe this is not a number? See bug 1473 for more information. Skipping.", this);
                if (getValue().getType() == SnmpValue.SNMP_OCTET_STRING) {
                    try {
                        return Long.valueOf(getValue().toHexString(), 16).toString();
                    } catch(NumberFormatException ex) {
                        LOG.trace("Unable to process data received for attribute {} maybe this is not a number? See bug 1473 for more information. Skipping.", this);
                    }
                }
            }
            return null;
        }
    }
    
    /**
     * <p>getStringValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStringValue() {
        SnmpValue value=getValue();
        return (value == null ? null : value.toString());
    }
}
