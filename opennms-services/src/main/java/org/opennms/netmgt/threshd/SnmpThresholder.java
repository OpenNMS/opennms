//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jan 29: Formatting; refactor code to minimize size of methods; use Java 5 generics; use new method for evaluating thresholds and building events. - dj@opennms.org
// 2005 Nov 29: Added a method to allow for labels in Threshold events
// 2005 Jan 03: minor mod to support lame SNMP hosts
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 22: Added a threshold rearm event.
// 2002 Jul 08: Modified code to allow for Threshold-based event notifications.
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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Expression;
import org.opennms.netmgt.config.threshd.ResourceFilter;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.netmgt.dao.support.RrdFileConstants;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

/**
 * <P>
 * The SnmpThresholder class ...
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @deprecated No longer used - see ThresholdingVisitor
 */
public final class SnmpThresholder implements ServiceThresholder {

    private String m_serviceName;
    
    private ThresholdsDao m_thresholdsDao;
    
    private Map<NetworkInterface, SnmpThresholdNetworkInterface> m_snmpThresholdNetworkInterfaces;

    private IfInfoGetter m_ifInfoGetter;

    /**
     * <P>
     * Returns the name of the service that the plug-in collects ("SNMP").
     * </P>
     * 
     * @return The service that the plug-in collects.
     */
    public String serviceName() {
        return m_serviceName;
    }

    /**
     * <P>
     * Initialize the service thresholder.
     * </P>
     * 
     * @param parameters
     *            Not currently used.
     * 
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     * 
     */
    @SuppressWarnings("unchecked")
    public void initialize(Map parameters) {

        m_serviceName = (String)parameters.get("svcName");
        
        setupThresholdsDao();
        setupIfInfoGetter();
       
        log().debug("initialize: successfully instantiated RRD subsystem");
       
        m_snmpThresholdNetworkInterfaces = Collections.synchronizedMap(new HashMap<NetworkInterface, SnmpThresholdNetworkInterface>()); 
    }

    private void setupIfInfoGetter() {
        setIfInfoGetter(new JdbcIfInfoGetter());
    }

    public void reinitialize() {
        setupThresholdsDao();
    }
    
    private void setupThresholdsDao() {
       DefaultThresholdsDao defaultThresholdsDao = new DefaultThresholdsDao();
        
        try {
            defaultThresholdsDao.setThresholdingConfigFactory(ThresholdingConfigFactory.getInstance());
            defaultThresholdsDao.afterPropertiesSet();
        } catch (Throwable t) {
            log().error("initialize: Could not initialize DefaultThresholdsDao: " + t, t);
            throw new RuntimeException("Could not initialize DefaultThresholdsDao: " + t, t);
        }
        
        setThresholdsDao(defaultThresholdsDao);

    }

    /**
     * Responsible for freeing up any resources held by the thresholder.
     */
    public void release() {
        // Nothing to release...
    }

    /**
     * Responsible for performing all necessary initialization for the specified
     * interface in preparation for thresholding.
     * 
     * @param iface
     *            Network interface to be prepped for thresholding.
     * @param parameters
     *            Key/value pairs associated with the package to which the
     *            interface belongs..
     * 
     */
    @SuppressWarnings("unchecked")
    public void initialize(ThresholdNetworkInterface netIface, Map parms) {
        SnmpThresholdNetworkInterface snmpThresholdNetworkInterface = new SnmpThresholdNetworkInterface(m_thresholdsDao, netIface, parms);
        m_snmpThresholdNetworkInterfaces.put(netIface, snmpThresholdNetworkInterface);

        SnmpThresholdConfiguration config = snmpThresholdNetworkInterface.getThresholdConfiguration();

        if (!snmpThresholdNetworkInterface.isIPV4()) {
            throw new RuntimeException("Unsupported interface type, only TYPE_IPV4 currently supported");
        }

        if (log().isDebugEnabled()) {
            log().debug("initialize: dumping node thresholds defined for " + snmpThresholdNetworkInterface + ":");
            for (Set<ThresholdEntity> entitySet : config.getNodeResourceType().getThresholdMap().values()) {
                for (ThresholdEntity entity : entitySet) { 
                	log().debug("    " + entity);
                }
            }

            log().debug("initialize: dumping interface thresholds defined for " + snmpThresholdNetworkInterface + ":");
            for (Set<ThresholdEntity> entitySet : config.getIfResourceType().getThresholdMap().values()) {
            	for (ThresholdEntity entity : entitySet) {
            		log().debug("    " + entity);
            	}
            }
            
            log().debug("initialize: dumping generic resources thresholds defined for " + snmpThresholdNetworkInterface + ":");
            for (String resourceType : config.getGenericResourceTypeMap().keySet()) {
                for (Set<ThresholdEntity> entitySet : config.getGenericResourceTypeMap().get(resourceType).getThresholdMap().values()) {
                	for (ThresholdEntity entity : entitySet) {
                		log().debug("    " + resourceType + "." + entity);
                	}
                }
            }
        }

        if (log().isDebugEnabled()) {
            log().debug("initialize: initialization completed for " + snmpThresholdNetworkInterface);
        }
        
        return;
    }

