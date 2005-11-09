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
// Modifications:
//
// 2002 Sep 24: Added the ability to select SNMP interfaces for collection.
//              Code based on original manage/unmanage code.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.admin.nodeManagement;

/**
 * A servlet that stores interface information used in setting up SNMP Data
 * Collection
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class SnmpManagedInterface {
    /**
     * 
     */
    protected String address;

    /**
     * 
     */
    protected int nodeid;

    /**
     * 
     */
    protected int ifIndex;

    /**
     * 
     */
    protected String iphostname;

    /**
     * 
     */
    protected String snmpstatus;

    /**
     * 
     */
    protected String ifDescr;

    /**
     * 
     */
    protected int ifType;

    /**
     * 
     */
    protected String ifName;

    /**
     * 
     */
    protected String ifAlias;

    /**
     * 
     */
    public void setAddress(String newAddress) {
        address = newAddress;
    }

    /**
     */
    public String getAddress() {
        return address;
    }

    /**
     * 
     */
    public void setNodeid(int id) {
        nodeid = id;
    }

    /**
     * 
     */
    public int getNodeid() {
        return nodeid;
    }

    /**
     * 
     */
    public void setIfIndex(int index) {
        ifIndex = index;
    }

    /**
     * 
     */
    public int getIfIndex() {
        return ifIndex;
    }

    /**
     * 
     */
    public void setIpHostname(String newIpHostname) {
        iphostname = newIpHostname;
    }

    /**
     * 
     */
    public String getIpHostname() {
        return iphostname;
    }

    /**
     * 
     */
    public void setStatus(String newStatus) {
        snmpstatus = newStatus;
    }

    /**
     * 
     */
    public String getStatus() {
        return snmpstatus;
    }

    /**
     * 
     */
    public void setIfDescr(String newIfDescr) {
        ifDescr = newIfDescr;
    }

    /**
     * 
     */
    public String getIfDescr() {
        return ifDescr;
    }

    /**
     * 
     */
    public void setIfType(int newIfType) {
        ifType = newIfType;
    }

    /**
     * 
     */
    public int getIfType() {
        return ifType;
    }

    /**
     * 
     */
    public void setIfName(String newIfName) {
        ifName = newIfName;
    }

    /**
     * 
     */
    public String getIfName() {
        return ifName;
    }

    /**
     * 
     */
    public String getIfAlias() {
        return ifAlias;
    }

    /**
     * 
     */
    public void setIfAlias(String newIfAlias) {
        ifAlias = newIfAlias;
    }
}
