/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.OnmsNode;

/**
 * <p>RtcNodeModel class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class RtcNodeModel {
    private static final DecimalFormat AVAILABILITY_FORMAT = new DecimalFormat("0.000%");
    
    private List<RtcNode> m_nodeList = new ArrayList<RtcNode>();
    
    static {
        AVAILABILITY_FORMAT.setMultiplier(100);
    }
    
    /**
     * <p>addNode</p>
     *
     * @param node a {@link org.opennms.web.svclayer.support.RtcNodeModel.RtcNode} object.
     */
    public void addNode(RtcNode node) {
        m_nodeList.add(node);
    }
    
    /**
     * <p>getNodeList</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<RtcNode> getNodeList() {
        return m_nodeList;
    }
    
    public static class RtcNode {
        private OnmsNode m_node;
        private int m_serviceCount;
        private int m_downServiceCount;
        private double m_availability;
        
        public RtcNode(OnmsNode node, int serviceCount, int downServiceCount, double availability) {
            m_node = node;
            m_serviceCount = serviceCount;
            m_downServiceCount = downServiceCount;
            m_availability = availability;
        }

        public double getAvailability() {
            return m_availability;
        }
        
        public String getAvailabilityAsString() {
            return AVAILABILITY_FORMAT.format(m_availability);
        }

        public int getDownServiceCount() {
            return m_downServiceCount;
        }

        public OnmsNode getNode() {
            return m_node;
        }

        public int getServiceCount() {
            return m_serviceCount;
        }
        
        @Override
        public String toString() {
            return m_node.getLabel() + ": " + m_downServiceCount + " of " + m_serviceCount + ": " + getAvailabilityAsString();
        }
    }
}
