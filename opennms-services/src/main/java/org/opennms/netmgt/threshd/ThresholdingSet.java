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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigManager;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.ResourceFilter;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
public class ThresholdingSet {
    
    int m_nodeId;
    String m_hostAddress;
    String m_serviceName;
    RrdRepository m_repository;
    
    ThresholdsDao m_thresholdsDao;
    ThreshdConfigManager m_configManager;
    
    boolean m_initialized = false;    
    boolean m_hasThresholds = false;
    
    List<ThresholdGroup> m_thresholdGroups;

    private Map<String, Double> m_cache = new HashMap<String,Double>();

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
        m_thresholdGroups = new ArrayList<ThresholdGroup>();
        for (String groupName : groupNameList) {
            ThresholdGroup thresholdGroup = m_thresholdsDao.get(groupName);
            if (thresholdGroup == null) {
                log().error("Could not get threshold group with name " + groupName);
            }
            m_thresholdGroups.add(thresholdGroup);
            if (log().isDebugEnabled()) {
                log().debug("Adding threshold group: " + thresholdGroup);
            }
        }        
        m_hasThresholds = !m_thresholdGroups.isEmpty();
    }

    public void reinitialize() {
        m_initialized = false;
        initThresholdsDao();
        mergeThresholdGroups();
        m_hasThresholds = !m_thresholdGroups.isEmpty();
    }

    /*
     * Used to reload merge new thresholds configuration with current.
     * 
     * Extract ThresholdEntities for triggered thresholds and override new ThresholdGroup.
     */
    protected void mergeThresholdGroups() {
        initialize(); // FIXME Wrong. Only to recreate old implementation behavior
    }

    /*
     * Returns true if there are defined thresholds for this node/address/service
     */
    public boolean hasThresholds() {
        return m_hasThresholds;
    }

    /*
     * Returns true if the specified attribute is involved in any of defined threshols for node/address/service
     */
    public boolean hasThresholds(CollectionAttribute attribute) {
        CollectionResource resource = attribute.getResource();
        for (ThresholdGroup group : m_thresholdGroups) {
            Map<String,Set<ThresholdEntity>> entityMap = getEntityMap(group, resource.getResourceTypeName());
            for(String key : entityMap.keySet()) {
                for (ThresholdEntity thresholdEntity : entityMap.get(key)) {
                    Collection<String> requiredDatasources = thresholdEntity.getRequiredDatasources();
                    if (requiredDatasources.contains(attribute.getName()))
                        return true;
                }
            }
        }
        return false;
    }

    /*
     * Apply thresholds definitions for specified resource using attribuesMap as current values.
     * Return a list of events to be send if some thresholds must be triggered or be rearmed.
     */
    public List<Event> applyThresholds(CollectionResource resource, Map<String, CollectionAttribute> attributesMap) {
        Date date = new Date();
        List<Event> eventsList = new ArrayList<Event>();
        for (ThresholdGroup group : m_thresholdGroups) {
            Map<String,Set<ThresholdEntity>> entityMap = getEntityMap(group, resource.getResourceTypeName());
            for(String key : entityMap.keySet()) {
                for (ThresholdEntity thresholdEntity : entityMap.get(key)) {
                    if (passedThresholdFilters(resource, thresholdEntity, attributesMap)) {
                        log().info("applyThresholds: Processing threshold " + key + " : " + thresholdEntity);
                        Collection<String> requiredDatasources = thresholdEntity.getRequiredDatasources();
                        Map<String, Double> values = new HashMap<String,Double>();
                        boolean valueMissing = false;
                        for(String ds: requiredDatasources) {
                            Double dsValue = getCollectionAttributeValue(resource, attributesMap, ds);
                            if(dsValue == null) {
                                log().info("applyThresholds: Could not get data source value for '" + ds + "'.  Not evaluating threshold.");
                                valueMissing = true;
                            }
                            values.put(ds,dsValue);
                        }
                        if(!valueMissing) {
                            log().info("applyThresholds: All values found, evaluating");
                            List<Event> thresholdEvents = thresholdEntity.evaluateAndCreateEvents(resource.getInstance(), values, date);
                            String dsLabelValue = getDataSourceLabelValue(resource, attributesMap, thresholdEntity.getDatasourceLabel());
                            if (dsLabelValue == null) dsLabelValue = "Unknown";
                            completeEventList(thresholdEvents, resource, dsLabelValue);
                            eventsList.addAll(thresholdEvents);
                        }
                    } else {
                        log().info("applyThresholds: Not processing threshold " + key + " : " + thresholdEntity + " because no filters matched");
                    }
                }
            }
        }
        return eventsList;
    }
    
    /*
     * FIXME Why ?
     * I think that this should be part of ThresholdEntity implementation
     */
    private void completeEventList(List<Event> eventList, CollectionResource resource, String dsLabelValue) {
        for (Event event : eventList) {
            event.setNodeid(m_nodeId);
            event.setService(m_serviceName);
            event.setInterface(m_hostAddress);
            Parms eventParms = event.getParms();
            Parm eventParm;
            Value parmValue;
            if (dsLabelValue != null) {
                eventParm = new Parm();
                eventParm.setParmName("label");
                parmValue = new Value();
                parmValue.setContent(dsLabelValue);
                eventParm.setValue(parmValue);
                eventParms.addParm(eventParm);
            }
            if (resource.getResourceTypeName().equals("if")) {
                File resourceDir = resource.getResourceDir(m_repository);
                String ifLabel = resourceDir.getName();
                String snmpIfIndex = getIfInfo(m_nodeId, ifLabel, "snmpifindex");
                if (ifLabel != null) {
                    eventParm = new Parm();
                    eventParm.setParmName("ifLabel");
                    parmValue = new Value();
                    parmValue.setContent(ifLabel);
                    eventParm.setValue(parmValue);
                    eventParms.addParm(eventParm);
                }
                if (snmpIfIndex != null) {
                    eventParm = new Parm();
                    eventParm.setParmName("ifIndex");
                    parmValue = new Value();
                    parmValue.setContent(snmpIfIndex);
                    eventParm.setValue(parmValue);
                    eventParms.addParm(eventParm);
                }                
            }
        }
    }

    private boolean passedThresholdFilters(CollectionResource resource, ThresholdEntity thresholdEntity, Map<String, CollectionAttribute> attributesMap) {
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
            String attr = getDataSourceLabelValue(resource, attributesMap, f.getField());
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
                log().warn("passedThresholdFilters: can't find value of " + attr + " for resource " + resource);
            }
        }
        return false;
    }
    
    private Double getCollectionAttributeValue(CollectionResource resource, Map<String, CollectionAttribute> attributes, String ds) {
        if (attributes.get(ds) == null) {
            log().warn("getCollectionAttributeValue: can't find attribute called " + ds + " on " + resource);
            return null;
        }
        String numValue = attributes.get(ds).getNumericValue();
        if (numValue == null) {
            log().warn("getCollectionAttributeValue: can't find numeric value for " + ds + " on " + resource);
            return null;
        }
        String id = resource.toString() + "." + ds;
        Double current = Double.parseDouble(numValue);
        if (attributes.get(ds).getType().toLowerCase().startsWith("counter") == false) {
            if (log().isDebugEnabled()) {
                log().debug("getCollectionAttributeValue: " + id + "(gauge) value= " + current);
            }
            return current;
        }
        Double last = m_cache.get(id);
        if (log().isDebugEnabled()) {
            log().debug("getCollectionAttributeValue: " + id + "(counter) last=" + last + ", current=" + current);
        }
        m_cache.put(id, current);
        if (last == null) {
            return Double.NaN;
        }
        if (current < last) {
            log().info("getCollectionAttributeValue: counter reset detected, ignoring value");
            return Double.NaN;
        }
        return current - last;
    }
    
    private String getDataSourceLabelValue(CollectionResource resource, Map<String, CollectionAttribute> attributes, String ds) {
        if (ds == null)
            return null;
        if (log().isDebugEnabled()) {
            log().debug("getDataSourceLabelValue: Getting Value for " + resource.getResourceTypeName() + "::" + ds);
        }
        String value = null;
        File resourceDirectory = resource.getResourceDir(m_repository);
        if (ds.equals("ID")) {
            return resourceDirectory.getName();
        }
        try {
            if (resource.getResourceTypeName().equals("if")) {
                String ifLabel = resourceDirectory.getName();
                value = getIfInfo(m_nodeId, ifLabel, ds);
            }
            if (value == null) { // Find value on collected string attributes
                value = attributes.containsKey(ds) ? attributes.get(ds).getStringValue() : null;
            }
            if (value == null) { // Find value on saved string attributes                
                value = ResourceTypeUtils.getStringProperty(resourceDirectory, ds);
            }
        } catch (Exception e) {
            log().info("getDataSourceLabelValue: Can't get value for attribute " + ds + " for resource " + resource + ". " + e, e);
        }
        return value;
    }

    private String getIfInfo(final int nodeid, final String ifLabel, final String attributeName) {
        return new JdbcIfInfoGetter().getIfInfoForNodeAndLabel(m_nodeId, ifLabel).get(attributeName);
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

        List<String> groupNameList = new ArrayList<String>();
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
                log().error("getEntityMap: Generic Resource Type map was null (this shouldn't happen)");
                return null;
            }
            ThresholdResourceType thisResourceType = typeMap.get(resourceType);
            if (thisResourceType == null) {
                log().warn("getEntityMap: No thresholds configured for resource type " + resourceType + ".  Not processing this collection ");
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

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
