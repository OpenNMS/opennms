//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.netmgt.threshd;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.DbIpInterfaceEntry;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.utils.RowProcessor;

public class SnmpThresholdInterface {
    
    private static final String SNMP_THRESH_IFACE_KEY = SnmpThresholdInterface.class.getName();

    public static SnmpThresholdInterface get(NetworkInterface iface) {
        
        SnmpThresholdInterface snmpIface = (SnmpThresholdInterface) iface.getAttribute(SNMP_THRESH_IFACE_KEY);
        if (snmpIface == null) {
            snmpIface = new SnmpThresholdInterface(iface);
            iface.setAttribute(SNMP_THRESH_IFACE_KEY, snmpIface);
        }
        
        return snmpIface;
    }

    private NetworkInterface m_netInterface;
    /**
     * Interface attribute key used to store the interface's node id
     */
    static final String NODE_ID_KEY = "org.opennms.netmgt.collectd.SnmpThresholder.NodeId";
    
    static final String PRIMARY_IFINDEX_KEY = "org.opennms.netmgt.collectd.SnmpThresholder.primaryIfIndex";

    static final String IS_SNMP_PRIMARY_KEY = "org.opennms.netmgt.collectd.SnmpThresholder.isSnmpPrimary";
/**
     * We must maintain a map of interface level ThresholdEntity objects on a
     * per interface basis in order to maintain separate exceeded counts and the
     * like for each of a node's interfaces. This interface attribute key used
     * to store a map of interface level ThresholdEntity object maps keyed by
     * ifLabel. So it wil refer to a map of maps indexed by ifLabel.
     */
    static final String ALL_IF_THRESHOLD_MAP_KEY = "org.opennms.netmgt.collectd.SnmpThresholder.AllIfThresholdMap";

    

    public SnmpThresholdInterface(NetworkInterface iface) {
        m_netInterface = iface;
        initialize();
    }

    public NetworkInterface getNetworkInterface() {
        return m_netInterface;
    }

    boolean isIPV4() {
        return getNetworkInterface().getType() == NetworkInterface.TYPE_IPV4;
    }

    InetAddress getInetAddress() {
        return (InetAddress) getNetworkInterface().getAddress();
    }

    String getIpAddress() {
        return getInetAddress().getHostAddress();
    }

    void setNodeId(Integer nodeId) {
        getNetworkInterface().setAttribute(SnmpThresholdInterface.NODE_ID_KEY, nodeId);
    }

    Integer getNodeId() {
        return (Integer) getNetworkInterface().getAttribute(SnmpThresholdInterface.NODE_ID_KEY);
    }
    
    void setPrimaryIfIndex(Integer primaryIfIndex) {
        getNetworkInterface().setAttribute(SnmpThresholdInterface.PRIMARY_IFINDEX_KEY, primaryIfIndex);
    }
    
    Integer getPrimaryIfIndex() {
        return (Integer) getNetworkInterface().getAttribute(SnmpThresholdInterface.PRIMARY_IFINDEX_KEY);
    }
    
    void setIsSnmpPrimary(String isSnmpPrimary) {
        if (isSnmpPrimary == null || isSnmpPrimary.length() < 1)
            setIsSnmpPrimary(DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE);
        else
            setIsSnmpPrimary(isSnmpPrimary.charAt(0));
    }
    
    void setIsSnmpPrimary(char isSnmpPrimary) {
        getNetworkInterface().setAttribute(SnmpThresholdInterface.IS_SNMP_PRIMARY_KEY, new Character(isSnmpPrimary));
    }
    
    char getIsSnmpPrimary() {
        Character val = (Character) getNetworkInterface().getAttribute(SnmpThresholdInterface.IS_SNMP_PRIMARY_KEY);
        return val == null ? DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE : val.charValue();
    }

    void initialize() {
        
        Querier querier = new Querier(DataSourceFactory.getDataSource(), SnmpThresholder.SQL_GET_NODEID, new RowProcessor() {
        
            public void processRow(ResultSet rs) throws SQLException {
                setNodeId(SnmpThresholdInterface.getInteger(rs, 1));
                setPrimaryIfIndex(SnmpThresholdInterface.getInteger(rs, 2));
                setIsSnmpPrimary(rs.getString(3));
            }
            
        });
        querier.execute(getIpAddress());
        
        
        
        if (log().isDebugEnabled())
            log().debug("initialize: db retrieval info: nodeid = " + getNodeId() + ", address = " + getIpAddress() + ", ifIndex = " + getPrimaryIfIndex() + ", isSnmpPrimary = " + getIsSnmpPrimary());
        
        // RuntimeException is thrown if any of the following are true:
        // - node id is invalid
        // - primaryIfIndex is invalid
        // - Interface is not the primary SNMP interface for the node
        //
        if (getNodeId() == null)
            throw new RuntimeException("Unable to retrieve node id for interface " + getIpAddress());
        
        if (getPrimaryIfIndex() == null)
            // allow this for nodes without ipAddrTables
            // throw new RuntimeException("Unable to retrieve ifIndex for interface " + ipAddr.getHostAddress());
            if (log().isDebugEnabled())
                log().debug("initialize: db retrieval info: node " + getNodeId() + " does not have a legitimate primaryIfIndex. Assume node does not supply ipAddrTable and continue...");
        
        if (getIsSnmpPrimary() != DbIpInterfaceEntry.SNMP_PRIMARY)
            throw new RuntimeException("Interface " + getIpAddress() + " is not the primary SNMP interface for nodeid " + getNodeId());
        // Add nodeId as an attribute of the interface for retrieval
        // by the check() method.
        //
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    static Integer getInteger(ResultSet rs, int columnIndex) throws SQLException {
        int val = rs.getInt(columnIndex);
        if (rs.wasNull())
            return null;
        return new Integer(val);
    }



}
