/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigManager;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.threshd.ResourceFilter;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>Abstract ThresholdingSet class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class ThresholdingSet {
    
    protected final int m_nodeId;
    protected final String m_hostAddress;
    protected final String m_serviceName;
    protected final RrdRepository m_repository;
    
    protected ThresholdsDao m_thresholdsDao;
    
    protected boolean m_initialized = false;
    protected boolean m_hasThresholds = false;
    
    protected List<ThresholdGroup> m_thresholdGroups = new LinkedList<ThresholdGroup>();
    protected final List<String> m_scheduledOutages = new ArrayList<String>();

    /**
     * <p>Constructor for ThresholdingSet.</p>
     *
     * @param nodeId a int.
     * @param hostAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param repository a {@link org.opennms.netmgt.model.RrdRepository} object.
     * @param interval a long.
     */
    public ThresholdingSet(int nodeId, String hostAddress, String serviceName, RrdRepository repository) {
        m_nodeId = nodeId;
        m_hostAddress = (hostAddress == null ? null : hostAddress.intern());
        m_serviceName = (serviceName == null ? null : serviceName.intern());
        m_repository = repository;
        initThresholdsDao();
        initialize();
    }
    
    /**
     * <p>initialize</p>
     */
    protected void initialize() {
        List<String> groupNameList = getThresholdGroupNames(m_nodeId, m_hostAddress, m_serviceName);
        m_thresholdGroups.clear();
        for (String groupName : groupNameList) {
            try {
                ThresholdGroup thresholdGroup = m_thresholdsDao.get(groupName);
                if (thresholdGroup == null) {
                    log().error("initialize: Could not get threshold group with name " + groupName);
                }
                m_thresholdGroups.add(thresholdGroup);
                if (log().isDebugEnabled()) {
                    log().debug("initialize: Adding threshold group: " + thresholdGroup);
                }
            } catch (Throwable e) {
                log().error("initialize: Can't process threshold group " + groupName, e);
            }
        }
        m_hasThresholds = !m_thresholdGroups.isEmpty();
        updateScheduledOutages();
    }

    /**
     * <p>reinitialize</p>
     */
    public void reinitialize() {
        m_initialized = false;
        ThresholdingEventProxyFactory.getFactory().getProxy().removeAllEvents();
        initThresholdsDao();
        mergeThresholdGroups();
        m_hasThresholds = !m_thresholdGroups.isEmpty();
        updateScheduledOutages();
        ThresholdingEventProxyFactory.getFactory().getProxy().sendAllEvents();
    }

    /*
     * Used to reload merge new thresholds configuration with current.
     * 
     * Extract thresholdEvaluatorStates Map from each ThresholdEntity, then copy this to new thresholdEntity.
     */
    /**
     * <p>mergeThresholdGroups</p>
     */
    protected void mergeThresholdGroups() {
        log().debug("mergeThresholdGroups: begin merging operation");
        List<String> groupNameList = getThresholdGroupNames(m_nodeId, m_hostAddress, m_serviceName);
        // If size differs its because some groups where deleted.
        if (groupNameList.size() != m_thresholdGroups.size()) {
            // Deleting Groups
            log().debug("mergeThresholdGroups: new group name list differs from current threshold group list");
            for (Iterator<ThresholdGroup> i = m_thresholdGroups.iterator(); i.hasNext();) {
                ThresholdGroup group = i.next();
                if (!groupNameList.contains(group.getName())) {
                    log().info("mergeThresholdGroups: deleting group " + group);
                    group.delete();
                    i.remove();
                }
            }
        }
        List<ThresholdGroup> newThresholdGroupList = new LinkedList<ThresholdGroup>();
        for (String groupName : groupNameList) {
            // Check if group exist on current configured list
            ThresholdGroup foundGroup = null;
            for (ThresholdGroup group : m_thresholdGroups) {
                if (group.getName().equals(groupName))
                    foundGroup = group;
            }
            if (foundGroup == null) {
                // Add new group
                ThresholdGroup thresholdGroup = m_thresholdsDao.get(groupName);
                if (thresholdGroup == null) {
                    log().error("mergeThresholdGroups: Could not get threshold group with name " + groupName);
                } else {
                    newThresholdGroupList.add(thresholdGroup);
                    if (log().isDebugEnabled()) {
                        log().debug("mergeThresholdGroups: Adding threshold group: " + thresholdGroup);
                    }
                }
            } else {
                // Merge existing data with current data
                ThresholdGroup thresholdGroup = m_thresholdsDao.merge(foundGroup);
                newThresholdGroupList.add(thresholdGroup);
                if (log().isDebugEnabled()) {
                    log().debug("mergeThresholdGroups: Merging threshold group: " + thresholdGroup);
                }
            }
        }
        m_thresholdGroups = newThresholdGroupList;
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
     */
    /**
     * <p>hasThresholds</p>
     *
     * @param resourceTypeName a {@link java.lang.String} object.
     * @param attributeName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasThresholds(String resourceTypeName, String attributeName) {
        boolean ok = false;
        for (ThresholdGroup group : m_thresholdGroups) {
            Map<String,Set<ThresholdEntity>> entityMap = getEntityMap(group, resourceTypeName);
            if (entityMap != null) {
                for(String key : entityMap.keySet()) {
                    for (ThresholdEntity thresholdEntity : entityMap.get(key)) {
                        Collection<String> requiredDatasources = thresholdEntity.getRequiredDatasources();
                        if (requiredDatasources.contains(attributeName)) {
                            ok = true;
                            LogUtils.debugf(ThresholdingSet.class, "hasThresholds: %s@%s? %s", resourceTypeName, attributeName, ok);
                        } else {
                            LogUtils.tracef(ThresholdingSet.class, "hasThresholds: %s@%s? %s", resourceTypeName, attributeName, ok);
                        }
                    }
                }
            }
        }
        return ok;
    }

    public boolean isNodeInOutage() {
        PollOutagesConfigFactory outageFactory = PollOutagesConfigFactory.getInstance();
        boolean outageFound = false;
        for (String outageName : m_scheduledOutages) {
            if (outageFactory.isCurTimeInOutage(outageName)) {
                log().debug("isNodeInOutage[node=" + m_nodeId + "]: current time is on outage using '" + outageName + "'; checking the node with IP " + m_hostAddress);
                if (outageFactory.isNodeIdInOutage(m_nodeId, outageName) || outageFactory.isInterfaceInOutage(m_hostAddress, outageName)) {
                    log().debug("isNodeInOutage[node=" + m_nodeId + "]: configured outage '" + outageName + "' applies, interface " + m_hostAddress + " will be ignored for threshold processing");
                    outageFound = true;
                    break;
                }
            }
        }
        return outageFound;
    }

    /*
     * Apply thresholds definitions for specified resource using attribuesMap as current values.
     * Return a list of events to be send if some thresholds must be triggered or be rearmed.
     */
    /**
     * <p>applyThresholds</p>
     *
     * @param resourceWrapper a {@link org.opennms.netmgt.threshd.CollectionResourceWrapper} object.
     * @param attributesMap a {@link java.util.Map} object.
     * @return a {@link java.util.List} object.
     */
    protected List<Event> applyThresholds(CollectionResourceWrapper resourceWrapper, Map<String, CollectionAttribute> attributesMap) {
        List<Event> eventsList = new LinkedList<Event>();
        if (attributesMap == null || attributesMap.size() == 0) {
            log().debug("applyThresholds: Ignoring resource " + resourceWrapper + " because required attributes map is empty.");
            return eventsList;
        }
        log().debug("applyThresholds: Applying thresholds on " + resourceWrapper + " using " + attributesMap.size() + " attributes.");
        Date date = new Date();
        for (ThresholdGroup group : m_thresholdGroups) {
            Map<String,Set<ThresholdEntity>> entityMap = getEntityMap(group, resourceWrapper.getResourceTypeName());
            if (entityMap != null) {
                for(String key : entityMap.keySet()) {
                    for (ThresholdEntity thresholdEntity : entityMap.get(key)) {
                        if (passedThresholdFilters(resourceWrapper, thresholdEntity)) {
                            log().info("applyThresholds: Processing threshold " + key + " : " + thresholdEntity);
                            Collection<String> requiredDatasources = thresholdEntity.getRequiredDatasources();
                            Map<String, Double> values = new HashMap<String,Double>();
                            boolean valueMissing = false;
                            for(String ds: requiredDatasources) {
                                Double dsValue = resourceWrapper.getAttributeValue(ds);
                                if(dsValue == null) {
                                    log().info("applyThresholds: Could not get data source value for '" + ds + "'.  Not evaluating threshold.");
                                    valueMissing = true;
                                }
                                values.put(ds,dsValue);
                            }
                            if(!valueMissing) {
                                log().info("applyThresholds: All values found, evaluating");
                                resourceWrapper.setLabel(thresholdEntity.getDatasourceLabel());
                                List<Event> thresholdEvents = thresholdEntity.evaluateAndCreateEvents(resourceWrapper, values, date);
                                eventsList.addAll(thresholdEvents);
                            }
                        } else {
                            log().info("applyThresholds: Not processing threshold " + key + " : " + thresholdEntity + " because no filters matched");
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
        ResourceFilter[] filters = thresholdEntity.getThresholdConfig().getBasethresholddef().getResourceFilter();
        if (filters.length == 0) return true;
        // Threshold definition with filters must match ThresholdEntity (checking DataSource and ResourceType)
        if (log().isDebugEnabled()) {
            log().debug("passedThresholdFilters: applying " + filters.length + " filters to resource " + resource);
        }
        int count = 1;
        String operator = thresholdEntity.getThresholdConfig().getBasethresholddef().getFilterOperator().toLowerCase();
        boolean andResult = true;
        for (ResourceFilter f : filters) {
            if (log().isDebugEnabled()) {
                log().debug("passedThresholdFilters: filter #" + count + ": field=" + f.getField() + ", regex='" + f.getContent() + "'");
            }
            count++;
            // Read Resource Attribute and apply filter rules if attribute is not null
            String attr = resource.getLabelValue(f.getField());
            if (attr != null) {
                try {
                    final Pattern p = Pattern.compile(f.getContent());
                    final Matcher m = p.matcher(attr);
                    boolean pass = m.matches();
                    if (log().isDebugEnabled()) {
                        log().debug("passedThresholdFilters: the value of " + f.getField() + " is " + attr + ". Pass filter? " + pass);
                    }
                    if (operator.equals("or") && pass) {
                        return true;
                    }
                    if (operator.equals("and")) {
                        andResult = andResult && pass;
                        if (andResult == false)
                            return false;
                    }
                } catch (PatternSyntaxException e) {
                    log().warn("passedThresholdFilters: the regular expression " + f.getContent() + " is invalid: " + e.getMessage(), e);
                    return false;
                }
            } else {
                log().warn("passedThresholdFilters: can't find value of " + f.getField() + " for resource " + resource);
            }
        }
        if (operator.equals("and") && andResult)
            return true;
        return false;
    }
    
    /**
     * <p>initThresholdsDao</p>
     */
    protected void initThresholdsDao() {
        if (!m_initialized) {
            log().debug("initThresholdsDao: Initializing Factories and DAOs");
            m_initialized = true;
            DefaultThresholdsDao defaultThresholdsDao = new DefaultThresholdsDao();
            try {
                ThresholdingConfigFactory.init();
                defaultThresholdsDao.setThresholdingConfigFactory(ThresholdingConfigFactory.getInstance());
                defaultThresholdsDao.afterPropertiesSet();
            } catch (Throwable t) {
                log().error("initThresholdsDao: Could not initialize DefaultThresholdsDao: " + t, t);
                throw new RuntimeException("Could not initialize DefaultThresholdsDao: " + t, t);
            }
            try {
                ThreshdConfigFactory.init();
            } catch (Throwable t) {
                log().error("initThresholdsDao: Could not initialize ThreshdConfigFactory: " + t, t);
                throw new RuntimeException("Could not initialize ThreshdConfigFactory: " + t, t);
            }
            m_thresholdsDao = defaultThresholdsDao;
        }
    }
    
    /*
     * The next code was extracted from Threshd.scheduleService.
     * 
     * - Search for packages defined on threshd-configuration.xml.
     * - Compare interface/service pair against each Threshd package.
     * - For each match, create new ThresholdableService object and schedule it for collection
     */
    private List<String> getThresholdGroupNames(int nodeId, String hostAddress, String serviceName) {
        ThreshdConfigManager configManager = ThreshdConfigFactory.getInstance();

        List<String> groupNameList = new LinkedList<String>();
        for (org.opennms.netmgt.config.threshd.Package pkg : configManager.getConfiguration().getPackage()) {

            // Make certain the the current service is in the package and enabled!
            if (!configManager.serviceInPackageAndEnabled(serviceName, pkg)) {
                if (log().isDebugEnabled())
                    log().debug("getThresholdGroupNames: address/service: " + hostAddress + "/" + serviceName + " not scheduled, service is not enabled or does not exist in package: " + pkg.getName());
                continue;
            }

            // Is the interface in the package?
            if (log().isDebugEnabled())
                log().debug("getThresholdGroupNames: checking ipaddress " + hostAddress + " for inclusion in pkg " + pkg.getName());
            if (!configManager.interfaceInPackage(hostAddress, pkg)) {
                if (log().isDebugEnabled())
                    log().debug("getThresholdGroupNames: address/service: " + hostAddress + "/" + serviceName + " not scheduled, interface does not belong to package: " + pkg.getName());
                continue;
            }

            // Getting thresholding-group for selected service and adding to groupNameList
            for (org.opennms.netmgt.config.threshd.Service svc : pkg.getService()) {
                if (svc.getName().equals(serviceName)) {
                    for (org.opennms.netmgt.config.threshd.Parameter parameter : svc.getParameter()) {
                        if (parameter.getKey().equals("thresholding-group")) {
                            String groupName = parameter.getValue();
                            groupNameList.add(groupName);
                            if (log().isDebugEnabled()) {
                                log().debug("getThresholdGroupNames:  address/service: " + hostAddress + "/" + serviceName + ". Adding Group " + groupName);
                            }
                        }
                    }
                }
            }
        }
        
        return groupNameList;
    }

    protected void updateScheduledOutages() {
        m_scheduledOutages.clear();
        ThreshdConfigManager configManager = ThreshdConfigFactory.getInstance();
        for (org.opennms.netmgt.config.threshd.Package pkg : configManager.getConfiguration().getPackage()) {
            for (String outageCal : pkg.getOutageCalendarCollection()) {
                log().info("updateScheduledOutages[node=" + m_nodeId + "]: checking scheduled outage '" + outageCal + "'");
                try {
                    Outage outage = PollOutagesConfigFactory.getInstance().getOutage(outageCal);
                    if (outage == null) {
                        log().info("updateScheduledOutages[node=" + m_nodeId + "]: scheduled outage '" + outageCal + "' is not defined.");
                    } else {
                        log().debug("updateScheduledOutages[node=" + m_nodeId + "]: outage calendar '" + outage.getName() + "' found on package '" + pkg.getName() + "'");
                        m_scheduledOutages.add(outageCal);
                    }
                } catch (Exception e) {
                    log().info("updateScheduledOutages[node=" + m_nodeId + "]: scheduled outage '" + outageCal + "' does not exist.");                    
                }
            }
        }
    }

    private static Map<String, Set<ThresholdEntity>> getEntityMap(ThresholdGroup thresholdGroup, String resourceType) {
        LogUtils.tracef(ThresholdingSet.class, "getEntityMap: checking if the resourceType '%s' exists on threshold group %s", resourceType, thresholdGroup);
        Map<String, Set<ThresholdEntity>> entityMap = null;
        if ("node".equals(resourceType)) {
            entityMap = thresholdGroup.getNodeResourceType().getThresholdMap();
        } else if ("if".equals(resourceType)) {
            entityMap = thresholdGroup.getIfResourceType().getThresholdMap();
        } else {
            Map<String, ThresholdResourceType> typeMap = thresholdGroup.getGenericResourceTypeMap();
            if (typeMap == null) {
                LogUtils.errorf(ThresholdingSet.class, "getEntityMap: Generic Resource Type map was null (this shouldn't happen) for threshold group %s", thresholdGroup.getName());
                return null;
            }
            ThresholdResourceType thisResourceType = typeMap.get(resourceType);
            if (thisResourceType == null) {
                LogUtils.infof(ThresholdingSet.class, "getEntityMap: No thresholds configured for resource type '%s' in threshold group %s. Skipping this group.", resourceType, thresholdGroup.getName());
                return null;
            }
            entityMap = thisResourceType.getThresholdMap();
        }
        return Collections.unmodifiableMap(entityMap);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return m_thresholdGroups.toString();
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected static ThreadCategory log() {
        return ThreadCategory.getInstance(ThresholdingSet.class);
    }

}
