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
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.collectd.AbstractCollectionSetVisitor;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigManager;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.ResourceFilter;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.springframework.dao.DataAccessException;

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
 * @author opennms
 *
 */
public class ThresholdingVisitor extends AbstractCollectionSetVisitor {
    private static ThresholdsDao s_thresholdsDao;
    private static ThreshdConfigManager s_threshdConfig;
    static {
        initThresholdsDao();
    }
    
    protected static void initThresholdsDao() {
        DefaultThresholdsDao defaultThresholdsDao = new DefaultThresholdsDao();
        
        try {
            ThresholdingConfigFactory.init();
            defaultThresholdsDao.setThresholdingConfigFactory(ThresholdingConfigFactory.getInstance());
            defaultThresholdsDao.afterPropertiesSet();
        } catch (Throwable t) {
            ThreadCategory.getInstance(ThresholdingVisitor.class).error("initialize: Could not initialize DefaultThresholdsDao: " + t, t);
            throw new RuntimeException("Could not initialize DefaultThresholdsDao: " + t, t);
        }
        try {
            ThreshdConfigFactory.init();
        } catch (Throwable t) {
            ThreadCategory.getInstance(ThresholdingVisitor.class).error("initialize: Could not initialize ThreshdConfigFactory: " + t, t);
            throw new RuntimeException("Could not initialize ThreshdConfigFactory: " + t, t);
        }
        s_thresholdsDao = defaultThresholdsDao;
        s_threshdConfig = ThreshdConfigFactory.getInstance();            
    }
    
    public static void handleThresholdConfigChanged() {
    	initThresholdsDao();
    }

    //DB node id of the node where thresholding
    private int m_nodeId;
    
    private List<String> m_groupNameList;
    
    //The address of the interface being thresholded; only used for display/events, not as an actual IP address
    private String m_hostAddress;
    
    //The name of the service being thresholded (SNMP, NSClient etc)
    private String m_serviceName;
    
    //Fetched from the db and stored here, if this Visitor is in use on an interface
    private String m_snmpIfIndex;
    
    //The last success status of thresholding; used to know if status has changed and events should be generated
    private boolean m_success = true; //Default to true at the start (we assume thresholding was working when OpenNMS starts up) 
    
    //The primary store of numeric values pulled out of the collection Set; flushed every run
    private Map<String, Double> m_numericAttributeValues;
    
    //The primary store of string values pulled out of the collection Set; flushed every run
    private Map<String, String> m_stringAttributeValues;

    //The set of ThresholdEntity instances
    private List<ThresholdGroup> m_thresholdGroupList;
    
    //Handy local reference to the rrd repository in use.  
    private RrdRepository m_repository;
    
