/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2018 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigManager;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.opennms.netmgt.config.threshd.FilterOperator;
import org.opennms.netmgt.config.threshd.ResourceFilter;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract ThresholdingSet class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class ThresholdingSet {

    private static final Logger LOG = LoggerFactory.getLogger(ThresholdingSet.class);

    protected final int m_nodeId;
    protected final String m_hostAddress;
    protected final String m_serviceName;
    protected final RrdRepository m_repository;

    protected ThresholdsDao m_thresholdsDao;

    private boolean m_initialized = false;
    private boolean m_hasThresholds = false;

    protected final List<ThresholdGroup> m_thresholdGroups = new LinkedList<>();
    protected final List<String> m_scheduledOutages = new ArrayList<>();

    /**
     * <p>Constructor for ThresholdingSet.</p>
     *
     * @param nodeId a int.
     * @param hostAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param repository a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     * @param interval a long.
     * @throws ThresholdInitializationException 
     */
    public ThresholdingSet(int nodeId, String hostAddress, String serviceName, RrdRepository repository) throws ThresholdInitializationException {
        m_nodeId = nodeId;
        m_hostAddress = (hostAddress == null ? null : hostAddress.intern());
        m_serviceName = (serviceName == null ? null : serviceName.intern());
        m_repository = repository;
        initThresholdsDao();
        initialize();
        if (!m_initialized) {
            throw new ThresholdInitializationException("Failed to initialize thresholding set.");
        }
    }

    /**
     * <p>initialize</p>
     * @throws ThresholdInitializationException 
     */
    protected void initialize() throws ThresholdInitializationException {
        final String logHeader = "initialize(nodeId=" + m_nodeId + ",ipAddr=" + m_hostAddress + ",svc=" + m_serviceName + ")";
        final List<String> groupNameList = getThresholdGroupNames(m_nodeId, m_hostAddress, m_serviceName);
        synchronized(m_thresholdGroups) {
            m_thresholdGroups.clear();
            for (final String groupName : groupNameList) {
                try {
                    final ThresholdGroup thresholdGroup = m_thresholdsDao.get(groupName);
                    if (thresholdGroup == null) {
                        LOG.error("{}: Could not get threshold group with name {}", logHeader, groupName);
                    } else {
                        m_thresholdGroups.add(thresholdGroup);
                        LOG.debug("{}: Adding threshold group: {}", logHeader, thresholdGroup);
                    }
                } catch (Throwable e) {
                    LOG.error("{}: Can't process threshold group {}", logHeader, groupName, e);
                    // It should not throw an exception when we are unable to find the group, see NMS-3532
                }
            }
            m_hasThresholds = !m_thresholdGroups.isEmpty();
        }
        updateScheduledOutages();
    }

    public void reinitialize() {
        reinitialize(false);
    }

    /**
     * <p>reinitialize</p>
     */
    public void reinitialize(final boolean reloadThresholdConfig) {
        m_initialized = false;
        ThresholdingEventProxyFactory.getFactory().getProxy().removeAllEvents();

        final ThresholdingConfigFactory tcf = ThresholdingConfigFactory.getInstance();
        final boolean hasThresholds = m_hasThresholds;
        final List<ThresholdGroup> thresholdGroups = new ArrayList<>(m_thresholdGroups);
        final List<String> scheduledOutages = new ArrayList<>(m_scheduledOutages);
        try {
            if (reloadThresholdConfig) {
                ThresholdingConfigFactory.reload();
            }
            initThresholdsDao();
            mergeThresholdGroups(m_nodeId, m_hostAddress, m_serviceName);
            updateScheduledOutages();
        } catch (final Exception e) {
            LOG.error("Failed to reinitialize thresholding set.  Reverting to previous configuration.", e);
            ThresholdingConfigFactory.setInstance(tcf);
            m_hasThresholds = hasThresholds;
            if (!thresholdGroups.equals(m_thresholdGroups)) {
                m_thresholdGroups.clear();
                m_thresholdGroups.addAll(thresholdGroups);
            }
            if (!scheduledOutages.equals(m_scheduledOutages)) {
                m_scheduledOutages.clear();
                m_scheduledOutages.addAll(scheduledOutages);
            }
            m_initialized = true;
        }
        ThresholdingEventProxyFactory.getFactory().getProxy().sendAllEvents();
    }

    /*
     * Used to reload merge new thresholds configuration with current.
     * 
     * Extract thresholdEvaluatorStates Map from each ThresholdEntity, then copy this to new thresholdEntity.
     */
    /**
     * <p>mergeThresholdGroups</p>
     * @throws ThresholdInitializationException 
     */
    private void mergeThresholdGroups(final int nodeId, final String hostAddress, final String serviceName) throws ThresholdInitializationException {
        final String logHeader = "mergeThresholdGroups(nodeId=" + nodeId + ",ipAddr=" + hostAddress + ",svc=" + serviceName + ")";
        LOG.debug("{}: Begin merging operation", logHeader);
        final List<String> existingGroupNameList = m_thresholdGroups.stream().map(ThresholdGroup::getName).collect(Collectors.toList());
        List<String> newGroupNameList = getThresholdGroupNames(nodeId, hostAddress, serviceName);

        synchronized(m_thresholdGroups) {
            // If size differs its because some groups where deleted.
            if (newGroupNameList.size() != m_thresholdGroups.size() || !existingGroupNameList.equals(newGroupNameList)) {
                // Deleting Groups
                LOG.debug("{}: New group name list differs from current threshold group list", logHeader);
                for (final Iterator<ThresholdGroup> i = m_thresholdGroups.iterator(); i.hasNext();) {
                    final ThresholdGroup group = i.next();
                    if (!newGroupNameList.contains(group.getName())) {
                        LOG.info("{}: deleting group {}", logHeader, group);
                        group.delete();
                        i.remove();
                    }
                }
            }
            final List<ThresholdGroup> newThresholdGroupList = new LinkedList<>();
            for (final String groupName : newGroupNameList) {
                // Check if group exist on current configured list
                final Optional<ThresholdGroup> foundGroup = m_thresholdGroups.stream().filter(tg -> groupName.equals(tg.getName())).findFirst();
                try {
                    if (!foundGroup.isPresent()) {
                        // Add new group
                        final ThresholdGroup thresholdGroup = m_thresholdsDao.get(groupName);
                        if (thresholdGroup == null) {
                            LOG.error("{}: Could not get threshold group with name {}", logHeader, groupName);
                        } else {
                            newThresholdGroupList.add(thresholdGroup);
                            LOG.debug("{}: Adding threshold group: {}", logHeader, thresholdGroup);
                        }
                    } else {
                        // Merge existing data with current data
                        final ThresholdGroup thresholdGroup = m_thresholdsDao.merge(foundGroup.get());
                        newThresholdGroupList.add(thresholdGroup);
                        LOG.debug("{}: Merging threshold group: {}", logHeader, thresholdGroup);
                    }
                } catch (final IllegalStateException e) {
                    final ThresholdInitializationException tie = new ThresholdInitializationException("Unable to add or merge existing group " + foundGroup.orElse(null), e);
                    LOG.error(tie.getLocalizedMessage(), e);
                    throw tie;
                }
            }
            m_thresholdGroups.clear();
            m_thresholdGroups.addAll(newThresholdGroupList);
            m_hasThresholds = !m_thresholdGroups.isEmpty();
        }
    }

    /*
     * Returns true if there are defined thresholds for this node/address/service
     */
    /**
     * <p>hasThresholds</p>
     *
     * @return a boolean.
     */
    public boolean hasThresholds() {
        return m_hasThresholds;
    }

    /*
     * Returns true if the specified attribute is involved in any of defined thresholds for node/address/service
     * 
     * @param resourceTypeName a {@link java.lang.String} object.
     * @param attributeName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasThresholds(final String resourceTypeName, final String attributeName) {
        boolean ok = false;
        synchronized(m_thresholdGroups) {
            for (ThresholdGroup group : m_thresholdGroups) {
                Map<String,Set<ThresholdEntity>> entityMap = getEntityMap(group, resourceTypeName);
                if (entityMap != null) {
                    for (final Entry<String, Set<ThresholdEntity>> entry : entityMap.entrySet()) {
                        final Set<ThresholdEntity> value = entry.getValue();
                        for (final ThresholdEntity thresholdEntity : value) {
                            final Collection<String> requiredDatasources = thresholdEntity.getRequiredDatasources();
                            if (requiredDatasources.contains(attributeName)) {
                                ok = true;
                                LOG.debug("hasThresholds: {}@{}? {}", resourceTypeName, attributeName, ok);
                            } else {
                                LOG.trace("hasThresholds: {}@{}? {}", resourceTypeName, attributeName, ok);
                            }
                        }
                    }
                }
            }
        }
        return ok;
    }

    public final boolean isNodeInOutage() {
        PollOutagesConfigFactory outageFactory = PollOutagesConfigFactory.getInstance();
        boolean outageFound = false;
        synchronized(m_scheduledOutages) {
            for (String outageName : m_scheduledOutages) {
                if (outageFactory.isCurTimeInOutage(outageName)) {
                    LOG.debug("isNodeInOutage[node={}]: current time is on outage using '{}'; checking the node with IP {}", m_nodeId, outageName, m_hostAddress);
                    if (outageFactory.isNodeIdInOutage(m_nodeId, outageName) || outageFactory.isInterfaceInOutage(m_hostAddress, outageName)) {
                        LOG.debug("isNodeInOutage[node={}]: configured outage '{}' applies, interface {} will be ignored for threshold processing", m_nodeId, outageName, m_hostAddress);
                        outageFound = true;
                        break;
                    }
                }
            }
        }
        return outageFound;
    }

    /*
     * Apply thresholds definitions for specified resource using attribuesMap as current values.
     * Return a list of events to be send if some thresholds must be triggered or be rearmed.
     * 
     * @param resourceWrapper a {@link org.opennms.netmgt.threshd.CollectionResourceWrapper} object.
     * @param attributesMap a {@link java.util.Map} object.
     * @return a {@link java.util.List} object.
     */
    protected final List<Event> applyThresholds(CollectionResourceWrapper resourceWrapper, Map<String, CollectionAttribute> attributesMap) {
        List<Event> eventsList = new LinkedList<>();
        if (attributesMap == null || attributesMap.size() == 0) {
            LOG.debug("applyThresholds: Ignoring resource {} because required attributes map is empty.", resourceWrapper);
            return eventsList;
        }
        LOG.debug("applyThresholds: Applying thresholds on {} using {} attributes.", resourceWrapper, attributesMap.size());
        Date date = new Date();
        synchronized(m_thresholdGroups) {
            for (ThresholdGroup group : m_thresholdGroups) {
                Map<String,Set<ThresholdEntity>> entityMap = getEntityMap(group, resourceWrapper.getResourceTypeName());
                if (entityMap != null) {
                    for (final Entry<String, Set<ThresholdEntity>> entry : entityMap.entrySet()) {
                        final String key = entry.getKey();
                        final Set<ThresholdEntity> value = entry.getValue();
                        for (final ThresholdEntity thresholdEntity : value) {
                            if (passedThresholdFilters(resourceWrapper, thresholdEntity)) {
                                LOG.info("applyThresholds: Processing threshold {} : {} on resource {}", key, thresholdEntity, resourceWrapper);
                                Collection<String> requiredDatasources = thresholdEntity.getThresholdConfig().getRequiredDatasources();
                                final Map<String, Double> values = new HashMap<String,Double>();
                                boolean valueMissing = false;
                                boolean relaxed = thresholdEntity.getThresholdConfig().getBasethresholddef().getRelaxed();
                                for(final String ds : requiredDatasources) {
                                    final Double dsValue = resourceWrapper.getAttributeValue(ds);
                                    if(dsValue == null) {
                                        LOG.info("applyThresholds: Could not get data source value for '{}', {}", ds, (relaxed ? "but the expression will be evaluated (relaxed mode enabled)" : "not evaluating threshold"));
                                        valueMissing = true;
                                    }
                                    values.put(ds,dsValue);
                                }
                                if(!valueMissing || relaxed) {
                                    LOG.info("applyThresholds: All attributes found for {}, evaluating", resourceWrapper);
                                    resourceWrapper.setDsLabel(thresholdEntity.getDatasourceLabel());
                                    try {
                                        List<Event> thresholdEvents = thresholdEntity.evaluateAndCreateEvents(resourceWrapper, values, date);
                                        eventsList.addAll(thresholdEvents);
                                    } catch (Exception e) {
                                        LOG.warn("applyThresholds: Can't evaluate {} on {} because {}", key, resourceWrapper, e.getMessage());
                                    }
                                }
                            } else {
                                LOG.info("applyThresholds: Not processing threshold {} : {} because no filters matched", key, thresholdEntity);
                            }
                        }
                    }
                }
            }
        }
        return eventsList;
    }

    /**
     * <p>passedThresholdFilters</p>
     *
     * @param resource a {@link org.opennms.netmgt.threshd.CollectionResourceWrapper} object.
     * @param thresholdEntity a {@link org.opennms.netmgt.threshd.ThresholdEntity} object.
     * @return a boolean.
     */
    protected boolean passedThresholdFilters(CollectionResourceWrapper resource, ThresholdEntity thresholdEntity) {
        // Find the filters for threshold definition for selected group/dataSource
        final List<ResourceFilter> filters = thresholdEntity.getThresholdConfig().getBasethresholddef().getResourceFilters();
        if (filters.size() == 0) return true;
        // Threshold definition with filters must match ThresholdEntity (checking DataSource and ResourceType)
        LOG.debug("passedThresholdFilters: applying {} filters to resource {}", filters.size(), resource);
        int count = 1;
        final FilterOperator operator = thresholdEntity.getThresholdConfig().getBasethresholddef().getFilterOperator();
        boolean andResult = true;
        for (ResourceFilter f : filters) {
            LOG.debug("passedThresholdFilters: filter #{}: field={}, regex='{}'", count, f.getField(), f.getContent().orElse(null));
            count++;
            // Read Resource Attribute and apply filter rules if attribute is not null
            String attr = resource.getFieldValue(f.getField());
            if (attr != null) {
                try {
                    final Pattern p = Pattern.compile(f.getContent().orElse(""));
                    final Matcher m = p.matcher(attr);
                    boolean pass = m.matches();
                    LOG.debug("passedThresholdFilters: the value of {} is {}. Pass filter? {}", f.getField(), attr, pass);
                    if (operator.equals(FilterOperator.OR) && pass) {
                        return true;
                    }
                    if (operator.equals(FilterOperator.AND)) {
                        andResult = andResult && pass;
                        if (andResult == false)
                            return false;
                    }
                } catch (PatternSyntaxException e) {
                    LOG.warn("passedThresholdFilters: the regular expression {} is invalid: {}", f.getContent().orElse(null), e.getMessage(), e);
                    return false;
                }
            } else {
                LOG.warn("passedThresholdFilters: can't find value of {} for resource {}", f.getField(), resource);
                if (operator.equals(FilterOperator.AND)) {
                    return false;
                }
            }
        }
        if (operator.equals(FilterOperator.AND) && andResult) {
            return true;
        }
        return false;
    }

    /**
     * <p>initThresholdsDao</p>
     * @throws ThresholdInitializationException 
     */
    protected final void initThresholdsDao() throws ThresholdInitializationException {
        if (!m_initialized) {
            LOG.debug("initThresholdsDao: Initializing Factories and DAOs");
            final DefaultThresholdsDao defaultThresholdsDao = new DefaultThresholdsDao();
            try {
                ThresholdingConfigFactory.init();
                defaultThresholdsDao.setThresholdingConfigFactory(ThresholdingConfigFactory.getInstance());
                defaultThresholdsDao.afterPropertiesSet();
            } catch (final Throwable t) {
                final ThresholdInitializationException tie = new ThresholdInitializationException("Could not initialize DefaultThresholdsDao.", t);
                LOG.error("initThresholdsDao: " + tie.getLocalizedMessage(), t);
                throw tie;
            }
            try {
                ThreshdConfigFactory.init();
            } catch (final Throwable t) {
                final ThresholdInitializationException tie = new ThresholdInitializationException("Could not initialize ThreshdConfigFactory.", t);
                LOG.error("initThresholdsDao: " + tie.getLocalizedMessage(), t);
                throw tie;
            }
            m_thresholdsDao = defaultThresholdsDao;
            m_initialized = true;
        }
    }

    /*
     * The next code was extracted from Threshd.scheduleService.
     * 
     * - Search for packages defined on threshd-configuration.xml.
     * - Compare interface/service pair against each Threshd package.
     * - For each match, create new ThresholdableService object and schedule it for collection
     */
    private static final List<String> getThresholdGroupNames(int nodeId, String hostAddress, String serviceName) throws ThresholdInitializationException {
        List<String> groupNameList = new LinkedList<>();

        ThreshdConfigManager configManager = null;

        try { 
            configManager = ThreshdConfigFactory.getInstance();
        } catch (final IllegalStateException e) {
            throw new ThresholdInitializationException(e);
        }

        if (configManager != null) {
            for (org.opennms.netmgt.config.threshd.Package pkg : configManager.getConfiguration().getPackages()) {

                // Make certain the the current service is in the package and enabled!
                if (!configManager.serviceInPackageAndEnabled(serviceName, pkg)) {
                    LOG.debug("getThresholdGroupNames: address/service: {}/{} not scheduled, service is not enabled or does not exist in package: {}", hostAddress, serviceName, pkg.getName());
                    continue;
                }

                // Is the interface in the package?
                LOG.debug("getThresholdGroupNames: checking ipaddress {} for inclusion in pkg {}", hostAddress, pkg.getName());
                if (!configManager.interfaceInPackage(hostAddress, pkg)) {
                    LOG.debug("getThresholdGroupNames: address/service: {}/{} not scheduled, interface does not belong to package: {}", hostAddress, serviceName, pkg.getName());
                    continue;
                }

                // Getting thresholding-group for selected service and adding to groupNameList
                for (org.opennms.netmgt.config.threshd.Service svc : pkg.getServices()) {
                    if (svc.getName().equals(serviceName)) {
                        for (org.opennms.netmgt.config.threshd.Parameter parameter : svc.getParameters()) {
                            if (parameter.getKey().equals("thresholding-group")) {
                                String groupName = parameter.getValue();
                                groupNameList.add(groupName);
                                LOG.debug("getThresholdGroupNames:  address/service: {}/{}. Adding Group {}", hostAddress, serviceName, groupName);
                            }
                        }
                    }
                }
            }
        }
        return groupNameList;
    }

    protected void updateScheduledOutages() throws ThresholdInitializationException {
        synchronized (m_scheduledOutages) {
            m_scheduledOutages.clear();
            ThreshdConfigManager configManager = null;
            try {
                configManager = ThreshdConfigFactory.getInstance();
            } catch (final IllegalStateException e) {
                final ThresholdInitializationException tie = new ThresholdInitializationException("Failed to get threshd configuration factory while attempting to update scheduled outages.", e);
                LOG.error(tie.getLocalizedMessage(), e);
                throw tie;
            }
            for (org.opennms.netmgt.config.threshd.Package pkg : configManager.getConfiguration().getPackages()) {
                for (String outageCal : pkg.getOutageCalendars()) {
                    LOG.info("updateScheduledOutages[node={}]: checking scheduled outage '{}'", m_nodeId, outageCal);
                    try {
                        Outage outage = PollOutagesConfigFactory.getInstance().getOutage(outageCal);
                        if (outage == null) {
                            LOG.info("updateScheduledOutages[node={}]: scheduled outage '{}' is not defined.", m_nodeId, outageCal);
                        } else {
                            LOG.debug("updateScheduledOutages[node={}]: outage calendar '{}' found on package '{}'", m_nodeId, outage.getName(), pkg.getName());
                            m_scheduledOutages.add(outageCal);
                        }
                    } catch (final Exception e) {
                        LOG.info("updateScheduledOutages[node={}]: scheduled outage '{}' does not exist.", m_nodeId, outageCal);
                    }
                }
            }
        }
    }

    private static Map<String, Set<ThresholdEntity>> getEntityMap(ThresholdGroup thresholdGroup, String resourceType) {
        LOG.trace("getEntityMap: checking if the resourceType '{}' exists on threshold group {}", resourceType, thresholdGroup);
        Map<String, Set<ThresholdEntity>> entityMap = null;
        if (CollectionResource.RESOURCE_TYPE_NODE.equals(resourceType)) {
            entityMap = thresholdGroup.getNodeResourceType().getThresholdMap();
        } else if (CollectionResource.RESOURCE_TYPE_IF.equals(resourceType)) {
            entityMap = thresholdGroup.getIfResourceType().getThresholdMap();
        } else {
            Map<String, ThresholdResourceType> typeMap = thresholdGroup.getGenericResourceTypeMap();
            if (typeMap == null) {
                LOG.error("getEntityMap: Generic Resource Type map was null (this shouldn't happen) for threshold group {}", thresholdGroup.getName());
                return null;
            }
            ThresholdResourceType thisResourceType = typeMap.get(resourceType);
            if (thisResourceType == null) {
                LOG.debug("getEntityMap: No thresholds configured for resource type '{}' in threshold group {}. Skipping this group.", resourceType, thresholdGroup.getName());
                return null;
            }
            entityMap = thisResourceType.getThresholdMap();
        }
        return Collections.unmodifiableMap(entityMap);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        synchronized (m_thresholdGroups) {
            return m_thresholdGroups.toString();
        }
    }
}
