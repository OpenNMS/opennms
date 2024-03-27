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
