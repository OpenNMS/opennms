//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 22: Added threshold rearm events.  
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
//      http://www.blast.com/
//

package org.opennms.netmgt.threshd;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * <P>The LatencyThresholder class ...</P>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
final class LatencyThresholder
	implements ServiceThresholder
{
	/**
	 * SQL statement to retrieve interface's 'ipinterface' table information.
	 */
	private static final String SQL_GET_NODEID  	= "SELECT nodeid FROM ipinterface WHERE ipAddr=? AND ismanaged!='D'";
	
	/** 
	 * Default thresholding interval (in milliseconds).
	 * 
	 */
	private static final int 	DEFAULT_INTERVAL = 300000; // 300s or 5m
	
	/**
	 * Interface attribute key used to store the interface's node id
	 */
	static final String RRD_REPOSITORY_KEY	= "org.opennms.netmgt.collectd.LatencyThresholder.RrdRepository";
	
	/**
	 * Interface attribute key used to store configured thresholds
	 */
	static final String THRESHOLD_MAP_KEY	= "org.opennms.netmgt.collectd.LatencyThresholder.ThresholdMap";

	/**
	 * Interface attribute key used to store the interface's node id
	 */
	static final String NODE_ID_KEY	= "org.opennms.netmgt.collectd.SnmpThresholder.NodeId";
	
	/**
	 * Instance of org.opennms.netmgt.rrd.Interface singleton class
	 * which provides access to RRD functions via JNI.
	 */
	private org.opennms.netmgt.rrd.Interface m_rrdInterface;
	
	/**
	 * Specific service that this thresholder is responsible for 
	 * latency threshold checking.
	 */
	private String	m_svcName;
	
	/** 
	 * Local host name 
	 */
	private String m_host;
	
	/**
	 * <P>Returns the name of the service that the plug-in threshold checks.</P>
	 *
	 * @return The service that the plug-in collects.
	 */
	public String serviceName()
	{
		return m_svcName;
	}
	
	/**
	 * <P>Initialize the service thresholder.</P>
	 *
	 * @param parameters	Parameter map which contains (currently) a single
	 *  			entry, the name of the service which this thresholder
	 *                      is responsible for latency threshold checking keyed
	 *    			by the String "svcName"
	 *
	 * @exception RuntimeException	Thrown if an unrecoverable error occurs that prevents 
	 * the plug-in from functioning.
	 *
	 */
	public void initialize(Map parameters) 
	{
		// Log4j category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		// Service name
		//
		m_svcName = (String)parameters.get("svcName");
		if (log.isDebugEnabled())
			log.debug("initialize: latency thresholder for service '" + m_svcName + "'");
		
		// Get local host name (used when generating threshold events)
		//
		try
		{
			m_host = InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException e)
		{
			if(log.isEnabledFor(Priority.WARN))
				log.warn("initialize: Unable to resolve local host name.", e);
			m_host = "unresolved.host";
		}
		
		// Initialize jni RRD interface.
		//
		try
		{
			org.opennms.netmgt.rrd.Interface.init();
		}
		catch(SecurityException se)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("initialize: Failed to initialize JNI RRD interface", se);
			throw new UndeclaredThrowableException(se);
		}
		catch(UnsatisfiedLinkError ule)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("initialize: Failed to initialize JNI RRD interface", ule);
			throw new UndeclaredThrowableException(ule);
		}
		
		// Save local reference to singleton instance 
		//
		m_rrdInterface = org.opennms.netmgt.rrd.Interface.getInstance();
		
		if (log.isDebugEnabled())
			log.debug("initialize: successfully instantiated JNI interface to RRD...");
		
		return;
	}
	
	/** 
	 * Responsible for freeing up any resources held by the thresholder.
	 */
	public void release()
	{
		// Nothing to release...
	}
	
	/**
	 * Responsible for performing all necessary initialization for
	 * the specified interface in preparation for thresholding.
	 * 
	 * @param iface		Network interface to be prepped for thresholding.
	 * @param parameters 	Key/value pairs associated with the package 
	 *			to which the interface belongs..
	 * 
	 */
	public void initialize(NetworkInterface iface, Map parameters)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		// Get interface address from NetworkInterface
		//
		if (iface.getType() != NetworkInterface.TYPE_IPV4)
			throw new RuntimeException("Unsupported interface type, only TYPE_IPV4 currently supported");
		
		InetAddress ipAddr = (InetAddress)iface.getAddress();
		
		// Retrieve the name of the thresholding group associated
		// with this interface.
		String groupName = ParameterMap.getKeyedString(parameters, "thresholding-group", "default");
		
		// Get the threshold group's RRD repository path
		// 
		String repository = null;
		try
		{
			repository = ThresholdingConfigFactory.getInstance().getRrdRepository(groupName);
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException("Thresholding group '" + groupName + "' does not exist.");
		}
		
		// Add RRD repository as an attribute of the interface for retrieval
		// by the check() method.
		//
		iface.setAttribute(RRD_REPOSITORY_KEY, repository);
		
		// Get database connection in order to retrieve the nodeid and
		// ifIndex from the database for this interface.
		//
		java.sql.Connection dbConn = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();
		}
		catch (SQLException sqlE)
		{
			if(log.isEnabledFor(Priority.ERROR))
				log.error("initialize: Failed getting connection to the database.", sqlE);
			throw new UndeclaredThrowableException(sqlE);
		}
		
		// Use IP address to lookup the node id
		//
		// NOTE:  All database calls wrapped in try/finally block so we make
		// certain that the connection will be closed when we are 
		// finished.
		//
		int nodeId = -1;

		try
		{
			// Prepare & execute the SQL statement to get the 'nodeid', 
			// 'ifIndex' and 'isSnmpPrimary' fields from the ipInterface table.
			//
			PreparedStatement stmt = null;
			try
			{
				stmt = dbConn.prepareStatement(SQL_GET_NODEID);
				stmt.setString(1, ipAddr.getHostAddress());   // interface address
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
				{
					nodeId = rs.getInt(1);
					if(rs.wasNull())
						nodeId = -1;
				} 
				rs.close();
			}
			catch (SQLException sqle)
			{
				if (log.isDebugEnabled())
					log.debug("initialize: SQL exception!!", sqle);
				throw new RuntimeException("SQL exception while attempting to retrieve node id for interface " + ipAddr.getHostAddress()); 
			}
			finally
			{
				try
				{
					stmt.close();
				}
				catch (Exception e)
				{
					// Ignore
				}
			}
		
			if (log.isDebugEnabled())
				log.debug("initialize: db retrieval info: nodeid = " + nodeId + ", address = " + ipAddr.getHostAddress());
			
			if (nodeId == -1)
				throw new RuntimeException("Unable to retrieve node id for interface " + ipAddr.getHostAddress());
		}
		finally
		{
			// Done with the database so close the connection
			try
			{
				dbConn.close();
			}
			catch (SQLException sqle)
			{
				if(log.isEnabledFor(Priority.INFO))
					log.info("initialize: SQLException while closing database connection", sqle);
			}
		}
		
		// Add nodeId as an attribute of the interface for retrieval
		// by the check() method.
		//
		iface.setAttribute(NODE_ID_KEY, new Integer(nodeId));
		
		// Retrieve the collection of Threshold objects associated with
		// the defined thresholding group and build maps of
		// ThresholdEntity objects keyed by datasource name.  The 
		// datasource type of the threshold determines which
		// map the threshold entity is added to.
		//
		// Each ThresholdEntity can wrap one high Threshold and one low
		// Threshold castor-generated object for a single datasource.
		// If more than one high or more than one low threshold is defined 
		// for a single datasource a warning messages is generated.  Only 
		// the first threshold in such a scenario will be used for thresholding.
		//
		
		// Create empty map for storing threshold entities
		Map thresholdMap = new HashMap();
		
		try
		{
			Iterator iter = ThresholdingConfigFactory.getInstance().getThresholds(groupName).iterator();
			while (iter.hasNext())
			{
				Threshold thresh = (Threshold)iter.next();
				
				// See if map entry already exists for this datasource
				// If not, create a new one.
				boolean newEntity = false;
				ThresholdEntity thresholdEntity = null;
				
				// All latency thresholds are per interface so confirm that
				// the datasource type is set to "if"
				//
				if (!thresh.getDsType().equals("if"))
				{
					log.warn("initialize: invalid datasource type, latency thresholder only supports interface level datasources.");
					continue;  // continue with the next threshold...
				}
				
				// First attempt to lookup the entry in the map
				thresholdEntity = (ThresholdEntity)thresholdMap.get(thresh.getDsName());
				
				// Found entry?
				if (thresholdEntity == null)
				{
					// Nope, create a new one
					newEntity = true;
					thresholdEntity = new ThresholdEntity();
				}
				
				try
				{
					// Set high/low threshold
					if (thresh.getType().equals(ThresholdEntity.HIGH_THRESHOLD))
						thresholdEntity.setHighThreshold(thresh);
					else if (thresh.getType().equals(ThresholdEntity.LOW_THRESHOLD))
						thresholdEntity.setLowThreshold(thresh);
				}
				catch (IllegalStateException e)
				{
					log.warn("Encountered duplicate " + thresh.getType() + " for datasource " + thresh.getDsName(), e);
				}
				
				// Add new entity to the map
				if (newEntity)
				{
					thresholdMap.put(thresh.getDsName(), thresholdEntity);
				}
			}
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException("Thresholding group '" + groupName + "' does not exist.");
		}
		
		// Add threshold maps as attributes for retrieval by the check() method.
		//
		iface.setAttribute(THRESHOLD_MAP_KEY, thresholdMap);
		
		// Debug
		//
		if (log.isDebugEnabled())
		{
			log.debug("initialize: dumping interface thresholds defined for " + ipAddr.getHostAddress() + "/" + groupName + ":");
			Iterator iter = thresholdMap.values().iterator();
			while (iter.hasNext())
				log.debug((ThresholdEntity)iter.next());
		}
	
		if (log.isDebugEnabled())
			log.debug("initialize: initialization completed for " + ipAddr.getHostAddress());
		return;
	}
	
	/**
	 * Responsible for releasing any resources associated with the
	 * specified interface.
	 * 
	 * @param iface	Network interface to be released.
	 */
	public void release(NetworkInterface iface)
	{
		// Nothing to release...
	}
	
	/**
	 * Perform threshold checking.
	 * 
	 * @param iface 	Network interface to be data collected.
	 * @param eproxy	Eventy proxy for sending events.
	 * @param parameters	Key/value pairs from the package to which 
	 * 			the interface belongs.
	 */
	public int check(NetworkInterface iface, EventProxy eproxy, Map parameters)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		InetAddress ipAddr = (InetAddress)iface.getAddress();
		int thresholdingStatus = THRESHOLDING_UNKNOWN;
		
		// Get configuration parameters
		//
		String groupName = ParameterMap.getKeyedString(parameters, "thresholding-group", "default");
		int interval = ParameterMap.getKeyedInteger(parameters, "interval", DEFAULT_INTERVAL);
		
		// NodeId attribute
		int nodeId = -1;
		Integer tmp = (Integer)iface.getAttribute(NODE_ID_KEY);
		if (tmp != null)
			nodeId = tmp.intValue();	
		if (nodeId == -1)
		{
			log.error("Threshold checking failed for " + m_svcName + "/" + ipAddr.getHostAddress() + ", missing nodeId.");
			return THRESHOLDING_FAILED;
		}
		
		if (log.isDebugEnabled())
			log.debug("check: service= " + m_svcName + " interface= " + ipAddr.getHostAddress() + 
				" nodeId= " + nodeId + " thresholding-group=" + groupName + " interval=" + interval + "ms");
		
		// RRD Repository attribute
		//
		String repository = (String)iface.getAttribute(RRD_REPOSITORY_KEY);
		if (log.isDebugEnabled())
			log.debug("check: rrd repository=" + repository);
		
		// ThresholdEntity map attributes
		//
		Map thresholdMap = (Map)iface.getAttribute(THRESHOLD_MAP_KEY);
		
		// Get File object representing the '/opt/OpenNMS/share/rrd/<svc_name>/<ipAddress>/' directory
		File latencyDir = new File(repository + File.separator + ipAddr.getHostAddress());
		if (!latencyDir.exists())
		{
			log.error("Latency directory for " + m_svcName + "/" + ipAddr.getHostAddress() + " does not exist.");
			log.error("Threshold checking failed for " + ipAddr.getHostAddress());
			return THRESHOLDING_FAILED;
		}
		else if (!RrdFileConstants.isValidRRDLatencyDir(latencyDir))
		{
			log.error("Latency directory for " + m_svcName + "/" + ipAddr.getHostAddress() + " is not a valid RRD latency directory.");
			log.error("Threshold checking failed for " + ipAddr.getHostAddress());
			return THRESHOLDING_FAILED;
		}
		
		// Create empty Events object to hold any threshold
		// events generated during the thresholding check...
		Events events = new Events();
		
		try
		{
			checkRrdDir(latencyDir, 
					nodeId,
					ipAddr,
					interval,
					new Date(),  // time stamp for outgoing events
					thresholdMap,
					events);
		}
		catch (IllegalArgumentException e)
		{
			log.error("check: Threshold checking failed for " + m_svcName + "/" + ipAddr.getHostAddress(), e);
			return THRESHOLDING_FAILED;
		}
		
		// Send created events
		//
		if (events.getEventCount() > 0)
		{
			try
			{
				Log eventLog = new Log();
				eventLog.setEvents(events);
				eproxy.send(eventLog);
			}
			catch(RuntimeException e)
			{
				log.error("check: Failed sending threshold events via event proxy...", e);
				return THRESHOLDING_FAILED;
			}
		}
		
		// return the status of the threshold check
		//
		return THRESHOLDING_SUCCEEDED;
	}
	
	/**
	 * Performs threshold checking on an directory which contains one or
	 * more RRD files containing latency/response time information.  
	 * ThresholdEntity objects are stored for performing threshold
	 * checking.  
	 * 
	 * @param directory		RRD repository directory
	 * @param nodeId		Node identifier of interface being checked
	 * @param ipAddr		IP address of the interface being checked
	 * @param interval		Configured thresholding interval
	 * @param date 			Source for timestamp to be used for all 
	 * 					generated events
	 * @param thresholdMap		Map of configured interface level ThresholdEntity 
	 * 					objects	keyed by datasource name.
	  * @param events		Castor events object containing any
	 * 					events to be generated as a result
	 * 					of threshold checking.
	 * 
	 * @throws IllegalArgumentException if path parameter is not a directory.
	 */
	private void checkRrdDir(File directory, 
				int nodeId,
				InetAddress ipAddr,
				int interval,
				Date date,
				Map thresholdMap,
				Events events)
		throws IllegalArgumentException
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		// Sanity Check
		if (directory == null ||
			ipAddr == null ||
			date == null 	||
			thresholdMap == null ||
			events == null)
		{
			throw new IllegalArgumentException("Null parameters not permitted.");
		}
		
		if (log.isDebugEnabled())
			log.debug("checkPerformanceDir: threshold checking dir: " + directory.getAbsolutePath());
		
		// Iterate over directory contents and threshold
		// check any RRD files which represent datasources
		// in the threshold maps.
		//
		File[] files = directory.listFiles(RrdFileConstants.RRD_FILENAME_FILTER);
		
		if (files == null)
			return;
		
		for (int i=0; i<files.length; i++)
		{
			// File name has format: <datsource>.rrd
			// Must strip off ".rrd" portion.
			String filename = files[i].getName();
			String datasource = filename.substring(0, filename.indexOf(".rrd"));
			
			// Lookup the ThresholdEntity object corresponding
			// to this datasource.  
			//
			ThresholdEntity threshold = (ThresholdEntity)thresholdMap.get(datasource);
			if (threshold != null)
			{
				// Use RRD JNI interface to "fetch" value of the
				// datasource from the RRD file
				//
				Double dsValue = null;
				try
				{
					dsValue = fetch(files[i].getAbsolutePath(), interval);
				}
				catch (NumberFormatException nfe)
				{
					log.warn("Unable to convert retrieved value for datasource '" + datasource + "' to a double, skipping evaluation.");
				}
				
				if (dsValue != null && !dsValue.isNaN())
				{
					// Evaluate the threshold
					// 
					// ThresholdEntity.evaluate() returns an integer value
					// which indicates which threshold values were
					// triggered and require an event to be generated (if any).
					// 
					int result = threshold.evaluate(dsValue.doubleValue());
					if (result != ThresholdEntity.NONE_TRIGGERED)
					{
						if (result == ThresholdEntity.HIGH_AND_LOW_TRIGGERED ||
							result == ThresholdEntity.HIGH_TRIGGERED)
						{
							events.addEvent(createEvent(nodeId,
										ipAddr,
										dsValue.doubleValue(),
										threshold.getHighThreshold(),
										EventConstants.HIGH_THRESHOLD_EVENT_UEI, 
										date));
						}
						
						if (result == ThresholdEntity.HIGH_AND_LOW_TRIGGERED ||
							result == ThresholdEntity.LOW_TRIGGERED)
						{
							events.addEvent(createEvent(nodeId,
										ipAddr,
										dsValue.doubleValue(),
										threshold.getLowThreshold(),
										EventConstants.LOW_THRESHOLD_EVENT_UEI, 
										date));
						}

						if (result == ThresholdEntity.HIGH_AND_LOW_REARMED ||
							result == ThresholdEntity.HIGH_REARMED)
						{
							events.addEvent(createEvent(nodeId,
										ipAddr,
										dsValue.doubleValue(),
										threshold.getHighThreshold(),
										EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, 
										date));
						}
						
						if (result == ThresholdEntity.HIGH_AND_LOW_REARMED ||
							result == ThresholdEntity.LOW_REARMED)
						{
							events.addEvent(createEvent(nodeId,
										ipAddr,
										dsValue.doubleValue(),
										threshold.getLowThreshold(),
										EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI, 
										date));
						}
					}
				}
			}
		}
	}
	
	/**
	 * This method uses the RRD JNI interface to issue an RRD fetch command
	 * to retrieve the last value of the datasource stored in the 
	 * specified RRD file.  The retrieved value returned to the caller.
	 * 
	 * NOTE:  This method assumes that each RRD file contains a single datasource.
	 *
	 * @param rrdFile	RRD file from which to fetch the data.
	 * @param interval	Thresholding interval (should equal RRD step size)
	 * 
	 * @return Retrived datasource value as a java.lang.Double
	 * 
	 * @throws NumberFormatException if the retrieved value fails to 
	 * 	convert to a double
	 */
	private Double fetch(String rrdFile, int interval)
		throws NumberFormatException
	{
		// Log4j category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		// Generate rrd_fetch() command through jrrd JNI interface in order to retrieve 
		// LAST pdp for the datasource stored in the specified RRD file
		//
		// String array returned from launch() native method format:
		//	String[0] - If success is null, otherwise contains reason for failure
		//	String[1] - All data source names contained in the RRD (space delimited)
		//	String[2]...String[n] - RRD fetch data in the following format:
		//		<timestamp> <value1> <value2> ... <valueX> where X is
		// 			the total number of data sources
		//
		// NOTE:  Specifying start time of 'now-<interval>' and
		//        end time of 'now-<interval>' where <interval> is the
		//	  configured thresholding interval (and should be the
		// 	  same as the RRD step size) in order to guarantee that
		//  	  we don't get a 'NaN' value from the fetch command.  This 
		// 	  is necessary because the collection is being done by collectd
		// 	  and there is nothing keeping us in sync.
		// 
		//	  interval argument is in milliseconds so must convert to seconds
		//
		String fetchCmd = "fetch " + rrdFile + " AVERAGE -s now-" + interval/1000 + " -e now-" + interval/1000;
	
		if (log.isDebugEnabled()) 
			log.debug("fetch: Issuing RRD command: " + fetchCmd);
	
		String[] fetchStrings = fetchStrings = m_rrdInterface.launch(fetchCmd);
	
		// Sanity check the returned string array
		if (fetchStrings == null)
		{
			if(log.isEnabledFor(Priority.ERROR))
			{
				log.error("fetch: Unexpected error issuing RRD 'fetch' command, no error text available.");
			}
			return null;
		}
	
		// Check error string at index 0, will be null if 'fetch' was successful
		if (fetchStrings[0] != null)
		{
			if(log.isEnabledFor(Priority.ERROR))
			{
				log.error("fetch: RRD database 'fetch' failed, reason: " + fetchStrings[0]);
			}
			return null;
		}	
		
		// Sanity check
		if (fetchStrings[1] == null || fetchStrings[2] == null)
		{
			if(log.isEnabledFor(Priority.ERROR))
			{
				log.error("fetch: RRD database 'fetch' failed, no data retrieved.");
			}
			return null;
		}	
		
		// String at index 1 contains the RRDs datasource names
		//
		String dsName = fetchStrings[1].trim();
		
		// String at index 2 contains fetched values for the current time
		// Convert value string into a Double
		//
		Double dsValue = null;
		if (fetchStrings[2].trim().equalsIgnoreCase("nan"))
		{
			dsValue = new Double(Double.NaN);
		} 
		else
		{
			try
			{
				dsValue = new Double(fetchStrings[2].trim());
			}
			catch (NumberFormatException nfe)
			{
				if(log.isEnabledFor(Priority.WARN))
					log.warn("fetch: Unable to convert fetched value (" + fetchStrings[2].trim() + ") to Double for data source " + dsName);
				throw nfe;
			}
		}
				
		if (log.isDebugEnabled()) 
			log.debug("fetch: fetch successful: " + dsName + "= " + dsValue);
					
		return dsValue;
	}
	
	/**
	 * Creates a new threshold event from the specified parms.
	 *
	 * @param nodeId	Node identifier of the affected interface
	 * @param ipAddr	IP address of the affected interface
	 * @param dsValue	Data source value which triggered the threshold event
	 * @param threshold	Configured threshold
	 * @param uei		Event identifier 
	 * @param data		source of event's timestamp
	 * 
	 * @return new threshold event to be sent to Eventd
	 */
	private Event createEvent(int 		nodeId,
				InetAddress	ipAddr,
				double		dsValue,
				Threshold	threshold,
				String 		uei,
				java.util.Date 	date)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		if (threshold == null)
			throw new IllegalArgumentException("threshold cannot be null.");
			
		if (log.isDebugEnabled()) 
		{
			log.debug("createEvent: ds=" + threshold.getDsName() + " uei=" + uei);
		}
			
		// create the event to be sent
		Event newEvent = new Event();
		newEvent.setUei(uei);
		newEvent.setNodeid((long)nodeId);
		newEvent.setInterface(ipAddr.getHostAddress());
		newEvent.setService(this.serviceName());
		
		// set the source of the event to the datasource name
		newEvent.setSource("OpenNMS.Threshd:" + threshold.getDsName());
		
		// Set event host
		//
		try
		{
			newEvent.setHost(InetAddress.getLocalHost().getHostName());
		}
		catch(UnknownHostException uhE)
		{
			newEvent.setHost("unresolved.host");
			log.warn("Failed to resolve local hostname", uhE);
		}
		
		// Set event time
		newEvent.setTime(EventConstants.formatToString(date));
		
		// Add appropriate parms
		//
		Parms eventParms = new Parms();
		Parm eventParm = null;
		Value parmValue = null;
		
		// Add datasource name
		eventParm = new Parm();
		eventParm.setParmName("ds");
		parmValue = new Value();
		parmValue.setContent(threshold.getDsName());
		eventParm.setValue(parmValue);
		eventParms.addParm(eventParm);
		
		// Add last known value of the datasource
		// fetched from its RRD file
		//
		eventParm  = new Parm();
		eventParm.setParmName("value");
		parmValue = new Value();
		parmValue.setContent(Double.toString(dsValue));
		eventParm.setValue(parmValue);
		eventParms.addParm(eventParm);
		
		// Add configured threshold value
		eventParm = new Parm();
		eventParm.setParmName("threshold");
		parmValue = new Value();
		parmValue.setContent(Double.toString(threshold.getValue()));
		eventParm.setValue(parmValue);
		eventParms.addParm(eventParm);
		
		// Add configured trigger value
		eventParm = new Parm();
		eventParm.setParmName("trigger");
		parmValue = new Value();
		parmValue.setContent(Integer.toString(threshold.getTrigger()));
		eventParm.setValue(parmValue);
		eventParms.addParm(eventParm);

		// Add configured rearm value
		eventParm = new Parm();
		eventParm.setParmName("rearm");
		parmValue = new Value();
		parmValue.setContent(Double.toString(threshold.getRearm()));
		eventParm.setValue(parmValue);
		eventParms.addParm(eventParm);
		
		// Add Parms to the event
		newEvent.setParms(eventParms);
		
		return newEvent;
	}
}
