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
