/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

import org.opennms.netmgt.config.DataCollectionConfigFactory;

/**
 * The Class SnmpInterfaceUpgrade.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class SnmpInterfaceUpgrade extends SnmpInterface {

    /** The node's directory. */
    private File nodeDir;

    /**
     * Instantiates a new SNMP interface upgrade.
     *
     * @param nodeId the node id
     * @param foreignSource the foreign source
     * @param foreignId the foreign id
     * @param ifDescr the SNMP interface description
     * @param ifName the SNMP interface name
     * @param physAddr the SNMP physical address
     * @param storeByForeignSource true, if store by foreign source is enabled
     */
    public SnmpInterfaceUpgrade(int nodeId, String foreignSource,
            String foreignId, String ifDescr, String ifName, String physAddr,
            boolean storeByForeignSource) {
        super(nodeId, foreignSource, foreignId, ifDescr, ifName, physAddr, storeByForeignSource);
    }

    /**
     * Instantiates a new SNMP interface upgrade.
     *
     * @param rs the ResultSet
     * @param storeByForeignSource true, if the store by foreign source is enabled
     * @throws SQLException the SQL exception
     */
    public SnmpInterfaceUpgrade(ResultSet rs, boolean storeByForeignSource) throws SQLException {
        super(rs, storeByForeignSource);
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.implementations.SnmpInterface#initialize()
     */
    protected void initialize() {
        super.initialize();
        nodeDir = getNodeDirectory(getNodeId(), getForeignSource(), getForeignId());
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
     * Checks if the interface directories should be merged.
     *
     * @return true, if the interface directory should be merged
     */
    public boolean shouldMerge() {
        // An SnmpInterfaceUpgrade entry only exist for SNMP interfaces with MAC Address.
        // For this reason, if the old directory exist and the label of the old interface is different than the new one,
        // that means, the interface statistics must be merged.
        return getOldInterfaceDir().exists() && !getOldRrdLabel().equals(getNewRrdLabel());
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
        return "Interface[ifName=" + getIfName() + ", nodeId=" + getNodeId() + ", foreignSource=" + getForeignSource() + ", foreignId=" + getForeignId() + ", directory=" + getNewInterfaceDir().toString() + "]";
    }

}
