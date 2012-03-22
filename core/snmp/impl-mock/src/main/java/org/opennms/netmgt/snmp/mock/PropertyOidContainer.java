package org.opennms.netmgt.snmp.mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.springframework.core.io.Resource;

public class PropertyOidContainer {
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
                LogUtils.debugf(this, "Unable to store '%s = %s', skipping. (%s)", key, value, nfe.getLocalizedMessage());
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
