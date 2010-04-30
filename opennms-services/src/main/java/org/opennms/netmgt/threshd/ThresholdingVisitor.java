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
// Modifications:
//
// 2008 June 11: Correct logic error when checking for generic resource types; update author - jeffg@opennms.org
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
import java.lang.Math;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.collectd.AbstractCollectionSetVisitor;
import org.opennms.netmgt.collectd.AliasedResource;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.collectd.IfInfo;
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
 * @author <a href="mailto:craig@opennms.org>Craig Miskell</a>
 * @author <a href="mailto:agalue@opennms.org>Alejandro Galue</a>
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
    
    //The last success status of thresholding; used to know if status has changed and events should be generated
    private boolean m_success = true; //Default to true at the start (we assume thresholding was working when OpenNMS starts up) 

    //The primary cache for numeric values pulled out of the collection Set;
    private Map<String, Double> m_cache = new HashMap<String,Double>();

    //The primary store of numeric values pulled out of the collection Set; flushed every run
    private Map<String, CollectionAttribute> m_numericAttributeValues;
    
    //The primary store of string values pulled out of the collection Set; flushed every run
    private Map<String, String> m_stringAttributeValues;

    //The set of ThresholdEntity instances
    private List<ThresholdGroup> m_thresholdGroupList;
    
    //Handy local reference to the rrd repository in use.  
    private RrdRepository m_repository;
    
    //Set to true by the reinit code when a refresh is required (at init time, or when thresholds change)
    private boolean m_needsRefresh=false;
    
    //Holds collection interval step. Counter attributes values must be returned as rates.
    private long m_interval;

    //Holds the IfInfo of the currently processed resource if it is an interface.
    private Map<String,String> m_currentIfInfo;

    public static ThresholdingVisitor createThresholdingVisitor(int nodeId, final String hostAddress, final String serviceName, final RrdRepository repo, Map<String,String> params, long interval) {
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
            if (log.isDebugEnabled()) {
                log.debug("createThresholdingVisitor: checking ipaddress " + hostAddress + " for inclusion in pkg " + pkg.getName());
            }
            boolean foundInPkg = s_threshdConfig.interfaceInPackage(hostAddress, pkg);
            if (!foundInPkg && Boolean.getBoolean("org.opennms.thresholds.filtersReloadEnabled")) {
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
            		for (org.opennms.netmgt.config.threshd.Parameter parameter : svc.getParameter()) {
            			if (parameter.getKey().equals("thresholding-group")) {
            				String groupName = parameter.getValue();
            			    groupNameList.add(groupName);
               	            if (log.isDebugEnabled()) {
                                log.debug("createThresholdingVisitor:  address/service: " + hostAddress + "/" + serviceName + ". Adding Group " + groupName);
            			    }
            			}
            		}
            	}
            }
        }
        
        // Create ThresholdingVisitor is groupNameList is not empty
        if (groupNameList.isEmpty()) {
            log.warn("createThresholdingVisitor: Can't create ThresholdingVisitor for " + hostAddress + "/" + serviceName);
            return null;
        }
        return new ThresholdingVisitor(nodeId, hostAddress, serviceName, repo, groupNameList, interval);
    }
    
    protected ThresholdingVisitor(int nodeId, final String hostAddress, final String serviceName, RrdRepository repo, List<String> groupNameList, long interval) { 
        m_interval=interval/1000; // Store interval in seconds
        m_groupNameList=groupNameList;
        m_nodeId=nodeId;
        m_hostAddress=hostAddress;
        m_serviceName=serviceName;
        m_repository=repo;
        initThresholdState();
        if (log().isDebugEnabled()) {
            log().debug(this + " just created!");
        }
    }
    
    /**
     * Causes a new set of ThresholdEntity instances (i.e. threshold states) to be obtained for this visitor, at some appropraite
     * point in the near future 
     * Can be called when thresholding configuration has changed and we need to refresh the threshold state
     */
    public void initThresholdState() {
        if (log().isDebugEnabled()) {
            log().debug("initThresholdState on "+this);
        }
        m_needsRefresh=true; 
    }
    
    /**
     * When called, we're starting a new resource.  Clears out any stored attribute values from previous resource visits
     */
    public void visitResource(CollectionResource resource) {
        if (resource instanceof IfInfo) {
            m_currentIfInfo = ((IfInfo) resource).getAttributesMap();
        } else if (resource instanceof AliasedResource) {
            m_currentIfInfo = ((AliasedResource) resource).getIfInfo().getAttributesMap();
        } else {
            m_currentIfInfo = null;
        }
        if (log().isDebugEnabled()) {
            log().debug(this+" visiting resource "+resource);
        }
        //Should only refresh our thresholds when we start checking a new collection; do the check now
        if (m_needsRefresh) {
            if (log().isDebugEnabled()) {
                log().debug(this + " needs refresh of state; refreshing now");
            }
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
        m_numericAttributeValues=new HashMap<String,CollectionAttribute>();
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
            m_numericAttributeValues.put(attribName, attribute);
            if (log().isDebugEnabled()) {
                log().debug("visitAttribute storing value "+numValue +" for attribute named "+attribName);
            }
        } else {
          //No numeric value available; storing as a string
          String stringValue=attribute.getStringValue();  
          m_stringAttributeValues.put(attribName, stringValue);
          if (log().isDebugEnabled()) {
              log().debug("visitAttribute storing value "+stringValue +" for attribute named "+attribName);
          }
        }
    }
    
    public void completeResource(CollectionResource resource) {
        Date date=new Date();
        List<Event> eventsList=new ArrayList<Event>();

        // The local cache for this instance of iterating through the numeric values
        // pulled out of the collection Set via getValue();
        Map<String, Double> localCache = new HashMap<String,Double>();

        if(log().isDebugEnabled()) {
            log().debug("Completing Resource "+resource.getResourceTypeName() + "/" + resource.getOwnerName() +"/"+(resource.getInstance()==null?"default":resource.getInstance())
                        +": "+resource.getType()+" ("+resource+")");
        }
        
        File resourceDir= resource.getResourceDir(m_repository); //used repeatedly; only obtain once
        for (ThresholdGroup thresholdGroup : m_thresholdGroupList) {
            // Find the appropriate ThresholdEntity map to use based on the type of
            // CollectionResource we're looking at
        	final String ifLabel = resource.getResourceDir(m_repository).getName();
        	final String resourceType = resource.getResourceTypeName();
            Map<String, Set<ThresholdEntity>> entityMap;
            boolean typeInterface = false;
            if ("node".equals(resourceType)) {
                entityMap = thresholdGroup.getNodeResourceType().getThresholdMap();
            } else if ("if".equals(resourceType)) {
                entityMap = thresholdGroup.getIfResourceType().getThresholdMap();
                typeInterface = true;
            } else {
                Map<String, ThresholdResourceType> typeMap = thresholdGroup.getGenericResourceTypeMap();
                if (typeMap == null) {
                    log().error("Generic Resource Type map was null (this shouldn't happen)");
                    return; // Cannot sensibly continue
                }
                ThresholdResourceType thisResourceType = typeMap.get(resourceType);
                if (thisResourceType == null) {
                    log().warn("No thresholds configured for resource type " + resourceType + ".  Not processing this collection ");
                    continue;
                }
                entityMap = thisResourceType.getThresholdMap();
            }

            // Now look at each
            for(String key : entityMap.keySet()) {
            	for (ThresholdEntity threshold : entityMap.get(key)) {
           	    
                   // Match up the threshold to the working RRD repo for the given snmp interface. (The 
                   // interface may be un-numbered, so this value cannot be cached.)
                   String snmpIfIndex;
                   String interfaceAddr;
                   if(typeInterface) {
                           if(null == m_currentIfInfo) {
                                   log().info("Could not get data interface information for '" + ifLabel + "'.  Not evaluating threshold.");
                                   continue;
                           }
 
                           snmpIfIndex = m_currentIfInfo.get("snmpifindex");
                           interfaceAddr = m_currentIfInfo.get("ipaddr");
                           
                           // Don't threshold the loopback interface on the router, or on invalid interfaces!
                           try {
                                   if(null == snmpIfIndex)
                                           continue;
                                   if(Integer.parseInt(snmpIfIndex) < 0)
                                           continue;
                           } catch(Exception e) {
                                   continue;
                           }
                   } else {
                           snmpIfIndex = null; // because this only make sense for interface resource
                           interfaceAddr = m_hostAddress;
                   }

	                if (passedThresholdFilters(resourceDir, thresholdGroup.getName(), threshold.getDatasourceType(), threshold)) {
	                    log().info("Processing threshold "+key+ " : " +threshold);
	                    
	                    Collection<String> requiredDatasources=threshold.getRequiredDatasources();
	                    Map<String, Double> values=new HashMap<String,Double>();
	                    boolean valueMissing=false;
	                    
	                    for(String ds: requiredDatasources) {
	                        log().info("Looking for datasource "+ds);

                            // Maintain the method level dsValue cache for this iteration through
                            // the data sources.  getValue can only be used once per ds per call
                            // to completeResource()
                            String vid = resource.toString() + "." + ds;
                            Double dsValue=localCache.get(vid);
                            if(dsValue==null) {
                                dsValue=getValue(resource, ds);
                                localCache.put(vid, dsValue);
                            } else if(log().isDebugEnabled()) {
                                log().debug("Using method level dsValue cache for datasource value=" + dsValue);
                            }

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
	                        completeEventList(thresholdEvents, ifLabel, snmpIfIndex, dsLabelValue, interfaceAddr); //Finishes off events with details that a ThresholdEntity shouldn't know
	                        eventsList.addAll(thresholdEvents);
	                    }
	                } else {
	                    log().info("Not processing threshold "+ key + " : " + threshold +" because no filters matched");
	                }
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
    
    private Double getValue(final CollectionResource resource, final String ds) {
        if (m_numericAttributeValues.get(ds) == null) {
            log().warn("getValue: can't find attribute called " + ds + " on " + resource);
            return null;
        }
        String numValue = m_numericAttributeValues.get(ds).getNumericValue();
        if (numValue == null) {
            log().warn("getValue: can't find numeric value for " + ds + " on " + resource);
            return null;
        }
        String id = resource.toString() + "." + ds;
        Double current = Double.parseDouble(numValue);
        if (m_numericAttributeValues.get(ds).getType().toLowerCase().startsWith("counter") == false) {
            if (log().isDebugEnabled()) {
                log().debug("getValue: " + id + "(gauge) value= " + current);
            }
            return current;
        }
        Double last = m_cache.get(id);
        if (log().isDebugEnabled()) {
            log().debug("getValue: " + id + "(counter) last=" + last + ", current=" + current);
        }
        m_cache.put(id, current);
        if (last == null) {
            return Double.NaN;
        }
        Double delta = current.doubleValue() - last.doubleValue();
        // wrapped counter handling(negative delta), rrd style
        if (delta < 0) {
            double newDelta = delta.doubleValue();
            // 2-phase adjustment method
            // try 32-bit adjustment
            newDelta += Math.pow(2, 32);
            if (newDelta < 0) {
                // try 64-bit adjustment
                newDelta += Math.pow(2, 64) - Math.pow(2, 32);
            }
            log().info("getValue: " + id + "(counter) wrapped counter adjusted last=" + last + ", current="
                        + current + ", olddelta=" + delta + ", newdelta=" + newDelta);
            delta = newDelta;
        }
        return delta/m_interval; // Treat counter as rates
    }

    private void thresholdingFinished(boolean success) {
        if (success != m_success) {
            // Generate transition events
            if (log().isDebugEnabled()) {
                log().debug("run: change in thresholding status, generating event.");
            }
            
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
     
    private void completeEventList(List<Event> eventList, String ifLabel, String snmpifIndex, String dsLabelValue, String ifAddress) {
        for (Event event : eventList) {
            // create the event to be sent
            event.setNodeid(m_nodeId);
            event.setService(m_serviceName);
            
            // Set event interface
            if(null != ifAddress)
            	event.setInterface(ifAddress);
            else
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
    
            
            if (snmpifIndex != null) {
                // Add ifIndex
                eventParm = new Parm();
                eventParm.setParmName("ifIndex");
                parmValue = new Value();
                parmValue.setContent(snmpifIndex);
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
     */
    private boolean passedThresholdFilters(final File resourceDir, final String thresholdGroup, final String resourceType, final ThresholdEntity thresholdEntity) {

        // Find the filters for threshold definition for selected group/dataSource
        ResourceFilter[] filters = thresholdEntity.getThresholdConfig().getBasethresholddef().getResourceFilter();
        if (filters.length == 0) return true;

        // Threshold definition with filters must match ThresholdEntity (checking DataSource and ResourceType)
        if (log().isDebugEnabled()) {
            log().debug("passedThresholdFilters: resource=" + resourceDir.getName() + ", group=" + thresholdGroup + ", type=" + resourceType + ", filters=" + filters.length);
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
            final String attr = getAttributeValue(resourceDir, resourceType, f.getField());
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
            }
        }
        if (operator.equals("and") && andResult)
            return true;
        return false;
    }
    
    /*
     * This directly access database to get OnmsSnmpInterface (snmpinterface table on database) data
     * for selected Interface ID.
     */
    private String getAttributeValue(final File resourceDirectory, final String resourceType, final String attribute) {
        if (log().isDebugEnabled()) {
            log().debug("Getting Value for " + resourceType + "::" + attribute);
        }
        String value = null;
        // Interface ID or Resource ID from data path
        if (attribute.equals("ID")) {
            return resourceDirectory.getName();
        }
        try {
            if (resourceType.equals("if")) {
                value = m_currentIfInfo != null ? m_currentIfInfo.get(attribute) : null;
            }
            if (value == null) {
                //Check in the current set of collected string vars first, then check numeric vars; only then check on disk (in string properties)
                value=m_stringAttributeValues.get(attribute);
                if(value==null) {
                    if (m_numericAttributeValues.get(attribute) != null)
                        value=m_numericAttributeValues.get(attribute).getNumericValue();
                    if(value==null) {
                        if (log().isDebugEnabled()) {
                            log().debug("Value not found in collection set, getting from " + resourceDirectory);
                        }
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
        return "ThresholdingVisitor for node "+m_nodeId+"("+m_hostAddress+"), thresholding groups: "+m_groupNameList+", on service "+m_serviceName;
    }
    
}
