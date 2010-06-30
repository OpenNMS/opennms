//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
import java.util.Date;
import java.util.Map;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.dao.support.RrdFileConstants;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * <p>LatencyInterface class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class LatencyInterface {

	private NetworkInterface m_iface;
	private String m_serviceName;

	/**
	 * <p>Constructor for LatencyInterface.</p>
	 *
	 * @param iface a {@link org.opennms.netmgt.poller.NetworkInterface} object.
	 * @param serviceName a {@link java.lang.String} object.
	 */
	public LatencyInterface(NetworkInterface iface, String serviceName) {
		m_iface = iface;
		m_serviceName = serviceName;
	}

	/**
	 * <p>getNetworkInterface</p>
	 *
	 * @return a {@link org.opennms.netmgt.poller.NetworkInterface} object.
	 */
	public NetworkInterface getNetworkInterface() {
		return m_iface;
	}

	Map getThresholdMap() {
	    NetworkInterface iface = getNetworkInterface();
		// ThresholdEntity map attributes
	    //
	    Map thresholdMap = (Map) iface.getAttribute(LatencyThresholder.THRESHOLD_MAP_KEY);
	    return thresholdMap;
	}

	InetAddress getInetAddress() {
	    NetworkInterface iface = getNetworkInterface();
	
		InetAddress ipAddr = (InetAddress) iface.getAddress();
	    return ipAddr;
	}

	/**
	 * <p>getServiceName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getServiceName() {
		// TODO Auto-generated method stub
		return m_serviceName;
	}

	int getNodeId() throws ThresholdingException {
	    NetworkInterface iface = getNetworkInterface();
	
		int nodeId = -1;
	    Integer tmp = (Integer) iface.getAttribute(LatencyThresholder.NODE_ID_KEY);
	    if (tmp != null)
	        nodeId = tmp.intValue();
	    if (nodeId == -1) {
	        throw new ThresholdingException("Threshold checking failed for " + getServiceName() + "/" + getInetAddress().getHostAddress() + ", missing nodeId.", LatencyThresholder.THRESHOLDING_FAILED);
	    }
	    return nodeId;
	}

	/**
	 * <p>getHostName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getHostName() {
		return getInetAddress().getHostAddress();
	}

	File getLatencyDir() throws ThresholdingException {
		String repository = (String) getNetworkInterface().getAttribute(LatencyThresholder.RRD_REPOSITORY_KEY);
	    if (log().isDebugEnabled())
	        log().debug("check: rrd repository=" + repository);
	    // Get File object representing the
	    // '/opt/OpenNMS/share/rrd/<svc_name>/<ipAddress>/' directory
	    File latencyDir = new File(repository + File.separator + getHostName());
	    if (!latencyDir.exists()) {
	        throw new ThresholdingException("Latency directory for " + getServiceName() + "/" + getHostName() + " does not exist. Threshold checking failed for " + getHostName(), LatencyThresholder.THRESHOLDING_FAILED);
	    } else if (!RrdFileConstants.isValidRRDLatencyDir(latencyDir)) {
	        throw new ThresholdingException("Latency directory for " + getServiceName() + "/" + getHostName() + " is not a valid RRD latency directory. Threshold checking failed for " + getHostName(), LatencyThresholder.THRESHOLDING_FAILED);
	    }
	    return latencyDir;
	}
	
	private final ThreadCategory log() {
		return ThreadCategory.getInstance(LatencyInterface.class);
	}

	/**
	 * Creates a new threshold event from the specified parms.
	 * @param dsValue
	 *            Data source value which triggered the threshold event
	 * @param threshold
	 *            Configured threshold
	 * @param uei
	 *            Event identifier
	 * @param date
	 *            source of event's timestamp
	 * @param m_nodeId
	 *            Node identifier of the affected interface
	 * @param ipAddr
	 *            IP address of the affected interface
	 * @param thresholder TODO
	 * @return new threshold event to be sent to Eventd
	 * @throws ThresholdingException 
	 */
	Event createEvent(double dsValue, Threshold threshold, String uei, Date date) throws ThresholdingException {
		int nodeId = getNodeId();
		InetAddress ipAddr = getInetAddress();
		
		ThreadCategory log = ThreadCategory.getInstance(LatencyInterface.class);
	
	    if (threshold == null)
	        throw new IllegalArgumentException("threshold cannot be null.");
	
	    if (log.isDebugEnabled()) {
	        log.debug("createEvent: ds=" + threshold.getDsName() + " uei=" + uei);
	    }
	
	    // create the event to be sent
	    Event newEvent = new Event();
	    newEvent.setUei(uei);
	    newEvent.setNodeid((long) nodeId);
	    newEvent.setInterface(ipAddr.getHostAddress());
	    newEvent.setService(getServiceName());
	
	    // set the source of the event to the datasource name
	    newEvent.setSource("OpenNMS.Threshd:" + threshold.getDsName());
	
	    // Set event host
	    //
	    try {
	        newEvent.setHost(InetAddress.getLocalHost().getHostName());
	    } catch (UnknownHostException uhE) {
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
	    eventParm = new Parm();
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
