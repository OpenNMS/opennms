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
