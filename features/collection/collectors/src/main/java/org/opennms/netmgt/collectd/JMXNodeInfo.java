/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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