    /**
     * Responsible for releasing any resources associated with the specified
     * interface.
     * 
     * @param iface
     *            Network interface to be released.
     */
    public void release(ThresholdNetworkInterface iface) {
        m_snmpThresholdNetworkInterfaces.remove(iface);
    }

    /**
     * Perform threshold checking.
     * 
     * @param iface
     *            Network interface to be data collected.
     * @param eproxy
     *            Eventy proxy for sending events.
     * @param parameters
     *            Key/value pairs from the package to which the interface
     *            belongs.
     */
    @SuppressWarnings("unchecked")
    public int check(ThresholdNetworkInterface netIface, EventProxy eproxy, Map parms) {
        SnmpThresholdNetworkInterface snmpThresholdNetworkInterface = m_snmpThresholdNetworkInterfaces.get(netIface);
        if (snmpThresholdNetworkInterface == null) {
            log().warn("check: interface has not been initialized in this thresholder: " + netIface);
            return THRESHOLDING_FAILED;
        }

        SnmpThresholdConfiguration config = snmpThresholdNetworkInterface.getThresholdConfiguration();

        // Get configuration parameters
        if (log().isDebugEnabled()) {
        	log().debug("check: service= " + serviceName() + " address= " + snmpThresholdNetworkInterface.getIpAddress() + " thresholding-group=" + config.getGroupName() + " interval=" + config.getInterval() + "ms range=" + config.getRange() + " mS");
        }

        // RRD Repository attribute
        if (log().isDebugEnabled()) {
            log().debug("check: rrd repository=" + config.getRrdRepository());
        }


        /*
         * -----------------------------------------------------------
         * 
         * Perform node-level threshold checking
         *
         * -----------------------------------------------------------
         */

        // Get File object representing the node directory
        File nodeDirectory = new File(config.getRrdRepository(), snmpThresholdNetworkInterface.getNodeId().toString());
        if (!RrdFileConstants.isValidRRDNodeDir(nodeDirectory)) {
            log().info("Node directory for " + snmpThresholdNetworkInterface.getNodeId() + "/" + snmpThresholdNetworkInterface.getIpAddress() + " does not exist or is not a valid RRD node directory.");
            log().info("Threshold checking failed for primary SNMP interface " + snmpThresholdNetworkInterface.getIpAddress());
            return THRESHOLDING_FAILED;
        }

        // Create empty Events object to hold any threshold events generated during the thresholding check
        Events events = new Events();

        // Date stamp for all outgoing events
        Date date = new Date();

        try {
        	checkNodeDir(nodeDirectory, snmpThresholdNetworkInterface, date, events);
        } catch (IllegalArgumentException e) {
            log().info("check: Threshold checking failed for primary SNMP interface " + snmpThresholdNetworkInterface.getIpAddress() + ": " + e, e);
            return THRESHOLDING_FAILED;
        }

        /*
         * -----------------------------------------------------------
         * 
         * Perform interface-level threshold checking
         *
         * -----------------------------------------------------------
         */

        /*
         * Iterate over node directory contents and call
         * checkInterfaceDirectory() for any/all RRD interface
         * directories.
         */
        File[] files = nodeDirectory.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);
        if (files != null) {
            for (File file : files) {
                try {
                    // Found interface directory...
                    checkIfDir(file, snmpThresholdNetworkInterface, date, events);
                } catch (IllegalArgumentException e) {
                    log().info("check: Threshold checking failed for primary SNMP interface " + snmpThresholdNetworkInterface.getIpAddress() + ": " + e, e);
                    return THRESHOLDING_FAILED;
                }
            }
        }

