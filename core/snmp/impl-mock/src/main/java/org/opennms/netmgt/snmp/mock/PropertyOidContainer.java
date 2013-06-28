/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class PropertyOidContainer {
	
	private static final Logger LOG = LoggerFactory.getLogger(PropertyOidContainer.class);
	
    private final NavigableMap<SnmpObjId,SnmpValue> m_tree = new TreeMap<SnmpObjId,SnmpValue>();

    public PropertyOidContainer(final Resource resource) throws IOException {
    	MockSnmpValueFactory factory = new MockSnmpValueFactory();
        final Properties moProps = new Properties();
        InputStream inStream = null;
        try {
            inStream = resource.getInputStream();
            moProps.load( inStream );
        } finally {
            IOUtils.closeQuietly(inStream);
        }

        for (final Object obj : moProps.keySet()) {
            final String key = obj.toString();
            if (!key.startsWith(".")) continue;
            final String value = moProps.getProperty(key);
            if (value.contains("No Such Object available on this agent at this OID")) { continue; }
            if (value.contains("No more variables left in this MIB View")) { continue; }
//          LogUtils.debugf(this, "%s = %s", key, value);
            try {
                m_tree.put(SnmpObjId.get(key), factory.parseMibValue(value));
            } catch (final NumberFormatException nfe) {
            	LOG.debug("Unable to store '{} = {}', skipping. ({})", key, value, nfe.getLocalizedMessage());
            }
        }
    }

    public SnmpValue findValueForOid(final SnmpObjId oid) {
        final SnmpValue value = m_tree.get(oid);
        if (value == null) {
            if (oid.getLastSubId() == 0) {
                return MockSnmpValue.NO_SUCH_OBJECT;
            } else {
                return MockSnmpValue.NO_SUCH_INSTANCE;
            }
        }
        return value;
    }

    public SnmpObjId findNextOidForOid(final SnmpObjId oid) {
        final NavigableMap<SnmpObjId,SnmpValue> next = m_tree.tailMap(oid, false);
        if (next.size() == 0) {
            return null;
        } else {
            return next.firstKey();
        }
    }
    
    public SnmpValue findNextValueForOid(final SnmpObjId oid) {
        final SnmpObjId nextOid = findNextOidForOid(oid);
        if (nextOid == null) {
            return MockSnmpValue.END_OF_MIB;
        } else {
            return findValueForOid(nextOid);
        }
    }

    public SnmpValue set(final SnmpObjId oid, final SnmpValue value) {
        m_tree.put(oid, value);
        return value;
    }

    public SnmpValue[] set(final SnmpObjId[] oids, final SnmpValue[] values) {
        for (int i = 0; i < oids.length; i++) {
            m_tree.put(oids[i], values[i]);
        }
        return values;
    }

}
