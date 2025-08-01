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

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.OnmsSnmpInterface;

import com.google.common.base.MoreObjects;

/**
 * <p>SnmpIfData class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class SnmpIfData {

    private final int m_nodeId;
    private final boolean m_collectionEnabled;
    private final int m_ifIndex;
    private final int m_ifType;
    private final String m_rrdLabel;
    private final String m_ifAlias;
    
    private final Map<String,String> m_attributes = new HashMap<String,String>();
    
    /**
     * <p>Constructor for SnmpIfData.</p>
     *
     * @param snmpIface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    public SnmpIfData(OnmsSnmpInterface snmpIface) {
        m_nodeId = nullSafeUnbox(snmpIface.getNode().getId(), -1);
        m_collectionEnabled = snmpIface.isCollectionEnabled();
        m_ifIndex = nullSafeUnbox(snmpIface.getIfIndex(), -1);
        m_ifType = nullSafeUnbox(snmpIface.getIfType(), -1);
        m_rrdLabel = snmpIface.computeLabelForRRD();
        m_ifAlias = snmpIface.getIfAlias();
        m_attributes.put("snmpphysaddr", snmpIface.getPhysAddr());
        m_attributes.put("snmpifindex", Integer.toString(m_ifIndex));
        m_attributes.put("snmpifdescr", snmpIface.getIfDescr());
        m_attributes.put("snmpiftype", Integer.toString(m_ifType));
        m_attributes.put("snmpifname", snmpIface.getIfName());
        m_attributes.put("snmpifadminstatus", Integer.toString(nullSafeUnbox(snmpIface.getIfAdminStatus(), -1)));
        m_attributes.put("snmpifoperstatus", Integer.toString(nullSafeUnbox(snmpIface.getIfOperStatus(), -1)));
        m_attributes.put("snmpifspeed", Long.toString(nullSafeUnbox(snmpIface.getIfSpeed(), -1)));
        m_attributes.put("snmpifalias", m_ifAlias);
    }
    
    protected static int nullSafeUnbox(Integer num, int dflt) {
        return (num == null ? dflt : num.intValue());
    }

    protected static long nullSafeUnbox(Long num, long dflt) {
        return (num == null ? dflt : num.longValue());
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return m_nodeId;
    }

    /**
     * <p>isCollectionEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isCollectionEnabled() {
        return m_collectionEnabled;
    }

    /**
     * <p>getIfIndex</p>
     *
     * @return a int.
     */
    public int getIfIndex() {
        return m_ifIndex;
    }

    /**
     * <p>getIfType</p>
     *
     * @return a int.
     */
    public int getIfType() {
        return m_ifType;
    }

    /**
     * <p>getLabelForRRD</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabelForRRD() {
        return m_rrdLabel;
    }

    /**
     * <p>getIfAlias</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfAlias() {
        return m_ifAlias;
    }
    
    /**
     * <p>getAttribtuesMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String,String> getAttributesMap() {
        return m_attributes;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nodeId", m_nodeId)
                .add("collectionEnabled", m_collectionEnabled)
                .add("ifIndex", m_ifIndex)
                .add("ifType", m_ifType)
                .add("rrdLabel", m_rrdLabel)
                .add("ifAlias", m_ifAlias)
                .add("attributes", m_attributes)
                .toString();
    }
}
