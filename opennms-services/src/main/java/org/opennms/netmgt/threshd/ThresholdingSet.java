//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

import java.util.Collection;
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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigManager;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.ResourceFilter;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.xml.event.Event;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
public abstract class ThresholdingSet {
    
    int m_nodeId;
    String m_hostAddress;
    String m_serviceName;
    RrdRepository m_repository;
    
    ThresholdsDao m_thresholdsDao;
    ThreshdConfigManager m_configManager;
    
    boolean m_initialized = false;    
    boolean m_hasThresholds = false;
    
    List<ThresholdGroup> m_thresholdGroups;

    public ThresholdingSet(int nodeId, String hostAddress, String serviceName, RrdRepository repository) {
        m_nodeId = nodeId;
        m_hostAddress = hostAddress;
        m_serviceName = serviceName;
        m_repository = repository;        
        initThresholdsDao();
        initialize();
    }
    
    protected void initialize() {
        List<String> groupNameList = getThresholdGroupNames(m_nodeId, m_hostAddress, m_serviceName);
        m_thresholdGroups = new LinkedList<ThresholdGroup>();
        for (String groupName : groupNameList) {
            ThresholdGroup thresholdGroup = m_thresholdsDao.get(groupName);
            if (thresholdGroup == null) {
                log().error("initialize: Could not get threshold group with name " + groupName);
            }
            m_thresholdGroups.add(thresholdGroup);
            if (log().isDebugEnabled()) {
                log().debug("initialize: Adding threshold group: " + thresholdGroup);
            }
        }
        m_hasThresholds = !m_thresholdGroups.isEmpty();
    }

    public void reinitialize() {
        m_initialized = false;
        ThresholdingEventProxyFactory.getFactory().getProxy().removeAllEvents();
        initThresholdsDao();
        mergeThresholdGroups();
        m_hasThresholds = !m_thresholdGroups.isEmpty();
        ThresholdingEventProxyFactory.getFactory().getProxy().sendAllEvents();
    }

    /*
     * Used to reload merge new thresholds configuration with current.
     * 
     * Extract thresholdEvaluatorStates Map from each ThresholdEntity, then copy this to new thresholdEntity.
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
    public boolean hasThresholds() {
        return m_hasThresholds;
    }

    /*
     * Returns true if the specified attribute is involved in any of defined thresholds for node/address/service
     */
    public boolean hasThresholds(String resourceTypeName, String attributeName) {
        boolean ok = false;
        for (ThresholdGroup group : m_thresholdGroups) {
            Map<String,Set<ThresholdEntity>> entityMap = getEntityMap(group, resourceTypeName);
            if (entityMap != null) {
                for(String key : entityMap.keySet()) {
                    for (ThresholdEntity thresholdEntity : entityMap.get(key)) {
                        Collection<String> requiredDatasources = thresholdEntity.getRequiredDatasources();
                        if (requiredDatasources.contains(attributeName))
                            ok = true;
                    }
                }
            }
        }
        log().debug("hasThresholds: " + resourceTypeName + "@" + attributeName + "? " + ok);
        return ok;
    }

