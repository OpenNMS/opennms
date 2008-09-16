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

package org.opennms.web.element;

import org.opennms.web.Util;

public class Node implements Comparable<Node> {

    int m_nodeId;
    int m_nodeParent;
    String m_label;
    String m_dpname;
    String m_nodeCreateTime;
    String m_nodeSysId;
    String m_nodeSysName;
    String m_nodeSysDescr;
    String m_nodeSysLocn;
    String m_nodeSysContact;
    char m_nodeType;
    String m_operatingSystem;
    String m_foreignSource;
    String m_foreignSourceId;
    
    /* package-protected so only the NetworkElementFactory can instantiate */
    Node() {
    }

    /* package-protected so only the NetworkElementFactory can instantiate */
    Node(int nodeId, int nodeParent, String label, String dpname, String nodeCreateTime, String nodeSysId, String nodeSysName, String nodeSysDescr, String nodeSysLocn, String nodeSysContact, char nodeType, String operatingSystem) {
        m_nodeId = nodeId;
        m_nodeParent = nodeParent;
        m_label = label;
        m_dpname = dpname;
        m_nodeCreateTime = nodeCreateTime;
        m_nodeSysId = nodeSysId;
        m_nodeSysName = nodeSysName;
        m_nodeSysDescr = nodeSysDescr;
        m_nodeSysLocn = nodeSysLocn;
        m_nodeSysContact = nodeSysContact;
        m_nodeType = nodeType;
        m_operatingSystem = operatingSystem;
    }

    /* package-protected so only the NetworkElementFactory can instantiate */
    Node(int nodeId, int nodeParent, String label, String dpname, String nodeCreateTime, String nodeSysId, String nodeSysName, String nodeSysDescr, String nodeSysLocn, String nodeSysContact, char nodeType, String operatingSystem, String foreignSourceId, String foreignSource) {
        m_nodeId = nodeId;
        m_nodeParent = nodeParent;
        m_label = label;
        m_dpname = dpname;
        m_nodeCreateTime = nodeCreateTime;
        m_nodeSysId = nodeSysId;
        m_nodeSysName = nodeSysName;
        m_nodeSysDescr = nodeSysDescr;
        m_nodeSysLocn = nodeSysLocn;
        m_nodeSysContact = nodeSysContact;
        m_nodeType = nodeType;
        m_operatingSystem = operatingSystem;
        m_foreignSourceId = foreignSourceId;
        m_foreignSource = foreignSource;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public int getNodeParent() {
        return m_nodeParent;
    }

    public String getLabel() {
        return m_label;
    }

    public String getDpName() {
        return m_dpname;
    }

    public String getNodeCreateTime() {
        return m_nodeCreateTime;
    }

    public String getNodeSysId() {
        return m_nodeSysId;
    }

    public String getNodeSysName() {
        return Util.htmlify(m_nodeSysName);
    }

    public String getNodeSysDescr() {
        return Util.htmlify(m_nodeSysDescr);
    }

    public String getNodeSysLocn() {
        return Util.htmlify(m_nodeSysLocn);
    }

    public String getNodeSysContact() {
        return Util.htmlify(m_nodeSysContact);
    }

    public char getNodeType() {
        return m_nodeType;
    }

    public String getOperatingSystem() {
        return Util.htmlify(m_operatingSystem);
    }

    public String getForeignSource() {
        return m_foreignSource;
    }
    
    public String getForeignSourceId() {
        return m_foreignSourceId;
    }
    
    public String toString() {
        StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n");
        str.append("Node Parent = " + m_nodeParent + "\n");
        str.append("Node Create Time = " + m_nodeCreateTime + "\n");
        str.append("Dp name = " + m_dpname + "\n");
        str.append("Node Sys Id = " + m_nodeSysId + "\n");
        str.append("Node Sys Name = " + m_nodeSysName + "\n");
        str.append("Node Sys Descr = " + m_nodeSysDescr + "\n");
        str.append("Node Sys Locn = " + m_nodeSysLocn + "\n");
        str.append("Node Sys Contact = " + m_nodeSysContact + "\n");
        str.append("Node Sys Type = " + m_nodeType + "\n");
        str.append("Operating System = " + m_operatingSystem + "\n");
        return str.toString();
    }

    public int compareTo(Node o) {
        String compareLabel = "";
        Integer compareId = 0;

        if (o != null) {
            compareLabel = o.getLabel();
            compareId = o.getNodeId();
        }

        int returnval = this.getLabel().compareToIgnoreCase(compareLabel);
        if (returnval == 0) {
            return (new Integer(this.getNodeId())).compareTo(compareId);
        }
        return returnval;
    }
}