        /*
         * -----------------------------------------------------------
         * 
         * Perform generic resources threshold checking
         *
         * -----------------------------------------------------------
         */

        /*
         * Iterate over generic resource directory contents and call
         * checkGenericResourceDirectory() for any/all RRD interface
         * directories.
         */
        if (config.getGenericResourceTypeMap().size() > 0) {
            for (String resourceType : config.getGenericResourceTypeMap().keySet()) {
                File file = new File(nodeDirectory, resourceType);
                try {
                    // Found resource directory...
                    checkResourceDir(file, snmpThresholdNetworkInterface, date, events);
                } catch (IllegalArgumentException e) {
                    log().info("check: Threshold checking failed for primary SNMP interface " + snmpThresholdNetworkInterface.getIpAddress() + ": " + e, e);
                    return THRESHOLDING_FAILED;
                }
            }
        }

        
        // Send created events
        if (events.getEventCount() > 0) {
            try {
                Log eventLog = new Log();
                eventLog.setEvents(events);
                eproxy.send(eventLog);
            } catch (EventProxyException e) {
                log().info("check: Failed sending threshold events: " + e, e);
                return THRESHOLDING_FAILED;
            }
        }

        return THRESHOLDING_SUCCEEDED;
    }

    /**
     * Performs threshold checking on an SNMP RRD node directory.
     * 
     * @param directory
     *            RRD repository directory
     * @param snmpIface TODO
     * @param date
     *            Source for timestamp to be used for all generated events
     * @param events
     *            Castor events object containing any events to be generated as
     *            a result of threshold checking.
     * @param interval
     *            Configured thresholding interval
     * @param range
     *            Time interval before last possible PDP is considered
     *            "out of date"
     * @param thresholdMap
     *            Map of node level ThresholdEntity objects keyed by datasource
     *            name.
     * @param nodeId
     *            Node identifier
     * @param primary
     *            Primary SNMP interface address
     * @throws IllegalArgumentException
     *             if path parameter is not a directory.
     */
    protected void checkNodeDir(File directory, SnmpThresholdNetworkInterface thresholdNetworkInterface, Date date, Events events) throws IllegalArgumentException {
        Assert.notNull(directory, "directory argument cannot be null");
        Assert.notNull(thresholdNetworkInterface, "thresholdNetworkInterface argument cannot be null");
        Assert.notNull(date, "date argument cannot be null");
        Assert.notNull(events, "events argument cannot be null");
        
        SnmpThresholdConfiguration config = thresholdNetworkInterface.getThresholdConfiguration();
        Assert.notNull(config, "getThresholdConfiguration() of thresholdNetworkInterface argument cannot be null");
        Assert.notNull(thresholdNetworkInterface.getNetworkInterface(), "getNetworkInterface() of thresholdNetworkInterface argument cannot be null");
        Assert.notNull(thresholdNetworkInterface.getNodeId(), "getNodeId() of thresholdNetworkInterface argument cannot be null");
        Assert.notNull(thresholdNetworkInterface.getInetAddress(), "getInetAddress() of thresholdNetworkInterface argument cannot be null");

        if (log().isDebugEnabled()) {
            log().debug("checkNodeDir: threshold checking node dir: " + directory.getAbsolutePath());
        }
        
        Map<String, Set<ThresholdEntity>> thresholdMap = thresholdNetworkInterface.getNodeThresholdMap();
        
        for(String threshKey  :thresholdMap.keySet()) {
        	for (ThresholdEntity thresholdEntity : thresholdMap.get(threshKey)) {
        		processThresholdForNode(directory, thresholdNetworkInterface, date, events, thresholdEntity);
        	}
        }
    }
    
    private List<Event> processThreshold(File directory, SnmpThresholdNetworkInterface snmpIface, ThresholdEntity threshold, Date date) {
        //Find out what data sources this threshold needs, check if they are available, and if so,
        // then get them and evaluate with them
        SnmpThresholdConfiguration thresholdConfiguration = snmpIface.getThresholdConfiguration();
        Collection<String> requiredDatasources=threshold.getRequiredDatasources();
        Map<String, Double> values=new HashMap<String,Double>();
        String group = snmpIface.getThresholdConfiguration().getGroupName();
        for(String ds: requiredDatasources) {
            File dsFile= ResourceTypeUtils.getRrdFileForDs(directory,ds);
            Double dsValue=null;
            if(dsFile.exists() && passedThresholdFilters(directory, group, threshold.getDatasourceType(), ds)) {
                dsValue = getDataSourceValue(thresholdConfiguration, dsFile, ds);
            }
            if(dsValue==null) {
                log().info("Could not get data source value for '" + ds + "'.  Not evaluating threshold.");
                return null;
            }
            values.put(ds,dsValue);
        }
        List<Event> eventList=threshold.evaluateAndCreateEvents(values, date);
        return eventList;
    }

    private String getDsLabel(ThresholdEntity threshold) {
        String dsLabelValue = threshold.getDatasourceLabel();
        if(dsLabelValue == null) {
            dsLabelValue = "Null";
        }
        return dsLabelValue;
    }
    
    private void processThresholdForNode(File directory, SnmpThresholdNetworkInterface snmpIface, Date date, Events events, ThresholdEntity threshold)  {
        List<Event> eventList=processThreshold(directory, snmpIface, threshold, date);
        if (eventList==null || eventList.size() == 0) {
            //Nothing to do, so return
            return;
        }
        completeEventListAndAddToEvents(events, eventList, snmpIface, null, getDsLabel(threshold));
    }
    
    private void processThresholdForInterface(File directory, SnmpThresholdNetworkInterface snmpIface, Date date, Events events, ThresholdEntity threshold, String ifLabel, Map<String, String> ifDataMap)  {
        List<Event> eventList=processThreshold(directory, snmpIface, threshold, date);
        if (eventList==null || eventList.size() == 0) {
            //Nothing to do, so return
            return;
        }

        if (ifDataMap.size() == 0 && ifLabel != null) {
            populateIfDataMap(ifDataMap, snmpIface.getNodeId().intValue(), ifLabel);
        }
        completeEventListAndAddToEvents(events, eventList, snmpIface, ifDataMap, getDsLabel(threshold));
    }
    
    private void processThresholdForResource(File directory, SnmpThresholdNetworkInterface snmpIface, Date date, Events events, ThresholdEntity threshold, String resource)  {
        List<Event> eventList=processThreshold(directory, snmpIface, threshold, date);
        if (eventList==null || eventList.size() == 0) {
            return;
        }

        completeEventListAndAddToEvents(events, eventList, snmpIface, null, resource);
    }

    /**
     * Performs threshold checking on an SNMP RRD interface directory.
     * 
     * @param directory
     *            RRD repository directory
     * @param snmpIface TODO
     * @param date
     *            Source for timestamp to be used for all generated events
     * @param allIfThresholdMap
     *            Map of threshold maps indexed by ifLabel
     * @param events
     *            Castor events object containing any events to be generated as
     *            a result of threshold checking.
     * @param nodeId
     *            Node identifier
     * @param primary
     *            Primary SNMP interface address
     * @param interval
     *            Configured thresholding interval
     * @param range
     *            Time interval before last possible PDP is considered
     *            "out of date"
     * @param baseIfThresholdMap
     *            Map of configured interface level ThresholdEntity objects
     *            keyed by datasource name.
     * @throws IllegalArgumentException
     *             if path parameter is not a directory.
     */
    protected void checkIfDir(File directory, SnmpThresholdNetworkInterface snmpIface, Date date, Events events) throws IllegalArgumentException {
        // TODO: do more specific and thorough assertions on arguments

        // Sanity Check
        if (directory == null || snmpIface.getNodeId() == null || snmpIface.getInetAddress() == null || date == null || events == null) {
            throw new IllegalArgumentException("Null parameters not permitted.");
        }

        if (log().isDebugEnabled()) {
            log().debug("checkIfDir: threshold checking interface dir: " + directory.getAbsolutePath());
        }

        String ifLabel = directory.getName();
        if (log().isDebugEnabled()) {
            log().debug("checkIfDir: ifLabel=" + ifLabel);
        }

        Map<String, Set<ThresholdEntity>> thresholdMap = snmpIface.getInterfaceThresholdMap(ifLabel);
        
        Map<String, String> ifDataMap = new HashMap<String, String>();
        for(String threshKey  :thresholdMap.keySet()) {
        	for (ThresholdEntity thresholdEntity : thresholdMap.get(threshKey)) {
        		processThresholdForInterface(directory, snmpIface, date, events, thresholdEntity, ifLabel, ifDataMap);
        	}
        }
    }

    protected void checkResourceDir(File directory, SnmpThresholdNetworkInterface snmpIface, Date date, Events events) throws IllegalArgumentException {
        // TODO: do more specific and thorough assertions on arguments

        // Sanity Check
        if (directory == null || snmpIface.getNodeId() == null || snmpIface.getInetAddress() == null || date == null || events == null) {
            throw new IllegalArgumentException("Null parameters not permitted.");
        }

        if (log().isDebugEnabled()) {
            log().debug("checkResourceDir: threshold checking generic resource dir: " + directory.getAbsolutePath());
        }
        
        String resourceType = directory.getName();
        
        if (!directory.exists()) {
        	log().debug("Aborting check because this node does not support Resource Type " + resourceType);
        	return;
        }

        SnmpThresholdConfiguration config = snmpIface.getThresholdConfiguration(); 
        if (log().isDebugEnabled()) {
            log().debug("checkResourceDir: group="  + config.getGroupName() + ", resourceType=" + resourceType);
        }
        
        ThresholdResourceType thresholdResourceType = config.getGenericResourceTypeMap().get(resourceType);
        if (thresholdResourceType == null) {
            log().info("No generic resources for group " + config.getGroupName());
            return;
        }
        Map<String, Set<ThresholdEntity>> thresholdMap = thresholdResourceType.getThresholdMap();
        
        File[] files = directory.listFiles();
        for (File file : files) {
            String resource = file.getName();
            for(String threshKey  :thresholdMap.keySet()) {
                if (log().isDebugEnabled()) {
                    log().debug("checkResourceDir: resource=" + resource);
                }
                for (ThresholdEntity thresholdEntity : thresholdMap.get(threshKey)) {
	                String dsLabelValue = getDataSourceLabel(file, snmpIface, thresholdEntity);
	                processThresholdForResource(file, snmpIface, date, events, thresholdEntity, dsLabelValue);
                }
            }
        }
    }
    
    private ResourceFilter[] getThresholdFilters(String thresholdGroup, String dataSource) {
        Collection<Basethresholddef> thresholds = m_thresholdsDao.getThresholdingConfigFactory().getThresholds(thresholdGroup);
        for (Basethresholddef thresh : thresholds) {
            if (thresh instanceof Threshold) {
                Threshold t = (Threshold)thresh;
                if (t.getDsName().equals(dataSource)) {
                    return t.getResourceFilter();
                }
            } else {
                Expression e = (Expression)thresh;
                if (e.getExpression().indexOf(dataSource) > 0) {
                    return e.getResourceFilter();
                }
            }
        }
        ResourceFilter[] filters = {};
        return filters;
    }
    
    /*
     * If Threshold has Filters defined for selected ThresholdGroup/DataSource/ResourceType then, apply filter rules.
     * TODO: What happend if getAttributeValue returns null ?
     */
    protected boolean passedThresholdFilters(File resourceDir, String thresholdGroup, String resourceType, String dataSource) {

        // Find the filters for threshold definition for selected group/dataSource
        ResourceFilter[] filters = getThresholdFilters(thresholdGroup, dataSource);
        if (filters.length == 0) return true;

        // Threshold definition with filters must match ThresholdEntity (checking DataSource and ResourceType)
        log().debug("checkFilters: resource=" + resourceDir.getName() + ", group=" + thresholdGroup + ", type=" + resourceType + ", filters=" + filters.length);
        int count = 1;
        for (ResourceFilter f : filters) {
            log().debug("checkFilters: filter #" + count + ": field=" + f.getField() + ", regex=" + f.getContent());
            count++;
            // Read Resource Attribute and apply filter rules if attribute is not null
            String attr = getAttributeValue(resourceDir, resourceType, f.getField());
            if (attr != null) {
                Pattern p = Pattern.compile(f.getContent());
                Matcher m = p.matcher(attr);
                boolean pass = m.find();
                log().debug("checkFilters: the value of " + dataSource + " is " + attr + ". Pass filter? " + pass);
                if (pass) return true;
            }
        }
        return false;
    }
    
    private void completeEventListAndAddToEvents(Events events, List<Event> eventList, SnmpThresholdNetworkInterface snmpIface, Map<String, String> ifDataMap, String dsLabelValue) {
        // TODO: do more specific and thorough assertions on arguments
        for (Event event : eventList) {
            /*
            Integer nodeId = snmpIface.getNodeId();
            InetAddress primary = snmpIface.getInetAddress();
            if (nodeId == null || primary == null) {
                throw new IllegalArgumentException("nodeid, primary, and threshold cannot be null.");
            }
    
            if (log().isDebugEnabled()) {
                log().debug("createEvent: nodeId=" + nodeId + " primaryAddr=" + primary + " ds=" + threshold.getDsName() + " uei=" + uei);
    
                if (ifDataMap != null) {
                    log().debug("createEvent: specific interface data: ifAddr=" + ifDataMap.get("ipaddr") + " macAddr=" + ifDataMap.get("snmpphysaddr") + " ifName=" + ifDataMap.get("snmpifname") + " ifDescr=" + ifDataMap.get("snmpifdescr") + " ifIndex=" + ifDataMap.get("snmpifindex") + " ifLabel=" + ifDataMap.get("iflabel"));
                }
            }
            */
    
            // create the event to be sent
            event.setNodeid(snmpIface.getNodeId().longValue());
            event.setService(this.serviceName());
    
            // Set event interface
            if (ifDataMap == null || ifDataMap.get("ipaddr") == null) {
                // Node level datasource
                if (snmpIface.getInetAddress() != null) {
                    event.setInterface(snmpIface.getInetAddress().getHostAddress());
                }
            } else {
                /*
                 * Interface-level datasource
                 * 
                 * NOTE: Non-IP interfaces will have an
                 * address of "0.0.0.0".
                 */
                String ifAddr = ifDataMap.get("ipaddr");
                event.setInterface(ifAddr);
            }
        
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
            if (ifDataMap != null && ifDataMap.get("iflabel") != null) {
                // Add ifLabel
                eventParm = new Parm();
                eventParm.setParmName("ifLabel");
                parmValue = new Value();
                parmValue.setContent(ifDataMap.get("iflabel"));
                eventParm.setValue(parmValue);
                eventParms.addParm(eventParm);
            }
    
            if (ifDataMap != null && ifDataMap.get("snmpifindex") != null) {
                // Add ifIndex
                eventParm = new Parm();
                eventParm.setParmName("ifIndex");
                parmValue = new Value();
                parmValue.setContent(ifDataMap.get("snmpifindex"));
                eventParm.setValue(parmValue);
                eventParms.addParm(eventParm);
            }

            events.addEvent(event);
        }
    }

    /**
     * Use RRD strategy to "fetch" value of the datasource from the RRD file
     * using the threshold configuration.
     */
    private Double getDataSourceValue(SnmpThresholdConfiguration thresholdConfiguration, File file, String datasource) {
        Double dsValue;

        try {
        	if (thresholdConfiguration.getRange() != 0) {
        		if (log().isDebugEnabled()) {
                    log().debug("Checking datasource '" + datasource + "' for values within " + thresholdConfiguration.getRange() + " milliseconds of last possible PDP with interval " + thresholdConfiguration.getInterval() + ".");
                }
        		dsValue = RrdUtils.fetchLastValueInRange(file.getAbsolutePath(), datasource, thresholdConfiguration.getInterval(), thresholdConfiguration.getRange());
        	} else {
        		if (log().isDebugEnabled()) {
                    log().debug("Checking datasource '" + datasource + "' for value of last possible PDP only with interval " + thresholdConfiguration.getInterval() + ".");
                }
        		dsValue = RrdUtils.fetchLastValue(file.getAbsolutePath(), datasource, thresholdConfiguration.getInterval());
        	}
        } catch (NumberFormatException e) {
            log().warn("Unable to convert retrieved value for datasource '" + datasource + "' to a double: " + e);
            return null;
        } catch (RrdException e) {
            log().info("An error occurred retriving the last value for datasource '" + datasource + "': " + e, e);
            return null;
        }

        if (dsValue == null) {
            log().info("fetch value for data source '" + datasource + "' was null.");
            return null;
        }
        
        if (dsValue.isNaN()) {
            log().info("fetch value for data source '" + datasource + "' was NaN.");
            return null;
        }
        
        return dsValue;
    }

    /**
     * File name has format: <datsource><extension>
     *
     * @return the fileName with the <extension> portion stripped off the end.
     */
    protected String stripRrdExtension(String fileName) {
        if (!fileName.endsWith(RrdUtils.getExtension())) {
            log().info("stripRrdExtension: File '" + fileName + "' does not end with the RRD extension '" + RrdUtils.getExtension() + "'.");
            return null;
        }
        return fileName.substring(0, fileName.lastIndexOf(RrdUtils.getExtension()));
    }
    
    /*
     * This directly access database to get OnmsSnmpInterface (snmpinterface table on database) data
     * for selected Interface ID.
     */
    private String getAttributeValue(File resourceDirectory, String resourceType, String attribute) {
        log().debug("Getting Value for " + resourceType + "::" + attribute + " from " + resourceDirectory);
        String value = null;
        // Interface ID or Resource ID from data path
        if (attribute.equals("ID")) {
            return resourceDirectory.getName();
        }
        try {
            if (resourceType.equals("if")) {
                String ifLabel = resourceDirectory.getName();
                int nodeId = Integer.parseInt(resourceDirectory.getParentFile().getName());
                Map<String,String> info = new HashMap<String,String>();
                populateIfDataMap(info, nodeId, ifLabel);
                value = info.get(attribute);
            } else {
                value = ResourceTypeUtils.getStringProperty(resourceDirectory, attribute);
            }
        } catch (Exception e) {
            log().warn("Can't get value for attribute " + attribute + ". " + e, e);
        }
        return value;
    }

    /**
     * Get the value to use for the ds-label from this threshold
     */
    private String getDataSourceLabel(File directory, SnmpThresholdNetworkInterface snmpIface, ThresholdEntity threshold) {
        String dsLabelValue = null;
        
        try {
            String key = threshold.getDatasourceLabel();
            dsLabelValue = (key == null ? null : ResourceTypeUtils.getStringProperty(directory, key));
        } catch (DataAccessException e) {
            if (log().isDebugEnabled()) {
                log().debug ("getDataSourceLabel: I/O exception when looking for strings.properties file for node id: " + snmpIface.getNodeId() + " looking here: " + directory + ": " + e, e);
            }
        }
        
        return (dsLabelValue == null ? "Unknown" : dsLabelValue);

    }


    /**
     * ifLabel will either be set to null for node level
     * datasource values
     * or to a specific interface in the case of an
     * interface level datasource.
     *
     * ifLabel has the following format:
     * <ifName|ifDescr>-<macAddr>
     * 
     * Call IfLabel.getInterfaceInfoFromLabel() utility
     * method to retrieve
     * data from the 'snmpInterfaces' table for this
     * interface. This method
     * will return a Map of database values keyed by field
     * name.
     */
    private void populateIfDataMap(Map<String, String> ifDataMap, int nodeId, String ifLabel) {
        Map<String, String> ifInfo = m_ifInfoGetter.getIfInfoForNodeAndLabel(nodeId, ifLabel);
        ifDataMap.putAll(ifInfo);
       // Adding ifLabel value to the map for potential use by the createEvent() method
        ifDataMap.put("iflabel", ifLabel);
    }

    protected static Map<String, Set<ThresholdEntity>> getAttributeMap(ThresholdResourceType resourceType) {
        Map<String, Set<ThresholdEntity>> thresholdMap = new HashMap<String, Set<ThresholdEntity>>();

        /*
         * Iterate over base interface threshold map and clone each
         * ThresholdEntity object and add it to the threshold map.
         * for this interface.
         */ 
        for (Set<ThresholdEntity> entitySet : resourceType.getThresholdMap().values()) {
        	for (ThresholdEntity entity : entitySet) {
        		if (!thresholdMap.containsKey(entity.getDataSourceExpression())) {
        			thresholdMap.put(entity.getDataSourceExpression(), new LinkedHashSet<ThresholdEntity>());
        		}
        		thresholdMap.get(entity.getDataSourceExpression()).add(entity.clone());
        	}
        }
        return thresholdMap;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    public ThresholdsDao getThresholdsDao() {
        return m_thresholdsDao;
    }

    public void setThresholdsDao(ThresholdsDao thresholdsDao) {
        m_thresholdsDao = thresholdsDao;
    }

    public IfInfoGetter getIfInfoGetter() {
        return m_ifInfoGetter;
    }

    public void setIfInfoGetter(IfInfoGetter ifInfoGetter) {
        m_ifInfoGetter = ifInfoGetter;
    }
}
