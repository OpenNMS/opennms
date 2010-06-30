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
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.6.12
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
     * <p>Setter for the field <code>address</code>.</p>
     *
     * @param newAddress a {@link java.lang.String} object.
     */
    public void setAddress(String newAddress) {
        address = newAddress;
    }

    /**
     * <p>Getter for the field <code>address</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAddress() {
        return address;
    }

    /**
     * <p>Setter for the field <code>nodeid</code>.</p>
     *
     * @param id a int.
     */
    public void setNodeid(int id) {
        nodeid = id;
    }

    /**
     * <p>Getter for the field <code>nodeid</code>.</p>
     *
     * @return a int.
     */
    public int getNodeid() {
        return nodeid;
    }

    /**
     * <p>Setter for the field <code>ifIndex</code>.</p>
     *
     * @param index a int.
     */
    public void setIfIndex(int index) {
        ifIndex = index;
    }

    /**
     * <p>Getter for the field <code>ifIndex</code>.</p>
     *
     * @return a int.
     */
    public int getIfIndex() {
        return ifIndex;
    }

    /**
     * <p>setIpHostname</p>
     *
     * @param newIpHostname a {@link java.lang.String} object.
     */
    public void setIpHostname(String newIpHostname) {
        iphostname = newIpHostname;
    }

    /**
     * <p>getIpHostname</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpHostname() {
        return iphostname;
    }

    /**
     * <p>setStatus</p>
     *
     * @param newStatus a {@link java.lang.String} object.
     */
    public void setStatus(String newStatus) {
        snmpstatus = newStatus;
    }

    /**
     * <p>getStatus</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatus() {
        return snmpstatus;
    }

    /**
     * <p>Setter for the field <code>ifDescr</code>.</p>
     *
     * @param newIfDescr a {@link java.lang.String} object.
     */
    public void setIfDescr(String newIfDescr) {
        ifDescr = newIfDescr;
    }

    /**
     * <p>Getter for the field <code>ifDescr</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfDescr() {
        return ifDescr;
    }

    /**
     * <p>Setter for the field <code>ifType</code>.</p>
     *
     * @param newIfType a int.
     */
    public void setIfType(int newIfType) {
        ifType = newIfType;
    }

    /**
     * <p>Getter for the field <code>ifType</code>.</p>
     *
     * @return a int.
     */
    public int getIfType() {
        return ifType;
    }

    /**
     * <p>Setter for the field <code>ifName</code>.</p>
     *
     * @param newIfName a {@link java.lang.String} object.
     */
    public void setIfName(String newIfName) {
        ifName = newIfName;
    }

    /**
     * <p>Getter for the field <code>ifName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfName() {
        return ifName;
    }

    /**
     * <p>Getter for the field <code>ifAlias</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfAlias() {
        return ifAlias;
    }

    /**
     * <p>Setter for the field <code>ifAlias</code>.</p>
     *
     * @param newIfAlias a {@link java.lang.String} object.
     */
    public void setIfAlias(String newIfAlias) {
        ifAlias = newIfAlias;
    }
}
