/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.config.collector.Persister;
import org.opennms.netmgt.config.collector.ServiceParameters;
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

    private CollectionResource m_resource;
    private SnmpAttributeType m_type;
    private SnmpValue m_val;

    /**
     * <p>Constructor for SnmpAttribute.</p>
     *
     * @param resource a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
     * @param type a {@link org.opennms.netmgt.collectd.SnmpAttributeType} object.
     * @param val a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpAttribute(CollectionResource resource, SnmpAttributeType type, SnmpValue val) {
        super();
        m_resource = resource;
        m_type = type;
        m_val = val;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SnmpAttribute) {
            SnmpAttribute attr = (SnmpAttribute) obj;
            return (m_resource.equals(attr.m_resource) && m_type.equals(attr.m_type));
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
        return (m_resource.hashCode() ^ m_type.hashCode());
    }

    /** {@inheritDoc} */
    @Override
    public void visit(CollectionSetVisitor visitor) {
        LOG.debug("Visiting attribute {}", this);
        visitor.visitAttribute(this);
        visitor.completeAttribute(this);
    }

    /**
     * <p>getAttributeType</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.SnmpAttributeType} object.
     */
    @Override
    public SnmpAttributeType getAttributeType() {
        return m_type;
    }

    /**
     * <p>getResource</p>
     *
     * @return a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
     */
    @Override
    public CollectionResource getResource() {
        return m_resource;
    }

    /**
     * <p>getValue</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue getValue() {
        return m_val;
    }

    void store(Persister persister) {
        getAttributeType().storeAttribute(this, persister);
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

    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getType() {
        return getAttributeType().getType();
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return getAttributeType().getName();
    }
    
    
    @Override
    public String getMetricIdentifier() {
        String instance = m_resource.getInstance();
        if (instance == null) {
            instance = m_type.getInstance();
        }
        return "SNMP_"+SnmpObjId.get(m_type.getSnmpObjId(), instance);
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
        } else if (getValue().getBytes().length == 8) {
            return Long.toString(SnmpUtils.getProtoCounter64Value(getValue()));
        } else {
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
