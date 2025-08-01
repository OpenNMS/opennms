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
