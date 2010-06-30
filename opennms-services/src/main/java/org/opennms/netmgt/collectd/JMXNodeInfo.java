//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//  
//For more information contact: 
// OpenNMS Licensing       <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.util.*;

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

    private Map m_oidList;
    private HashMap m_mbeans;

    private HashMap m_dsList;

    /**
     * <p>Constructor for JMXNodeInfo.</p>
     *
     * @param nodeId a int.
     */
    public JMXNodeInfo(int nodeId) {
        m_nodeId = nodeId;
        m_oidList = null;
        m_dsList = null;
        m_mbeans = new HashMap();
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
     * @param map a {@link java.util.HashMap} object.
     */
    public void setMBeans(HashMap map) {
        m_mbeans = map;
    }
    
    /**
     * <p>getMBeans</p>
     *
     * @return a {@link java.util.HashMap} object.
     */
    public HashMap getMBeans() {
        return m_mbeans;
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
     * @param dsList a {@link java.util.HashMap} object.
     */
    public void setDsMap(HashMap dsList) {
        m_dsList = dsList;
    }

    /**
     * <p>setAttributeMap</p>
     *
     * @param oidList a {@link java.util.Map} object.
     */
    public void setAttributeMap(Map oidList) {
        m_oidList = oidList;
    }

    /**
     * <p>getDsMap</p>
     *
     * @return a {@link java.util.HashMap} object.
     */
    public HashMap getDsMap() {
        return m_dsList;
    }

    /**
     * <p>getAttributeMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map getAttributeMap() {
        return m_oidList;
    }
} // end class