    /*
     * Apply thresholds definitions for specified resource using attribuesMap as current values.
     * Return a list of events to be send if some thresholds must be triggered or be rearmed.
     */
    protected List<Event> applyThresholds(CollectionResourceWrapper resourceWrapper, Map<String, CollectionAttribute> attributesMap) {
        log().debug("applyThresholds: Applying thresholds on " + resourceWrapper + " using " + attributesMap.size() + " attributes.");
        Date date = new Date();
        List<Event> eventsList = new LinkedList<Event>();
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
    
    protected boolean passedThresholdFilters(CollectionResourceWrapper resource, ThresholdEntity thresholdEntity) {
        // Find the filters for threshold definition for selected group/dataSource
        ResourceFilter[] filters = thresholdEntity.getThresholdConfig().getBasethresholddef().getResourceFilter();
        if (filters.length == 0) return true;
        // Threshold definition with filters must match ThresholdEntity (checking DataSource and ResourceType)
        if (log().isDebugEnabled()) {
            log().debug("passedThresholdFilters: applying " + filters.length + " filters to resource " + resource);
        }
        int count = 1;
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
                    if (pass) {
                        return true;
                    }
                } catch (PatternSyntaxException e) {
                    log().warn("passedThresholdFilters: the regular expression " + f.getContent() + " is invalid: " + e.getMessage(), e);
                    return false;
                }
            } else {
                log().warn("passedThresholdFilters: can't find value of " + f.getField() + " for resource " + resource);
            }
        }
        return false;
    }
    
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
            m_configManager = ThreshdConfigFactory.getInstance();            
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

        List<String> groupNameList = new LinkedList<String>();
        for (org.opennms.netmgt.config.threshd.Package pkg : m_configManager.getConfiguration().getPackage()) {

            // Make certain the the current service is in the package and enabled!
            if (!m_configManager.serviceInPackageAndEnabled(serviceName, pkg)) {
                if (log().isDebugEnabled())
                    log().debug("getThresholdGroupNames: address/service: " + hostAddress + "/" + serviceName + " not scheduled, service is not enabled or does not exist in package: " + pkg.getName());
                continue;
            }

            // Is the interface in the package?
            if (log().isDebugEnabled()) {
                log().debug("getThresholdGroupNames: checking ipaddress " + hostAddress + " for inclusion in pkg " + pkg.getName());
            }
            boolean foundInPkg = m_configManager.interfaceInPackage(hostAddress, pkg);
            if (!foundInPkg) {
                // The interface might be a newly added one, rebuild the package
                // to ipList mapping and again to verify if the interface is in
                // the package.
                m_configManager.rebuildPackageIpListMap();
                foundInPkg = m_configManager.interfaceInPackage(hostAddress, pkg);
            }
            if (!foundInPkg) {
                if (log().isDebugEnabled())
                    log().debug("getThresholdGroupNames: address/service: " + hostAddress + "/" + serviceName + " not scheduled, interface does not belong to package: " + pkg.getName());
                continue;
            }

            // Getting thresholding-group for selected service and adding to groupNameList
            for (org.opennms.netmgt.config.threshd.Service svc : pkg.getService()) {
                if (svc.getName().equals(serviceName)) {
                    String groupName = null;
                    for (org.opennms.netmgt.config.threshd.Parameter parameter : svc.getParameter()) {
                        if (parameter.getKey().equals("thresholding-group")) {
                            groupName = parameter.getValue();
                        }
                    }
                    if (groupName != null) {
                        groupNameList.add(groupName);
                        if (log().isDebugEnabled()) {
                            log().debug("getThresholdGroupNames:  address/service: " + hostAddress + "/" + serviceName + ". Adding Group " + groupName);
                        }
                    }
                }
            }
        }
        
        return groupNameList;
    }

    private Map<String, Set<ThresholdEntity>> getEntityMap(ThresholdGroup thresholdGroup, String resourceType) {
        Map<String, Set<ThresholdEntity>> entityMap = null;
        if ("node".equals(resourceType)) {
            entityMap = thresholdGroup.getNodeResourceType().getThresholdMap();
        } else if ("if".equals(resourceType)) {
            entityMap = thresholdGroup.getIfResourceType().getThresholdMap();
        } else {
            Map<String, ThresholdResourceType> typeMap = thresholdGroup.getGenericResourceTypeMap();
            if (typeMap == null) {
                log().error("getEntityMap: Generic Resource Type map was null (this shouldn't happen).");
                return null;
            }
            ThresholdResourceType thisResourceType = typeMap.get(resourceType);
            if (thisResourceType == null) {
                log().warn("getEntityMap: No thresholds configured for resource type " + resourceType + ". Not processing this collection.");
                return null;
            }
            entityMap = thisResourceType.getThresholdMap();
        }
        return entityMap;
    }

    @Override
    public String toString() {
        return m_thresholdGroups.toString();
    }

    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
