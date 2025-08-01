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

import java.util.*;

import org.opennms.netmgt.config.BeanInfo;
import org.opennms.netmgt.config.collectd.jmx.Attrib;

/**
 * This class encapsulates all of the node-level data required by the JMX data
 * collector in order to successfully perform data collection for a scheduled
 * primary JMX interface.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Jamison </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:mike@opennms.org">Mike Jamison </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public class JMXNodeInfo {
    private int m_nodeId;
    private Map<String, List<Attrib>> m_oidList;
    private Map<String, BeanInfo> m_mbeans;
    private Map<String, JMXDataSource> m_dsList;

    /**
     * <p>Constructor for JMXNodeInfo.</p>
     *
     * @param nodeId a int.
     */
    public JMXNodeInfo(int nodeId) {
        m_nodeId = nodeId;
        m_oidList = null;
        m_dsList = null;
        m_mbeans = new HashMap<String, BeanInfo>();
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
     * <p>setMBeans</p>
     *
     * @param map a {@link java.util.Map} object.
     */
    public void setMBeans(Map<String, BeanInfo> map) {
        m_mbeans = map;
    }
    
    /**
     * <p>getMBeans</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, BeanInfo> getMBeans() {
        return Collections.unmodifiableMap(m_mbeans);
    }

    /**
     * <p>setNodeId</p>
     *
     * @param nodeId a int.
     */
    public void setNodeId(int nodeId) {
        m_nodeId = nodeId;
    }

    /**
     * <p>setDsMap</p>
     *
     * @param dsList a {@link java.util.Map} object.
     */
    public void setDsMap(Map<String, JMXDataSource> dsList) {
        m_dsList = dsList;
    }

    /**
     * <p>getAttributeMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, List<Attrib>> getAttributeMap() {
        return Collections.unmodifiableMap(m_oidList);
    }

    /**
     * <p>setAttributeMap</p>
     *
     * @param oidList a {@link java.util.Map} object.
     */
    public void setAttributeMap(Map<String, List<Attrib>> oidList) {
        m_oidList = oidList;
    }

    /**
     * <p>getDsMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, JMXDataSource> getDsMap() {
        return Collections.unmodifiableMap(m_dsList);
    }

} // end class
