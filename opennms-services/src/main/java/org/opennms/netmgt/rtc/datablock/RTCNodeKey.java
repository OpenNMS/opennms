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
package org.opennms.netmgt.rtc.datablock;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressComparator;
import org.opennms.core.utils.InetAddressUtils;

/**
 * The key used to look up items in the data map
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class RTCNodeKey implements Comparable<RTCNodeKey> {
    /**
     * The node ID
     */
    private final int m_nodeID;

    /**
     * The ip address of the interface of the node
     */
    private final InetAddress m_ip;

    /**
     * The service name
     */
    private final String m_svcName;

    /**
     * the constructor for this class
     *
     * @param nodeid
     *            the node ID
     * @param ip
     *            the node IP
     * @param svcname
     *            the service in the node
     */
    public RTCNodeKey(int nodeid, InetAddress ip, String svcname) {
        m_nodeID = nodeid;
        m_ip = ip;
        // m_svcName = svcname.toUpperCase();
        m_svcName = svcname;
    }

    /**
     * Return the node ID
     *
     * @return the node ID
     */
    public int getNodeID() {
        return m_nodeID;
    }

    /**
     * Return the service name
     *
     * @return the service name
     */
    public String getSvcName() {
        return m_svcName;
    }

    /**
     * Return the IP address
     *
     * @return the IP address
     */
    public InetAddress getIP() {
        return m_ip;
    }

    /**
     * Overrides the 'hashCode()' method in the 'Object' superclass
     *
     * @return a sum of hashCodes of the individual attributes
     */
    @Override
    public int hashCode() {
        int hcode = (int) (m_nodeID + (m_ip == null ? 0 : m_ip.hashCode()) + (m_svcName == null ? 0 : m_svcName.hashCode()));

        return hcode;
    }

    /**
     * {@inheritDoc}
     *
     * Overrides the 'equals()' method in the 'Object' superclass
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RTCNodeKey)) {
            return false;
        }

        return compareTo((RTCNodeKey)o) == 0;
    }

    /**
     * Implements java.jang.Comparable since this is a key to a tree map
     *
     * @param obj a {@link org.opennms.netmgt.rtc.datablock.RTCNodeKey} object.
     * @return a int.
     */
    @Override
    public int compareTo(RTCNodeKey obj) {
        int rc = (int) (m_nodeID - obj.getNodeID());
        if (rc != 0) {
            return rc;
        }

        if (m_ip == null && obj.getIP() == null) {
            rc = 0;
        } else {
            rc = new InetAddressComparator().compare(m_ip, obj.getIP());
        }

        if (rc != 0) {
            return rc;
        }

        if(m_svcName == null) {
            if (obj.getSvcName() == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return m_svcName.compareTo(obj.getSvcName());
        }
    }

    /**
     * Returns a string representation of this key
     *
     * @return a string representation of this key
     */
    @Override
    public String toString() {
        return "RTCNodeKey\n[\n\t" + "nodeID    = " + m_nodeID + "\n\t" + "IP        = " + InetAddressUtils.str(m_ip) + "\n\t" + "Service   = " + m_svcName + "\n]\n";
    }
}
