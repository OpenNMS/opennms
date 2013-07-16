/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import java.io.File;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.dao.support.RrdFileConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>LatencyInterface class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class LatencyInterface {

        
        private static final Logger LOG = LoggerFactory.getLogger(LatencyInterface.class);
        
        private NetworkInterface<InetAddress> m_iface;
	private String m_serviceName;

	/**
	 * <p>Constructor for LatencyInterface.</p>
	 *
	 * @param iface a {@link org.opennms.netmgt.poller.NetworkInterface} object.
	 * @param serviceName a {@link java.lang.String} object.
	 */
	public LatencyInterface(NetworkInterface<InetAddress> iface, String serviceName) {
		m_iface = iface;
		m_serviceName = serviceName;
	}

	/**
	 * <p>getNetworkInterface</p>
	 *
	 * @return a {@link org.opennms.netmgt.poller.NetworkInterface} object.
	 */
	public NetworkInterface<InetAddress> getNetworkInterface() {
		return m_iface;
	}

	Map<String, ThresholdEntity> getThresholdMap() {
	    NetworkInterface<InetAddress> iface = getNetworkInterface();
		// ThresholdEntity map attributes
	    //
	    Map<String, ThresholdEntity> thresholdMap = iface.getAttribute(LatencyThresholder.THRESHOLD_MAP_KEY);
	    return Collections.unmodifiableMap(thresholdMap);
	}

	InetAddress getInetAddress() {
	    return getNetworkInterface().getAddress();
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
	    NetworkInterface<InetAddress> iface = getNetworkInterface();
	
		int nodeId = -1;
	    Integer tmp = iface.getAttribute(LatencyThresholder.NODE_ID_KEY);
	    if (tmp != null)
	        nodeId = tmp.intValue();
	    if (nodeId == -1) {
	        throw new ThresholdingException("Threshold checking failed for " + getServiceName() + "/" + getHostAddress() + ", missing nodeId.", LatencyThresholder.THRESHOLDING_FAILED);
	    }
	    return nodeId;
	}

	/**
	 * <p>getHostName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getHostAddress() {
		return InetAddressUtils.str(getInetAddress());
	}

	File getLatencyDir() throws ThresholdingException {
		String repository = getNetworkInterface().getAttribute(LatencyThresholder.RRD_REPOSITORY_KEY);
	    LOG.debug("check: rrd repository=", repository);
	    // Get File object representing the
	    // '/opt/OpenNMS/share/rrd/<svc_name>/<ipAddress>/' directory
	    File latencyDir = new File(repository + File.separator + getHostAddress());
	    if (!latencyDir.exists()) {
	        throw new ThresholdingException("Latency directory for " + getServiceName() + "/" + getHostAddress() + " does not exist. Threshold checking failed for " + getHostAddress(), LatencyThresholder.THRESHOLDING_FAILED);
	    } else if (!RrdFileConstants.isValidRRDLatencyDir(latencyDir)) {
	        throw new ThresholdingException("Latency directory for " + getServiceName() + "/" + getHostAddress() + " is not a valid RRD latency directory. Threshold checking failed for " + getHostAddress(), LatencyThresholder.THRESHOLDING_FAILED);
	    }
	    return latencyDir;
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
		
		if (threshold == null)
	        throw new IllegalArgumentException("threshold cannot be null.");
	
	    LOG.debug("createEvent: ds={} uei={}", threshold.getDsName(), uei);
	
	    // create the event to be sent
	    EventBuilder bldr = new EventBuilder(uei, "OpenNMS.Threshd:" + threshold.getDsName(), date);
	    bldr.setNodeid(nodeId);
	    bldr.setInterface(ipAddr);
	    bldr.setService(getServiceName());
	
	
	    // Set event host
        bldr.setHost(InetAddressUtils.getLocalHostName());
	    
	    bldr.addParam("ds", threshold.getDsName());
	    bldr.addParam("value", dsValue);
	    bldr.addParam("threshold", threshold.getValue());
	    bldr.addParam("trigger", threshold.getTrigger());
	    bldr.addParam("rearm", threshold.getRearm());
	
	    return bldr.getEvent();
	}
}
