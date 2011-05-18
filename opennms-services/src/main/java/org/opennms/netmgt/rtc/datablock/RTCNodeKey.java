//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

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
    private final long m_nodeID;

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
    public RTCNodeKey(long nodeid, InetAddress ip, String svcname) {
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
    public long getNodeID() {
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
        String s = "RTCNodeKey\n[\n\t" + "nodeID    = " + m_nodeID + "\n\t" + "IP        = " + InetAddressUtils.str(m_ip) + "\n\t" + "Service   = " + m_svcName + "\n]\n";
        return s;
    }
}
