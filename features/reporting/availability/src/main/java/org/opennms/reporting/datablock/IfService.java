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
package org.opennms.reporting.datablock;

/**
 * This class holds the managed node/ip/service information for valid outage
 * nodes.
 *
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 */
public class IfService extends Object {

    /**
     * Nodeid
     */
    int nodeid;

    /**
     * Ipaddress
     */
    String ipaddr;

    /**
     * Serviceid
     */
    int serviceid;

    /**
     * Node Name
     */
    String nodeName;

    /**
     * Service Name
     */
    String serviceName;

    /**
     * Default Constructor.
     */
    public IfService() {
        nodeid = -1;
        ipaddr = null;
        serviceid = -1;
    }

    /**
     * Constructor that initialises the nodeid, ipaddr, service.
     *
     * @param node a int.
     * @param ip a {@link java.lang.String} object.
     * @param service a int.
     * @param name a {@link java.lang.String} object.
     * @param svcname a {@link java.lang.String} object.
     */
    public IfService(int node, String ip, int service, String name, String svcname) {
        nodeid = node;
        ipaddr = ip;
        serviceid = service;
        nodeName = name;
        serviceName = svcname;
    }

    /**
     * Sets the node id.
     *
     * @param id
     *            Node id to be set
     */
    public void setNodeID(int id) {
        nodeid = id;
    }

    /**
     * Return node name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Sets the Service name.
     *
     * @param name
     *            Service name
     */
    public void setServiceName(String name) {
        serviceName = name;
    }

    /**
     * Return service name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the node name.
     *
     * @param name
     *            Node name to be set
     */
    public void setNodeName(String name) {
        nodeName = name;
    }

    /**
     * Returns the Node id
     *
     * @return node id.
     */
    public int getNodeID() {
        return nodeid;
    }

    /**
     * Sets the ipaddr.
     *
     * @param ip
     *            ipaddress to be set
     */
    public void setIpaddr(String ip) {
        ipaddr = ip;
    }

    /**
     * This method returns the ipaddress.
     *
     * @return Returns the ipaddress.
     */
    public String getIpaddr() {
        return ipaddr;
    }

    /**
     * This method sets the service.
     *
     * @param service
     *            Service id to be set.
     */
    public void setServiceId(int service) {
        serviceid = service;
    }

    /**
     * This returns the service id.
     *
     * @return a int.
     */
    public int getServiceId() {
        return serviceid;
    }

    /**
     * toString method
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("Node id " + nodeName + " Ipaddr " + ipaddr + " Service name " + serviceName);
    }
}
