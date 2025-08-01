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
package org.opennms.netmgt.collectd;

import org.opennms.netmgt.collection.api.AttributeType;
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

    private final SnmpValue m_val;

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
     * @return a {@link java.lang.Number} object.
     */
    @Override
    public Number getNumericValue() {
        final SnmpValue snmpValue = getValue();
        if (snmpValue == null) {
            LOG.debug("No data collected for attribute {}. Skipping", this);
            return null;
        }

        //Check snmpValue.isDouble() before snmpValue.isNumeric()
        //because isNumeric() returns also true on double values in OpaqueExt
        if (snmpValue.isDouble()) {
            return snmpValue.toDouble();
        }
        
        if (snmpValue.isNumeric()) {
            return snmpValue.toLong();
        }
        
        // Check to see if this is a 63-bit counter packed into an octetstring
        Long value = SnmpUtils.getProtoCounter63Value(snmpValue);
        if (value != null) {
            return value;
        }

        try {
            if (AttributeType.COUNTER.equals(getType())) { // See NMS-7839: for RRDtool the raw counter value must be an integer.
                return Long.valueOf(snmpValue.toString());
            }
            return Double.valueOf(snmpValue.toString());
        } catch(NumberFormatException e) {
            LOG.trace("Unable to process data received for attribute {} maybe this is not a number? See bug 1473 for more information. Skipping.", this);
            if (snmpValue.getType() == SnmpValue.SNMP_OCTET_STRING) {
                try {
                    return Long.valueOf(snmpValue.toHexString(), 16);
                } catch(NumberFormatException ex) {
                    LOG.trace("Unable to process data received for attribute {} maybe this is not a number? See bug 1473 for more information. Skipping.", this);
                }
            }
        }
        return null;
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
