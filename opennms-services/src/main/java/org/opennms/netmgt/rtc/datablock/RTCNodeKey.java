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

/**
 * The key used to look up items in the data map
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @version $Id: $
 */
public class RTCNodeKey extends Object implements Comparable {
    /**
     * The node ID
     */
    private long m_nodeID;

    /**
     * The ip address of the interface of the node
     */
    private String m_ip;

    /**
     * The service name
     */
    private String m_svcName;

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
    public RTCNodeKey(long nodeid, String ip, String svcname) {
        m_nodeID = nodeid;
        m_ip = ip;
        // m_svcName = svcname.toUpperCase();
        m_svcName = svcname;
    }

    /**
     * Set the node ID
     *
     * @param id
     *            the node ID
     */
    public void setNodeID(long id) {
        m_nodeID = id;
    }

    /**
     * Set the IP address
     *
     * @param ipStr
     *            the ip address
     */
    public void setIP(String ipStr) {
        m_ip = ipStr;
    }

    /**
     * Set the service name
     *
     * @param svcName
     *            the service name
     */
    public void setSvcName(String svcName) {
        m_svcName = svcName;
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
    public String getIP() {
        return m_ip;
    }

    /**
     * Overrides the 'hashCode()' method in the 'Object' superclass
     *
     * @return a sum of hashCodes of the inidividual attributes
     */
    public int hashCode() {
        int hcode = (int) (m_nodeID + m_ip.hashCode() + m_svcName.hashCode());

        return hcode;
    }

    /**
     * {@inheritDoc}
     *
     * Overrides the 'equals()' method in the 'Object' superclass
     */
    public boolean equals(Object o) {
        if (!(o instanceof RTCNodeKey)) {
            return false;
        }

        RTCNodeKey obj = (RTCNodeKey) o;

        if (m_nodeID == obj.getNodeID() && m_ip.equals(obj.getIP()) && m_svcName.equals(obj.getSvcName())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Implements java.jang.Comparable since this is a key to a treemap
     */
    public int compareTo(Object o) {
        if (!(o instanceof RTCNodeKey)) {
            return 0;
        }

        RTCNodeKey obj = (RTCNodeKey) o;

        int rc = (int) (m_nodeID - obj.getNodeID());
        if (rc != 0)
            return rc;

        rc = m_ip.compareTo(obj.getIP());
        if (rc != 0) {
            return rc;
        }

        else
            return m_svcName.compareTo(obj.getSvcName());
    }

    /**
     * Returns a string representation of this key
     *
     * @return a string representation of this key
     */
    public String toString() {
        String s = "RTCNodeKey\n[\n\t" + "nodeID    = " + m_nodeID + "\n\t" + "IP        = " + m_ip + "\n\t" + "Service   = " + m_svcName + "\n]\n";
        return s;
    }
}