    //Set to true by the reinit code when a refresh is required (at init time, or when thresholds change)
    private boolean m_needsRefresh=false;
    
    
    public static ThresholdingVisitor createThresholdingVisitor(int nodeId, String hostAddress, String serviceName, RrdRepository repo, Map params) {
        Category log = ThreadCategory.getInstance(ThresholdingVisitor.class);
        
        // Use the "thresholding-enable" to use Thresholds processing on Collectd
        String enabled = (String)params.get("thresholding-enabled");
        if (enabled == null || !enabled.equals("true")) {
        	log.warn("createThresholdingVisitor: Thresholds processing is not enabled. Check thresholding-enabled param on collectd package");
        	return null;
        }

        // The next code was extracted from Threshd.scheduleService
        //
        //
        // Searching for packages defined on threshd-configuration.xml
        // Compare interface/service pair against each threshd package
        // For each match, create new ThresholdableService object and
        // schedule it for collection
        //
        List<String> groupNameList = new ArrayList<String>();
        for (org.opennms.netmgt.config.threshd.Package pkg : s_threshdConfig.getConfiguration().getPackage()) {

            // Make certain the the current service is in the package
            // and enabled!
            //
            if (!s_threshdConfig.serviceInPackageAndEnabled(serviceName, pkg)) {
                if (log.isDebugEnabled())
                    log.debug("createThresholdingVisitor: address/service: " + hostAddress + "/" + serviceName + " not scheduled, service is not enabled or does not exist in package: " + pkg.getName());
                continue;
            }

            // Is the interface in the package?
            //
            log.debug("createThresholdingVisitor: checking ipaddress " + hostAddress + " for inclusion in pkg " + pkg.getName());
            boolean foundInPkg = s_threshdConfig.interfaceInPackage(hostAddress, pkg);
            if (!foundInPkg) {
                // The interface might be a newly added one, rebuild the package
                // to ipList mapping and again to verify if the interface is in
                // the package.
                //
                s_threshdConfig.rebuildPackageIpListMap();
                foundInPkg = s_threshdConfig.interfaceInPackage(hostAddress, pkg);
            }
            if (!foundInPkg) {
                if (log.isDebugEnabled())
                    log.debug("createThresholdingVisitor: address/service: " + hostAddress + "/" + serviceName + " not scheduled, interface does not belong to package: " + pkg.getName());
                continue;
            }

            // Getting thresholding-group for selected service and adding to groupNameList
            //
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
            			log.debug("createThresholdingVisitor:  address/service: " + hostAddress + "/" + serviceName + ". Adding Group " + groupName);
            		}
            	}
            }
        }
        
        // Create ThresholdingVisitor is groupNameList is not empty
        if (groupNameList.isEmpty()) {
            log.warn("createThresholdingVisitor: Can't create ThreshdingVisitor for " + hostAddress + "/" + serviceName);
            return null;
        }
        return new ThresholdingVisitor(nodeId, hostAddress, serviceName, repo, groupNameList);
    }
    
    protected ThresholdingVisitor(int nodeId, String hostAddress, String serviceName, RrdRepository repo, List<String> groupNameList) { 

        m_groupNameList=groupNameList;
        m_nodeId=nodeId;
        m_hostAddress=hostAddress;
        m_serviceName=serviceName;
        m_repository=repo;
        initThresholdState();
        log().debug(this + " just created!");
    }
    
    /**
     * Causes a new set of ThresholdEntity instances (i.e. threshold states) to be obtained for this visitor, at some appropraite
     * point in the near future 
     * Can be called when thresholding configuration has changed and we need to refresh the threshold state
     */
    public void initThresholdState() {
        log().debug("initThresholdState on "+this);
        m_needsRefresh=true; 
    }
    
    /**
     * When called, we're starting a new resource.  Clears out any stored attribute values from previous resource visits
     */
    public void visitResource(CollectionResource resource) {
        log().debug(this+" visiting resource "+resource);
        //Should only refresh our thresholds when we start checking a new collection; do the check now
        if (m_needsRefresh) {
            log().debug(this + " needs refresh of state; refreshing now");
            m_thresholdGroupList = new ArrayList<ThresholdGroup>();
            for (String groupName : m_groupNameList) {
                ThresholdGroup thresholdGroup = s_thresholdsDao.get(groupName);
                if (thresholdGroup == null) {
                    log().error("Could not get threshold group with name " + groupName);
                }
                m_thresholdGroupList.add(thresholdGroup);
                m_needsRefresh = false;
                if (log().isDebugEnabled()) {
                    StringBuffer resDebugMsg = new StringBuffer("Resource types after refresh are [");
                    resDebugMsg.append("node: { ");
                    if (thresholdGroup.getNodeResourceType() != null) {
                        resDebugMsg.append(thresholdGroup.getNodeResourceType());
                    }
                    resDebugMsg.append(" }; iface: { ");
                    if (thresholdGroup.getIfResourceType() != null) {
                        resDebugMsg.append(thresholdGroup.getIfResourceType());
                    }
                    resDebugMsg.append(" }; generic: { ");
                    if (thresholdGroup.getGenericResourceTypeMap() != null) {
                        for (String rType : thresholdGroup.getGenericResourceTypeMap().keySet()) {
                            resDebugMsg.append(rType + " ");
                        }
                    }
                    resDebugMsg.append(" } ]");
                    log().debug(resDebugMsg.toString());
                }
            }
        }
        m_numericAttributeValues=new HashMap<String,Double>();
        m_stringAttributeValues=new HashMap<String, String>();
    }

    /*
     * Stores the attribute locally so that on completion we can do thresholding
     * @see org.opennms.netmgt.collectd.AbstractCollectionSetVisitor#visitAttribute(org.opennms.netmgt.collectd.CollectionAttribute)
     */
    public void visitAttribute(CollectionAttribute attribute) {
        //Store the value away until we hit completeResource
        String numValue=attribute.getNumericValue();
        String attribName=attribute.getName();
        if(numValue!=null) {
            double doubleValue=Double.parseDouble(numValue);
            m_numericAttributeValues.put(attribName, doubleValue);
            log().debug("visitAttribute storing value "+doubleValue +" for attribute named "+attribName);
        } else {
          //No numeric value available; storing as a string
          String stringValue=attribute.getStringValue();  
          m_stringAttributeValues.put(attribName, stringValue);
          log().debug("visitAttribute storing value "+stringValue +" for attribute named "+attribName);
        }
    }
    
    private String getIfInfo(int nodeid, String ifLabel, String attributeName) {
        return new JdbcIfInfoGetter().getIfInfoForNodeAndLabel(m_nodeId, ifLabel).get(attributeName);
    }
    
    public void completeResource(CollectionResource resource) {
        Date date=new Date();
        List<Event> eventsList=new ArrayList<Event>();
        if(log().isDebugEnabled()) {
            log().debug("Completing Resource "+resource.getResourceTypeName() + "/" + resource.getOwnerName() +"/"+(resource.getInstance()==null?"default":resource.getInstance())
                        +": "+resource.getType()+" ("+resource+")");
        }
        
        File resourceDir= resource.getResourceDir(m_repository); //used repeatedly; only obtain once
        for (ThresholdGroup thresholdGroup : m_thresholdGroupList) {
            // Find the appropriate ThresholdEntity map to use based on the type of
            // CollectionResource we're looking at
            Map<String, ThresholdEntity> entityMap;
            String resourceType = resource.getResourceTypeName();
            String ifLabel = null;
            if ("node".equals(resourceType)) {
                entityMap = thresholdGroup.getNodeResourceType().getThresholdMap();
            } else if ("if".equals(resourceType)) {
                entityMap = thresholdGroup.getIfResourceType().getThresholdMap();
                ifLabel = resource.getResourceDir(m_repository).getName();
                if (m_snmpIfIndex == null) {
                    m_snmpIfIndex = this.getIfInfo(m_nodeId, ifLabel, "snmpifindex");
                }
            } else {
                Map<String, ThresholdResourceType> typeMap = thresholdGroup.getGenericResourceTypeMap();
                if (typeMap == null) {
                    log().error("Generic Resource Type map was null (this shouldn't happen)");
                    return; // Cannot sensibly continue
                }
                ThresholdResourceType thisResourceType = typeMap.get(resourceType);
                if (thisResourceType == null) {
                    log().warn("No thresholds configured for resource type " + resourceType + ".  Not processing this collection ");
                    return; // Cannot sensibly continue; might be simply no thresholds configured, or maybe something deeper
                }
                entityMap = thisResourceType.getThresholdMap();
            }

            // Now look at each
            for(String key : entityMap.keySet()) {
                ThresholdEntity threshold=entityMap.get(key);
                if(passedThresholdFilters(resourceDir, thresholdGroup.getName(), threshold.getDatasourceType(), threshold)) {
                    log().info("Processing threshold "+key);
                    Collection<String> requiredDatasources=threshold.getRequiredDatasources();
                    Map<String, Double> values=new HashMap<String,Double>();
                    boolean valueMissing=false;
                    for(String ds: requiredDatasources) {
                        log().info("Looking for datasource "+ds);
                        Double dsValue=m_numericAttributeValues.get(ds);
                        if(dsValue==null) {
                            log().info("Could not get data source value for '" + ds + "'.  Not evaluating threshold.");
                            valueMissing=true;
                        }
                        values.put(ds,dsValue);
                    }
                    if(!valueMissing) {
                        log().info("All values found, evaluating");
                        List<Event> thresholdEvents=threshold.evaluateAndCreateEvents(resource.getInstance(), values, date);   
                    
                        //Check in the fetched values first; if the label isn't there, check on disk.
                        //This is acceptable because the strings.properties files tend to be tiny, so the performance implications are minimal
                        // whereas there is (IMHO) a reasonable chance that the labels users want to use may well not have been collected
                        // in the same collection set being visited now
                        String dsLabelValue=m_stringAttributeValues.get(threshold.getDatasourceLabel());
                        if(dsLabelValue==null) {
                            log().info("No datasource label found in CollectionSet, fetching from storage");
                            dsLabelValue= getDataSourceLabelFromFile(resourceDir, threshold);
                        }
                        completeEventList(thresholdEvents, ifLabel, m_snmpIfIndex, dsLabelValue); //Finishes off events with details that a ThresholdEntity shouldn't know
                        eventsList.addAll(thresholdEvents);
                    }
                } else {
                    log().info("Not processing threshold "+ key +" because no filters matched");
                }
            }
    	}
    	
        if (eventsList.size() > 0) {
            //Create the structure which can be passed around with events in it
            Events events=new Events();
            for (Event event: eventsList) {
                events.addEvent(event);
            }
            try {
                
                Log eventLog = new Log();
                eventLog.setEvents(events);
                //Used to use a proxy for this, but the threshd implementation was  just a simple wrapper around the following call
                // (not even any other code).  Rather than try to get an Event Proxy into this class, it's easier to just call direct.
                EventIpcManagerFactory.getIpcManager().sendNow(eventLog);
            } catch (Exception e) {
                log().info("completeResource: Failed sending threshold events: " + e, e);
                thresholdingFinished(false);
            }
        }

        thresholdingFinished(true);
    }
    
    private void thresholdingFinished(boolean success) {
        if (success != m_success) {
            // Generate transition events
            if (log().isDebugEnabled())
                log().debug("run: change in thresholding status, generating event.");
    
            // Send the appropriate event
            if(success) {
                sendEvent(EventConstants.THRESHOLDING_SUCCEEDED_EVENT_UEI);
            } else {
                sendEvent(EventConstants.THRESHOLDING_FAILED_EVENT_UEI);
            }
        }
        // Set the new status so we can check next time
        m_success = success;

    }


    
    private void sendEvent(String uei) {
        Category log = log();
        Event event = new Event();
        event.setUei(uei);
        event.setNodeid((long) m_nodeId);
        event.setInterface(m_hostAddress);
        event.setService(m_serviceName);
        event.setSource("OpenNMS.Threshd");
        try {
            event.setHost(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            event.setHost("unresolved.host");
        }

        event.setTime(EventConstants.formatToString(new java.util.Date()));

        // Send the event
        //
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(event);
        } catch (Exception ex) {
            log.error("Failed to send the event " + uei + " for interface " + m_hostAddress, ex);
        }

        if (log.isDebugEnabled())
            log.debug("sendEvent: Sent event " + uei + " for " + m_nodeId + "/" + m_hostAddress + "/" + m_serviceName);
    }
     
    private void completeEventList(List<Event> eventList, String ifLabel, String snmpifIndex, String dsLabelValue) {
        for (Event event : eventList) {
            // create the event to be sent
            event.setNodeid(m_nodeId);
            event.setService(m_serviceName);
            // Set event interface
            event.setInterface(m_hostAddress);

            // Add appropriate parms
            Parms eventParms = event.getParms();
            
            Parm eventParm;
            Value parmValue;
    
            // Add datasource label
            if (dsLabelValue != null) {
                eventParm = new Parm();
                eventParm.setParmName("label");
                parmValue = new Value();
                parmValue.setContent(dsLabelValue);
                eventParm.setValue(parmValue);
                eventParms.addParm(eventParm);
            }
    
            // Add interface parms if available
            if (ifLabel != null) {
                // Add ifLabel
                eventParm = new Parm();
                eventParm.setParmName("ifLabel");
                parmValue = new Value();
                parmValue.setContent(ifLabel);
                eventParm.setValue(parmValue);
                eventParms.addParm(eventParm);
            }
    
            
            if (m_snmpIfIndex != null) {
                // Add ifIndex
                eventParm = new Parm();
                eventParm.setParmName("ifIndex");
                parmValue = new Value();
                parmValue.setContent(m_snmpIfIndex);
                eventParm.setValue(parmValue);
                eventParms.addParm(eventParm);
            }
        }
    }
    
    /**
     * Get the value to use for the ds-label from this threshold.  
     */
    private String getDataSourceLabelFromFile(File directory, ThresholdEntity threshold) {
        String dsLabelValue = null;
        
        try {
            String key = threshold.getDatasourceLabel();
            dsLabelValue = (key == null ? null : ResourceTypeUtils.getStringProperty(directory, key));
        } catch (DataAccessException e) {
            if (log().isDebugEnabled()) {
                log().debug ("getDataSourceLabel: I/O exception when looking for strings.properties file for node id: " + m_nodeId+ " looking here: " + directory + ": " + e, e);
            }
        }
        
        return (dsLabelValue == null ? "Unknown" : dsLabelValue);

    }

    /*
     * If Threshold has Filters defined for selected ThresholdGroup/DataSource/ResourceType then, apply filter rules.
     * TODO: What happend if getAttributeValue returns null ?
     */
    private boolean passedThresholdFilters(File resourceDir, String thresholdGroup, String resourceType, ThresholdEntity thresholdEntity) {

        // Find the filters for threshold definition for selected group/dataSource
        ResourceFilter[] filters = thresholdEntity.getThresholdConfig().getBasethresholddef().getResourceFilter();
        if (filters.length == 0) return true;

        // Threshold definition with filters must match ThresholdEntity (checking DataSource and ResourceType)
        log().debug("passedThresholdFilters: resource=" + resourceDir.getName() + ", group=" + thresholdGroup + ", type=" + resourceType + ", filters=" + filters.length);
        int count = 1;
        for (ResourceFilter f : filters) {
            log().debug("passedThresholdFilters: filter #" + count + ": field=" + f.getField() + ", regex='" + f.getContent() + "'");
            count++;
            // Read Resource Attribute and apply filter rules if attribute is not null
            String attr = getAttributeValue(resourceDir, resourceType, f.getField());
            if (attr != null) {
            	Pattern p = Pattern.compile(f.getContent());
            	Matcher m = p.matcher(attr);
                boolean pass = m.find();
                log().debug("passedThresholdFilters: the value of " + f.getField() + " is " + attr + ". Pass filter? " + pass);
                if (pass) return true;
            }
        }
        return false;
    }
    
    /*
     * This directly access database to get OnmsSnmpInterface (snmpinterface table on database) data
     * for selected Interface ID.
     */
    private String getAttributeValue(File resourceDirectory, String resourceType, String attribute) {
         log().debug("Getting Value for " + resourceType + "::" + attribute);
        String value = null;
        // Interface ID or Resource ID from data path
        if (attribute.equals("ID")) {
            return resourceDirectory.getName();
        }
        try {
            if (resourceType.equals("if")) {
                String ifLabel = resourceDirectory.getName();
                value = this.getIfInfo(m_nodeId, ifLabel, attribute);
            } else {
                //Check in the current set of collected string vars first, then check numeric vars; only then check on disk (in string properties)
                value=m_stringAttributeValues.get(attribute);
                if(value==null) {
                    value=m_numericAttributeValues.get(attribute).toString();
                    if(value==null) {
                        log().debug("Value not found in collection set, getting from " + resourceDirectory);
                        value = ResourceTypeUtils.getStringProperty(resourceDirectory, attribute);
                    }
                }
            }
        } catch (Exception e) {
            log().warn("Can't get value for attribute " + attribute + ". " + e, e);
        }
        return value;
    }
    
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }


    public String toString() {
        return "ThresholdingVisitor for node "+m_nodeId+"("+m_hostAddress+"), thresholding groupa "+m_groupNameList+", "+m_serviceName;
    }
    
}
