/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.netutils.internal;

/**
 * The Node class constructs an object which contains all necessary information
 * and methods for a server or machine in a network
 * @author Leonardo Bell
 * @author Philip Grenon
 */
public class Node {
    
    private String label; //Name of the server or machine
    private String ipAddr; //IP Address of the server or machine
    private int nodeID; //Identification number
    
    public Node(int nodeID, String ip, String label) {
        this.nodeID = nodeID;
        this.ipAddr = ip;
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getIPAddress() {
        return ipAddr;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public void setIPAddress(String ip) {
        this.ipAddr = ip;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public int getNodeID() {
        return nodeID;
    }
}
