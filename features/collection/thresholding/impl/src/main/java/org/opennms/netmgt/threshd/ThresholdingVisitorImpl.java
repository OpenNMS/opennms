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
package org.opennms.netmgt.threshd;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.opennms.netmgt.threshd.api.ThresholdingEventProxy;
import org.opennms.netmgt.threshd.api.ThresholdingVisitor;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

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
public class ThresholdingVisitorImpl extends AbstractCollectionSetVisitor implements ThresholdingVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(ThresholdingVisitor.class);

	/**
     * Holds thresholds configuration for a node/interface/service
     */
    final ThresholdingSetImpl m_thresholdingSet;
    
    /**
     * Holds required attribute from CollectionResource to evaluate thresholds.
     */
    final Map<String, CollectionAttribute> m_attributesMap = new HashMap<String, CollectionAttribute>();

	private Date m_collectionTimestamp;

    private ThresholdingEventProxy m_thresholdingEventProxy;
    
    private final Long m_sequenceNumber;

    protected ThresholdingVisitorImpl(ThresholdingSetImpl thresholdingSet,
                                      ThresholdingEventProxy eventProxy, Long sequenceNumber) {
        m_thresholdingSet = thresholdingSet;
        m_thresholdingEventProxy = eventProxy;
        m_collectionTimestamp = new Date();
        m_sequenceNumber = sequenceNumber;
    }
    
    public void setCounterReset(boolean counterReset) {
        m_thresholdingSet.setCounterReset(counterReset);
    }

    public boolean hasThresholds() {
        return m_thresholdingSet.hasThresholds();
    }
    
    @VisibleForTesting
    List<ThresholdGroup> getThresholdGroups() {
        return Collections.unmodifiableList(m_thresholdingSet.getThresholdGroups());
    }

    @VisibleForTesting
    List<String> getScheduledOutages() {
        return Collections.unmodifiableList(m_thresholdingSet.getscheduledOutages());
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
        final String name = attribute.getName();
        m_attributesMap.put(name, attribute);
        LOG.debug("visitAttribute: storing value {} for attribute named {}",
                attribute.getNumericValue() != null ? attribute.getNumericValue() : attribute.getStringValue(), name);
    }

    /**
     * Apply threshold for specific resource (and required attributes).
     * Send thresholds events (if exists).
     */
    @Override
    public void completeResource(CollectionResource resource) {
        List<Event> eventList = m_thresholdingSet.applyThresholds(resource, m_attributesMap, m_collectionTimestamp,
                m_sequenceNumber);
        for (Event event : eventList) {
            m_thresholdingEventProxy.sendEvent(event);
        }
    }
    
    @VisibleForTesting
    public Date getCollectionTimestamp() {
    	return this.m_collectionTimestamp;
    }

    public ThresholdingEventProxy getEventProxy() {
        return m_thresholdingEventProxy;
    }

    public void setEventProxy(ThresholdingEventProxy eventProxy) {
        this.m_thresholdingEventProxy = eventProxy;
    }

    @Override
    public String toString() {
        return "ThresholdingVisitor for " + m_thresholdingSet;
    }

    public int getNodeId() {
        return m_thresholdingSet.getNodeId();
    }
    
}
