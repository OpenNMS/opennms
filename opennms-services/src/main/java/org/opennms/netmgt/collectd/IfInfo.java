
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.util.Map;

import org.opennms.core.utils.AlphaNumeric;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourcePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class encapsulates all the information required by the SNMP collector in
 * order to perform data collection for an individual interface and store that
 * data in an appropriately named RRD file.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class IfInfo extends SnmpCollectionResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(IfInfo.class);
    
    private SNMPCollectorEntry m_entry;
    private String m_ifAlias;
    private final SnmpIfData m_snmpIfData;

    /**
     * <p>Constructor for IfInfo.</p>
     *
     * @param def a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     * @param snmpIfData a {@link org.opennms.netmgt.collectd.SnmpIfData} object.
     */
    public IfInfo(ResourceType def, CollectionAgent agent, SnmpIfData snmpIfData) {
        super(def);
        m_snmpIfData = snmpIfData;
        m_ifAlias = snmpIfData.getIfAlias();
    }
    
    public int getNodeId() {
        return m_snmpIfData.getNodeId();
    }

    /**
     * <p>getIndex</p>
     *
     * @return a int.
     */
    public int getIndex() {
        return m_snmpIfData.getIfIndex();
    }

    /**
     * <p>getType</p>
     *
     * @return a int.
     */
    @Override
    public int getSnmpIfType() {
        return m_snmpIfData.getIfType();
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInterfaceLabel() {
        return m_snmpIfData.getLabelForRRD();
    }

    /**
     * <p>setIfAlias</p>
     *
     * @param ifAlias a {@link java.lang.String} object.
     */
    public void setIfAlias(String ifAlias) {
        m_ifAlias = ifAlias;
    }

    String getCurrentIfAlias() {
        return m_ifAlias;
    }

    /**
     * <p>isCollectionEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isCollectionEnabled() {
        return m_snmpIfData.isCollectionEnabled();
    }

    /**
     * <p>setEntry</p>
     *
     * @param ifEntry a {@link org.opennms.netmgt.collectd.SNMPCollectorEntry} object.
     */
    public void setEntry(SNMPCollectorEntry ifEntry) {
        m_entry = ifEntry;
    }

    /**
     * <p>getEntry</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.SNMPCollectorEntry} object.
     */
    protected SNMPCollectorEntry getEntry() {
        return m_entry;
    }

    /**
     * <p>getAttributesMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String,String> getAttributesMap() {
        return m_snmpIfData.getAttributesMap();
    }

    /**
     ** @deprecated
     **/

    String getNewIfAlias() {
        // FIXME: This should not be null
        if (getEntry() == null) {
            return getCurrentIfAlias();
        }
        return getEntry().getValueForBase(SnmpCollector.IFALIAS_OID);
    }

    boolean currentAliasIsOutOfDate(String ifAlias) {
        LOG.debug("currentAliasIsOutOfDate: ifAlias from collection = {}, current ifAlias = {}", ifAlias, getCurrentIfAlias());
        return ifAlias != null && !ifAlias.equals(getCurrentIfAlias());
    }

    void logAlias(String ifAlias) {
        LOG.debug("Alias for RRD directory name = {}", ifAlias);
    }

    String getAliasDir(String ifAlias, String ifAliasComment) {
        if (ifAlias != null) {
            if (ifAliasComment != null) {
                int si = ifAlias.indexOf(ifAliasComment);
                if (si > -1) {
                    ifAlias = ifAlias.substring(0, si).trim();
                }
            }
            if (ifAlias != null) {
                ifAlias = AlphaNumeric.parseAndReplaceExcept(ifAlias,
                                                             SnmpCollector.nonAnRepl, SnmpCollector.AnReplEx);
            }
        }

        logAlias(ifAlias);

        return ifAlias;
    }

    void logForceRescan(String ifAlias) {

        LOG.debug("Forcing rescan.  IfAlias {} for index {} does not match DB value: {}", ifAlias, getIndex(), getCurrentIfAlias());
    }

    public boolean isScheduledForCollection() {
        LOG.debug("{} .collectionEnabled = {}", this, isCollectionEnabled());
        LOG.debug("selectCollectionOnly = {}", getCollection().isSelectCollectionOnly());

        boolean isScheduled = isCollectionEnabled() || !getCollection().isSelectCollectionOnly();
        
        LOG.debug("isScheduled = {}", isScheduled);

        return isScheduled;

    }

    private OnmsSnmpCollection getCollection() {
        return getResourceType().getCollection();
    }

    @Override
    public ResourcePath getPath() {
        String label = getInterfaceLabel();
        if (label == null || "".equals(label)) {
            throw new IllegalStateException("Could not construct resource directory because interface label is null or blank: nodeId: " + getNodeId());
        }
        return ResourcePath.get(getCollectionAgent().getStorageResourcePath(), label);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "node["+ getNodeId() + "].interfaceSnmp[" + getInterfaceLabel() + ']';
    }

    boolean shouldStore(ServiceParameters serviceParameters) {
        if ("normal".equalsIgnoreCase(serviceParameters.getStoreByNodeID())) {
            return isScheduledForCollection();
        } else {
            return "true".equalsIgnoreCase(serviceParameters.getStoreByNodeID());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(ServiceParameters serviceParameters) {

        boolean shdprsist = shouldStore(serviceParameters) && (isScheduledForCollection() || serviceParameters.forceStoreByAlias(getCurrentIfAlias()));
        LOG.debug("shouldPersist = {}", shdprsist);
        return shdprsist;
    }
    
    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getResourceTypeName() {
        return CollectionResource.RESOURCE_TYPE_IF; //This is IfInfo, must be an interface
    }
    
    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInstance() {
        return Integer.toUnsignedString(getIndex()); //For interfaces, use ifIndex as it's unique within a node (by definition)
    }

    @Override
    public ResourcePath getParent() {
        return getCollectionAgent().getStorageResourcePath();
    }

} // end class
