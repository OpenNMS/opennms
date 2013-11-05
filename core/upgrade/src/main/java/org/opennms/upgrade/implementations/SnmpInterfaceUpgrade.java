/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.upgrade.implementations;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.opennms.core.utils.RrdLabelUtils;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.model.OnmsResource;

/**
 * The Class SnmpInterfaceUpgrade.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class SnmpInterfaceUpgrade {

    /** The node's directory. */
    private File nodeDir;

    /** The node's id. */
    private int nodeId;

    /** The node's foreign id. */
    private String foreignId;

    /** The node's foreign source. */
    private String foreignSource;

    /** The SNMP interface name. */
    private String ifName;

    /** The SNMP interface description. */
    private String ifDescr;

    /** The SNMP physical address. */
    private String physAddr;

    /** The old RRD label. */
    private String oldRrdLabel;

    /** The new RRD label. */
    private String newRrdLabel;

    /**
     * Instantiates a new SNMP interface upgrade.
     *
     * @param rs the ResultSet
     * @throws SQLException the sQL exception
     */
    public SnmpInterfaceUpgrade(ResultSet rs) throws SQLException {
        nodeId = rs.getInt("nodeid");
        foreignSource = rs.getString("foreignsource");
        foreignId = rs.getString("foreignid");
        ifDescr = rs.getString("snmpifdescr");
        ifName = rs.getString("snmpifname");
        physAddr = rs.getString("snmpphysaddr");
        initialize();
    }

    /**
     * Instantiates a new SNMP interface upgrade.
     *
     * @param nodeId the node id
     * @param foreignId the foreign id
     * @param foreignSource the foreign source
     * @param ifName the SNMP interface name
     * @param ifDescr the SNMP interface description
     * @param physAddr the SNMP physical address
     */
    public SnmpInterfaceUpgrade(int nodeId, String foreignSource,
            String foreignId, String ifDescr, String ifName,
            String physAddr) {
        this.nodeId = nodeId;
        this.foreignSource = foreignSource;
        this.foreignId = foreignId;
        this.ifDescr = ifDescr;
        this.ifName = ifName;
        this.physAddr = physAddr;
        initialize();
    }

    /**
     * Initialize.
     */
    private void initialize() {
        oldRrdLabel = RrdLabelUtils.computeLabelForRRD(ifName, ifDescr, null);
        newRrdLabel = RrdLabelUtils.computeLabelForRRD(ifName, ifDescr, physAddr);
        nodeDir = getNodeDirectory(nodeId, foreignSource, foreignId);
    }

    /**
     * Gets the node directory.
     *
     * @return the node directory
     */
    public File getNodeDir() {
        return nodeDir;
    }

    /**
     * Gets the old interface directory.
     *
     * @return the old interface directory
     */
    public File getOldInterfaceDir() {
        return new File(getNodeDir(), getOldRrdLabel());
    }

    /**
     * Gets the new interface directory.
     *
     * @return the new interface directory
     */
    public File getNewInterfaceDir() {
        return new File(getNodeDir(), getNewRrdLabel());
    }

    /**
     * Gets the node id.
     *
     * @return the node id
     */
    public int getNodeId() {
        return nodeId;
    }

    /**
     * Gets the foreign id.
     *
     * @return the foreign id
     */
    public String getForeignId() {
        return foreignId;
    }

    /**
     * Gets the foreign source.
     *
     * @return the foreign source
     */
    public String getForeignSource() {
        return foreignSource;
    }

    /**
     * Gets the interface name.
     *
     * @return the interface name
     */
    public String getIfName() {
        return ifName;
    }

    /**
     * Gets the interface description.
     *
     * @return the interface description
     */
    public String getIfDescr() {
        return ifDescr;
    }

    /**
     * Gets the physical address.
     *
     * @return the physical address
     */
    public String getPhysAddr() {
        return physAddr;
    }

    /**
     * Gets the old RRD label.
     *
     * @return the old RRD label
     */
    public String getOldRrdLabel() {
        return oldRrdLabel;
    }

    /**
     * Gets the new RRD label.
     *
     * @return the new RRD label
     */
    public String getNewRrdLabel() {
        return newRrdLabel;
    }

    /**
     * Checks the should merge flag.
     *
     * @return true, if the interface directory should be merged
     */
    public boolean shouldMerge() {
        // An SnmpInterfaceUpgrade entry only exist for SNMP interfaces with MAC Address.
        // For this reason, if the old directory exist, that means, the interface statistics must be merged.
        return getOldInterfaceDir().exists();
    }

    /**
     * Gets the old resource id.
     *
     * @return the old resource id
     */
    public String getOldResourceId() {
        return OnmsResource.createResourceId("node", Integer.toString(nodeId), "interfaceSnmp", oldRrdLabel);
    }

    /**
     * Gets the new resource id.
     *
     * @return the new resource id
     */
    public String getNewResourceId() {
        return OnmsResource.createResourceId("node", Integer.toString(nodeId), "interfaceSnmp", newRrdLabel);
    }

    /**
     * Gets the node directory.
     *
     * @param nodeId the node id
     * @param foreignSource the foreign source
     * @param foreignId the foreign id
     * @return the node directory
     */
    protected File getNodeDirectory(int nodeId, String foreignSource, String foreignId) {
        String rrdPath = DataCollectionConfigFactory.getInstance().getRrdPath();
        File dir = new File(rrdPath, String.valueOf(nodeId));
        if (Boolean.getBoolean("org.opennms.rrd.storeByForeignSource") && !(foreignSource == null) && !(foreignId == null)) {
            File fsDir = new File(rrdPath, "fs" + File.separatorChar + foreignSource);
            dir = new File(fsDir, foreignId);
        }
        return dir;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Interface[ifName=" + getIfName() + ", nodeId=" + getNodeId() + ", foreignSource=" + getForeignSource() + ", foreignId=" + getForeignId() + "]";
    }

}
