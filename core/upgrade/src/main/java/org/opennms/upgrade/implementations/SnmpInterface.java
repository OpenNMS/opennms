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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.RrdLabelUtils;
import org.opennms.netmgt.model.ResourceId;

/**
 * The Class SnmpInterface.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class SnmpInterface {

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

    /** The store by foreign source. */
    private boolean storeByForeignSource;

    /**
     * Instantiates a new SNMP interface.
     *
     * @param rs the ResultSet
     * @param storeByForeignSource true, if the store by foreign source is enabled
     * @throws SQLException the SQL exception
     */
    public SnmpInterface(ResultSet rs, boolean storeByForeignSource) throws SQLException {
        this(rs.getInt("nodeid"), rs.getString("foreignsource"), rs.getString("foreignid"), rs.getString("snmpifdescr"), rs.getString("snmpifname") ,rs.getString("snmpphysaddr"), storeByForeignSource);
    }

    /**
     * Instantiates a new SNMP interface.
     *
     * @param nodeId the node id
     * @param foreignSource the foreign source
     * @param foreignId the foreign id
     * @param ifDescr the SNMP interface description
     * @param ifName the SNMP interface name
     * @param physAddr the SNMP physical address
     * @param storeByForeignSource true, if store by foreign source is enabled
     */
    public SnmpInterface(int nodeId, String foreignSource,
            String foreignId, String ifDescr, String ifName,
            String physAddr, boolean storeByForeignSource) {
        this.nodeId = nodeId;
        this.foreignSource = normalize(foreignSource);
        this.foreignId = normalize(foreignId);
        this.ifDescr = normalize(ifDescr);
        this.ifName = normalize(ifName);
        this.physAddr = normalize(physAddr);
        this.storeByForeignSource = storeByForeignSource;
        initialize();
    }

    /**
     * Normalizes a String.
     *
     * @param source the source
     * @return the normalized string
     */
    private String normalize(String source) {
        return StringUtils.isBlank(source) ? null : StringUtils.trim(source);
    }

    /**
     * Initialize.
     */
    protected void initialize() {
        // If the ifName and the ifDescr are null at the same time, OpenNMS is going to create a directory like this:
        // /var/opennms/rrd/snmp/10/null or /var/opennms/rrd/snmp/10/null-00029906ced7
        if (ifDescr == null && ifName == null) {
            ifName = "null";
        }
        oldRrdLabel = RrdLabelUtils.computeLabelForRRD(ifName, ifDescr, null);
        newRrdLabel = RrdLabelUtils.computeLabelForRRD(ifName, ifDescr, physAddr);
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
     * Gets the old resource id.
     *
     * @return the old resource id
     */
    public ResourceId getOldResourceId() {
        return getResourceId(oldRrdLabel);
    }

    /**
     * Gets the new resource id.
     *
     * @return the new resource id
     */
    public ResourceId getNewResourceId() {
        return getResourceId(newRrdLabel);
    }

    /**
     * Gets the resource id.
     *
     * @param label the label
     * @return the resource id
     */
    private ResourceId getResourceId(String label) {
        String parentType = storeByForeignSource ? "nodeSource" : "node";
        String parentId   = storeByForeignSource ? foreignSource + ':' + foreignId : Integer.toString(nodeId);
        return ResourceId.get(parentType, parentId).resolve("interfaceSnmp", label);
    }

    /**
     * Checks if the resourceId should be updated.
     *
     * @param resourceId the resource id to check
     * @return true, if the resource should be updated
     */
    public boolean shouldUpdate(String resourceId) {
        return resourceId.endsWith("interfaceSnmp[" + oldRrdLabel + "]");
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Interface[ifName=" + getIfName() + ", nodeId=" + getNodeId() + ", foreignSource=" + getForeignSource() + ", foreignId=" + getForeignId() + "]";
    }

}
