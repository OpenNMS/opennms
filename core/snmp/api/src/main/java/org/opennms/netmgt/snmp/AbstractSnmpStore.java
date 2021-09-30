/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.net.InetAddress;
import java.util.Map;
import java.util.TreeMap;


public abstract class AbstractSnmpStore {

    private final Map<String, SnmpValue> m_responseMap = new TreeMap<String, SnmpValue>();
    public static final String IFINDEX = "ifIndex";
    public abstract void storeResult(SnmpResult res);

    public AbstractSnmpStore() {
    }

    public Integer getInt32(String key) {
        SnmpValue val = getValue(key);
        return (val == null ? null : Integer.valueOf(val.toInt()));
    }

    public Long getUInt32(String key) {
        SnmpValue val = getValue(key);
        return (val == null ? null : Long.valueOf(val.toLong()));
    }

    public String getDisplayString(String key) {
        SnmpValue val = getValue(key);
        return (val == null ? null : val.toDisplayString());
    }

    public String getHexString(String key) {
        SnmpValue val = getValue(key);
        return (val == null ? null : val.toHexString());
    }

    public InetAddress getIPAddress(String key) {
        SnmpValue val = getValue(key);
        return (val == null ? null : val.toInetAddress());
    }

    public String getObjectID(String key) {
        return (getValue(key) == null ? null : getValue(key).toString());
    }

    public SnmpValue getValue(String key) {
        return m_responseMap.get(key);
    }

    protected void putValue(String key, SnmpValue value) {
        m_responseMap.put(key, value);
    }

    public Integer getIfIndex() {
        return getInt32(IFINDEX);
    }

    protected void putIfIndex(int ifIndex) {
        putValue(IFINDEX, SnmpUtils.getValueFactory().getInt32(ifIndex));
    }
    
    public int size() {
        return m_responseMap.size();
    }
    
    public boolean isEmpty() {
        return m_responseMap.isEmpty();
    }


}
