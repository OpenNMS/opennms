/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.operations;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;

/**
 * <p>ScanResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ScanResource {
    private String m_type;
    private OnmsNode m_node = null;
    private final Map<String,String> m_attributes = new HashMap<String,String>();

    /**
     * <p>Constructor for ScanResource.</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public ScanResource(String type) {
        m_type = type;
    }
    
    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return m_type;
    }

    /**
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(OnmsNode node) {
        m_node = node;
    }

    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public OnmsNode getNode() {
        return m_node;
    }

    // TODO: change node comparison to use spring
    /**
     * <p>setAttribute</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setAttribute(String key, String value) {
        m_attributes.put(key, value);
        if (m_node != null) {
            if (key.equals("sysContact")) {
                m_node.setSysContact(value);
            } else if (key.equals("sysDescription")) {
                m_node.setSysDescription(value);
            } else if (key.equals("sysLocation")) {
                m_node.setSysLocation(value);
            } else if (key.equals("sysObjectId")) {
                m_node.setSysObjectId(value);
            } else if (key.equals("sysName")) {
                m_node.setSysName(value);

                // If the sysName isn't null or empty...
                if (value != null && !"".equals(value.trim())) {
                    // If the node is labeled as just the IP address from the newSuspect that created it,
                    // use the SNMP sysName value instead and update the label source to indicate this
                    if (m_node.getLabelSource() == NodeLabelSource.ADDRESS) {
                        m_node.setLabel(value);
                        m_node.setLabelSource(NodeLabelSource.SYSNAME);
                    }
                }
            }
            
        }
    }

    /**
     * <p>getAttribute</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getAttribute(String key) {
        return m_attributes.get(key);
    }
}
