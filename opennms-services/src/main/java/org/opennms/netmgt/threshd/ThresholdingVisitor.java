/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements CollectionSetVisitor to implement thresholding.
 * Works by simply recording all the attributes that come in via visitAttribute
 * into an internal data structure, per resource, and then on "completeResource", does
 * threshold checking against that in memory structure.
 *
 * Suggested usage is one per CollectableService; this object holds the current state of thresholds
 * for this interface/service combination
 * (so perhaps needs a better name than ThresholdingVisitor)
 * 
 * Assumes and requires that the any visitation start at CollectionSet level, so that the collection timestamp can
 * be recorded. 
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @author <a href="mailto:craig@opennms.org">Craig Miskell</a>
 * @version $Id: $
 */
public class ThresholdingVisitor extends AbstractCollectionSetVisitor {

    public enum ThresholdingResult {
        THRESHOLDING_UNKNOWN,
        THRESHOLDING_SUCCEEDED,
        THRESHOLDING_FAILED
    }

    private static final Logger LOG = LoggerFactory.getLogger(ThresholdingVisitor.class);

	/**
     * Holds thresholds configuration for a node/interface/service
     */
    final CollectorThresholdingSet m_thresholdingSet;
    
    /**
     * Holds required attribute from CollectionResource to evaluate thresholds.
     */
    final Map<String, CollectionAttribute> m_attributesMap = new HashMap<String, CollectionAttribute>();

	private Date m_collectionTimestamp;

    /**
     * Static method create must be used to create new ThresholdingVisitor instance.
     * Is static because successful creation depends on thresholding-enabled parameter.
     *
     * @param nodeId a int.
     * @param hostAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param repo a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     * @param svcParams a {@link org.opennms.netmgt.collection.api.ServiceParameters} object.
     * @return a {@link org.opennms.netmgt.threshd.ThresholdingVisitor} object.
     * @throws ThresholdInitializationException 
     */
    public static ThresholdingVisitor create(int nodeId, String hostAddress, String serviceName, RrdRepository repo, ServiceParameters svcParams, ResourceStorageDao resourceStorageDao) throws ThresholdInitializationException {

        String enabled = ParameterMap.getKeyedString(svcParams.getParameters(), "thresholding-enabled", null);
        if (enabled != null && !"true".equals(enabled)) {
            LOG.info("create: Thresholds processing is not enabled. Check thresholding-enabled param on collectd package");
            return null;
        }

        CollectorThresholdingSet thresholdingSet = new CollectorThresholdingSet(nodeId, hostAddress, serviceName, repo, svcParams, resourceStorageDao);
        if (!thresholdingSet.hasThresholds()) {
            LOG.warn("create: the ipaddress/service {}/{} on node {} has no configured thresholds.", hostAddress, serviceName, nodeId);
        }

        return new ThresholdingVisitor(thresholdingSet);
    }

    /**
     * <p>Constructor for ThresholdingVisitor.</p>
     *
     * @param thresholdingSet a {@link org.opennms.netmgt.threshd.CollectorThresholdingSet} object.
     */
    protected ThresholdingVisitor(CollectorThresholdingSet thresholdingSet) {
        m_thresholdingSet = thresholdingSet;
        m_collectionTimestamp = new Date();
    }
    
    public void setCounterReset(boolean counterReset) {
        m_thresholdingSet.setCounterReset(counterReset);
    }

    public boolean hasThresholds() {
        return m_thresholdingSet.hasThresholds();
    }
    
    /**
     * Get a list of thresholds groups (for JUnit only at this time).
     *
     * @return a {@link java.util.List} object.
     */
    List<ThresholdGroup> getThresholdGroups() {
        return Collections.unmodifiableList(m_thresholdingSet.m_thresholdGroups);
    }
    
    /**
     * Get a list of scheduled outages (for JUnit only at this time).
     */
    List<String> getScheduledOutages() {
        return Collections.unmodifiableList(m_thresholdingSet.m_scheduledOutages);
    }

    @Override
    public void visitCollectionSet(CollectionSet set) {
        m_collectionTimestamp = set.getCollectionTimestamp();
    }
    
    /**
     * Force reload thresholds configuration, and merge threshold states
     */
    public void reload() {
        m_thresholdingSet.reinitialize();
    }

    public void reloadScheduledOutages() throws ThresholdInitializationException {
        m_thresholdingSet.updateScheduledOutages();
    }

    public boolean isNodeInOutage() {
        return m_thresholdingSet.isNodeInOutage();
    }

    /**
     *  Clear required attributes map
     */
    @Override
    public void visitResource(CollectionResource resource) {
        m_attributesMap.clear();
    }

    /**
     * Add/Update required attributes for thresholds on m_attributeMap.
     * This is used because {@link CollectionResource} does not have direct reference to their attributes.
     * (The way to get attribute is against {@link AttributeGroup} object contained on {@link CollectionResource}
     * implementations).
     */
    @Override    
    public void visitAttribute(CollectionAttribute attribute) {
        if (m_thresholdingSet.hasThresholds(attribute)) {
            String name = attribute.getName();
            m_attributesMap.put(name, attribute);
            LOG.debug("visitAttribute: storing value {} for attribute named {}",
                    attribute.getNumericValue() != null ? attribute.getNumericValue() : attribute.getStringValue(), name);
        }
    }

    /**
     * Apply threshold for specific resource (and required attributes).
     * Send thresholds events (if exists).
     */
    @Override
    public void completeResource(CollectionResource resource) {
        List<Event> eventList = m_thresholdingSet.applyThresholds(resource, m_attributesMap, m_collectionTimestamp);
        ThresholdingEventProxy proxy = ThresholdingEventProxyFactory.getFactory().getProxy();
        proxy.add(eventList);
        proxy.sendAllEvents();
    }
    
    /**
     * Return the collection timestamp passed in at construct time.  Typically used by tests, but might be  useful elsewhere
     */
    public Date getCollectionTimestamp() {
    	return this.m_collectionTimestamp;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ThresholdingVisitor for " + m_thresholdingSet;
    }
    
}
