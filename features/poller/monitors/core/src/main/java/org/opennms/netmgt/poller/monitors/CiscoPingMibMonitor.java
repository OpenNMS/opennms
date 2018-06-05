/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the ICMP service on remote interfaces, using the CISCO-PING-MIB
 * as a proxy of sorts. The class implements the ServiceMonitor interface that
 * allows it to be used along with other plug-ins by the service poller framework.
 * 
 * Derived from the SnmpMonitor class.  See that class' docs for authorship history.
 * </P>
 *
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */

// This monitor uses SNMP and therefore relies on the SNMP peer factory,
// so it is not distributable
@Distributable(DistributionContext.DAEMON)
public class CiscoPingMibMonitor extends SnmpMonitorStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(CiscoPingMibMonitor.class);
	
    @SuppressWarnings("unused")
	private static final class CiscoPingEntry {
		private int m_ciscoPingSerialNumber;
		private int m_ciscoPingProtocol;
		private InetAddress m_ciscoPingAddress;
		private int m_ciscoPingPacketCount;
		private int m_ciscoPingPacketSize;
		private int m_ciscoPingPacketTimeout;
		private int m_ciscoPingPacketDelay;
		private String m_ciscoPingEntryOwner;
		private String m_ciscoPingVrfName;
		private int m_ciscoPingEntryStatus;

		public int getCiscoPingSerialNumber() {
			return m_ciscoPingSerialNumber;
		}
		public void setCiscoPingSerialNumber(int ciscoPingSerialNumber) {
			m_ciscoPingSerialNumber = ciscoPingSerialNumber;
		}
        public int getCiscoPingProtocol() {
			return m_ciscoPingProtocol;
		}
		public void setCiscoPingProtocol(int ciscoPingProtocol) {
			m_ciscoPingProtocol = ciscoPingProtocol;
		}
		public InetAddress getCiscoPingAddress() {
			return m_ciscoPingAddress;
		}
		public byte[] getCiscoPingAddressBytes() {
			return m_ciscoPingAddress.getAddress();
		}
		public void setCiscoPingAddress(InetAddress ciscoPingAddress) {
			m_ciscoPingAddress = ciscoPingAddress;
		}
		public int getCiscoPingPacketCount() {
			return m_ciscoPingPacketCount;
		}
		public void setCiscoPingPacketCount(int ciscoPingPacketCount) {
			m_ciscoPingPacketCount = ciscoPingPacketCount;
		}
		public int getCiscoPingPacketSize() {
			return m_ciscoPingPacketSize;
		}
		public void setCiscoPingPacketSize(int ciscoPingPacketSize) {
			m_ciscoPingPacketSize = ciscoPingPacketSize;
		}
		public int getCiscoPingPacketTimeout() {
			return m_ciscoPingPacketTimeout;
		}
		public void setCiscoPingPacketTimeout(int ciscoPingPacketTimeout) {
			m_ciscoPingPacketTimeout = ciscoPingPacketTimeout;
		}
		public int getCiscoPingPacketDelay() {
			return m_ciscoPingPacketDelay;
		}
		public void setCiscoPingPacketDelay(int ciscoPingPacketDelay) {
			m_ciscoPingPacketDelay = ciscoPingPacketDelay;
		}
		public String getCiscoPingEntryOwner() {
			return m_ciscoPingEntryOwner;
		}
		public void setCiscoPingEntryOwner(String ciscoPingEntryOwner) {
			m_ciscoPingEntryOwner = ciscoPingEntryOwner;
		}
		public String getCiscoPingVrfName() {
			return m_ciscoPingVrfName;
		}
		public void setCiscoPingVrfName(String ciscoPingVrfName) {
			m_ciscoPingVrfName = ciscoPingVrfName;
		}
		public int getCiscoPingEntryStatus() {
			return m_ciscoPingEntryStatus;
		}
		public void setCiscoPingEntryStatus(int ciscoPingEntryStatus) {
			m_ciscoPingEntryStatus= ciscoPingEntryStatus;
		}
		public int calculateMinInitialWait() {
			return m_ciscoPingPacketCount * (m_ciscoPingPacketTimeout + m_ciscoPingPacketDelay);
		}
		
		public SnmpObjId[] generateCreateOids() {
			SnmpObjId[] oids = {
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_PROTOCOL + "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_ADDRESS + "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_PACKET_COUNT+ "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_PACKET_SIZE + "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_PACKET_TIMEOUT + "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_DELAY + "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_ENTRY_OWNER + "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_ENTRY_STATUS + "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_VRF_NAME + "." + m_ciscoPingSerialNumber)
			};
			return oids;
		}
		
		public SnmpValue[] generateCreateValues() {
			SnmpValueFactory vf = SnmpUtils.getValueFactory();
			SnmpValue[] values = {
					vf.getInt32(m_ciscoPingProtocol),
					vf.getOctetString(m_ciscoPingAddress.getAddress()),
					vf.getInt32(m_ciscoPingPacketCount),
					vf.getInt32(m_ciscoPingPacketSize),
					vf.getInt32(m_ciscoPingPacketTimeout),
					vf.getInt32(m_ciscoPingPacketDelay),
					vf.getOctetString(m_ciscoPingEntryOwner.getBytes()),
					vf.getInt32(m_ciscoPingEntryStatus),
					vf.getOctetString(m_ciscoPingVrfName.getBytes())
			};
			return values;
		}
		
		public SnmpObjId[] generateRowStatusOids() {
			SnmpObjId[] oids = {
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_ENTRY_STATUS + "." + m_ciscoPingSerialNumber)
			};
			return oids;
		}
		
		public SnmpValue[] generateRowStatusValues() {
			SnmpValueFactory vf = SnmpUtils.getValueFactory();
			SnmpValue[] values = {
					vf.getInt32(m_ciscoPingEntryStatus)
			};
			return values;
		}
		
		public SnmpObjId[] generateResultsOids() {
			SnmpObjId[] oids = {
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_SENT_PACKETS + "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_RECEIVED_PACKETS + "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_MIN_RTT + "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_AVG_RTT + "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_MAX_RTT + "." + m_ciscoPingSerialNumber),
					SnmpObjId.get(PING_ENTRY_OID + "." + PING_COMPLETED + "." + m_ciscoPingSerialNumber)
			};
			return oids;
		}
		
                @Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("CiscoPingEntry: [ciscoPingSerialNumber=");
			sb.append(m_ciscoPingSerialNumber).append(",");
			sb.append("ciscoPingProtocol=").append(m_ciscoPingProtocol).append(",");
			sb.append("ciscoPingAddress=").append(m_ciscoPingAddress).append(",");
			sb.append("ciscoPingPacketCount=").append(m_ciscoPingPacketCount).append(",");
			sb.append("ciscoPingPacketSize=").append(m_ciscoPingPacketSize).append(",");
			sb.append("ciscoPingPacketTimeout=").append(m_ciscoPingPacketTimeout).append(",");
			sb.append("ciscoPingPacketDelay=").append(m_ciscoPingPacketDelay).append(",");
			sb.append("ciscoPingEntryOwner=").append(m_ciscoPingEntryOwner).append(",");
			sb.append("ciscoPingVrfName=").append(m_ciscoPingVrfName);
			sb.append("]");
			
			return sb.toString();
		}
	}

    /**
     * Default timeout, in milliseconds, for the SNMP operations underlying this poll
     */
    private static final int DEFAULT_TIMEOUT = 1800;
    
    /**
     * Default retry count for the SNMP operations underlying this poll
     */
    private static final int DEFAULT_RETRY = 1;

    /**
     * Identifier of the ciscoPingEntry object
     */
    private static final String PING_ENTRY_OID = ".1.3.6.1.4.1.9.9.16.1.1.1"; // Enterprises / cisco / ciscoMgmt /
                                                                                // ciscoPingMIB / ciscoPingMIBObjects /
                                                                                // ciscoPingTable / ciscoPingEntry

    @SuppressWarnings("unused")
    private static final String PING_SERIAL = "1";
    private static final String PING_PROTOCOL = "2";
    private static final String PING_ADDRESS = "3";
    private static final String PING_PACKET_COUNT = "4";
    private static final String PING_PACKET_SIZE = "5";
    private static final String PING_PACKET_TIMEOUT = "6";
    private static final String PING_DELAY = "7";
    private static final String PING_SENT_PACKETS = "9";
    private static final String PING_RECEIVED_PACKETS = "10";
    private static final String PING_MIN_RTT = "11";
    private static final String PING_AVG_RTT = "12";
    private static final String PING_MAX_RTT = "13";
    private static final String PING_COMPLETED = "14";
    private static final String PING_ENTRY_OWNER = "15";
    private static final String PING_ENTRY_STATUS = "16";
    private static final String PING_VRF_NAME = "17";
    
    @SuppressWarnings("unused")
    private static final int ROWSTATUS_ACTIVE = 1;
    @SuppressWarnings("unused")
    private static final int ROWSTATUS_NOT_IN_SERVICE = 2;
    @SuppressWarnings("unused")
    private static final int ROWSTATUS_NOT_READY = 3;
    private static final int ROWSTATUS_CREATE_AND_GO = 4;
    @SuppressWarnings("unused")
    private static final int ROWSTATUS_CREATE_WAIT = 5;
    private static final int ROWSTATUS_DESTROY = 6;
    
    private static final int PING_PROTOCOL_IPV4 = 1;
    private static final int PING_PROTOCOL_IPV6 = 20;

    /* Number of ping packets that IOS should send */
    private static final String PARM_PACKET_COUNT = "packet-count";
    private static final int PARM_PACKET_COUNT_DEFAULT = 5;
    
    /* Size in bytes of each ping packet that IOS should send */
    private static final String PARM_PACKET_SIZE = "packet-size";
    private static final int PARM_PACKET_SIZE_DEFAULT = 100;
    
    /* Timeout in milliseconds for each ping packet that IOS will send */
    private static final String PARM_PACKET_TIMEOUT = "packet-timeout";
    private static final int PARM_PACKET_TIMEOUT_DEFAULT = 2000;
    
    /* Delay in milliseconds among the ping packets that IOS will send */
    private static final String PARM_PACKET_DELAY = "packet-delay";
    private static final int PARM_PACKET_DELAY_DEFAULT = 0;
    
    /* A string identifying which management application "owns" this entry in the ciscoPingTable */
    private static final String PARM_ENTRY_OWNER = "entry-owner";
    private static final String PARM_ENTRY_OWNER_DEFAULT = "OpenNMS CiscoPingMibMonitor";
    
    /* A string indicating the VPN name in which IOS will perform the ping. Normally blank. */ 
    private static final String PARM_VRF_NAME = "vrf-name";
    private static final String PARM_VRF_NAME_DEFAULT = "";
    
    /* Maximum age (in ms, but with 1s accuracy) of a ciscoPingEntry that will not be deleted.
     * Set to zero to disable cleanup of the ciscoPingTable.  Best to leave set to default.
     */
    @SuppressWarnings("unused")
    private static final String PARM_CLEANUP_INTERVAL = "cleanup-interval";
    @SuppressWarnings("unused")
    private static final int PARM_CLEANUP_INTERVAL_DEFAULT = 86400000;
    
    /* The node ID of the node that will act as our IOS ping proxy */
    private static final String PARM_PROXY_NODE_ID = "proxy-node-id";
    
    /* The foreign-source name of the node that will act as our IOS ping proxy */
    private static final String PARM_PROXY_FOREIGN_SOURCE = "proxy-node-foreign-source";
    
    /* The foreign-id of the node that will act as our IOS ping proxy */
    private static final String PARM_PROXY_FOREIGN_ID = "proxy-node-foreign-id";
    
    /* The IP address of the interface to use as our IOS ping proxy */
    private static final String PARM_PROXY_IP_ADDR = "proxy-ip-addr";
    
    /* The IP address of the interface we ultimately want to ping */
    private static final String PARM_TARGET_IP_ADDR = "target-ip-addr";
    
    /* The percent of proxied pings that must succeed for the service to be up */
    private static final String PARM_SUCCESS_PERCENT = "success-percent";
    private static final int PARM_SUCCESS_PERCENT_DEFAULT = 100;

    private final Supplier<NodeDao> nodeDao = Suppliers.memoize(() -> BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class));

    @Override
    public Map<String, Object> getRuntimeAttributes(MonitoredService svc, Map<String, Object> parameters) {
        final Map<String, Object> attributes = new HashMap<>(super.getRuntimeAttributes(svc, parameters));

        // Determine the target address
        final InetAddress targetIpAddr = (InetAddress) determineTargetAddress(svc, parameters);

        // Determine the node to use as our IOS ping proxy
        final InetAddress proxyIpAddr = determineProxyAddress(parameters, svc);
        if (proxyIpAddr == null) {
            LOG.debug("Unable to determine proxy address for this service");
            throw new IllegalStateException("Unable to determine proxy address for this service");
        }

        attributes.put("targetIpAddr", InetAddrUtils.str(targetIpAddr));
        attributes.put("proxyIpAddr", InetAddrUtils.str(proxyIpAddr));
        return attributes;
    }
 
    /**
     * {@inheritDoc}
     *
     * <P>
     * The poll() method is responsible for setting up and following up the IOS
     * ping entry that proxies monitoring of the specified address for ICMP
     * service availability.
     * </P>
     * @exception RuntimeException
     *                Thrown for any unrecoverable errors.
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        final InetAddress targetIpAddr = InetAddrUtils.addr(getKeyedString(parameters, "targetIpAddr", null));
        final InetAddress proxyIpAddr = InetAddrUtils.addr(getKeyedString(parameters, "proxyIpAddr", null));

        int pingProtocol = 0;
        try {
            pingProtocol = determineAddrType(targetIpAddr);
        } catch (RuntimeException e) {
            LOG.debug("Unknown address type - neither IPv4 nor IPv6", e);
            return PollStatus.unavailable("Unknown address type - neither IPv4 nor IPv6");
        }

        // Get configuration parameters into a CiscoPingEntry object
        //
        CiscoPingEntry pingEntry = new CiscoPingEntry();
        pingEntry.setCiscoPingPacketCount(ParameterMap.getKeyedInteger(parameters, PARM_PACKET_COUNT, PARM_PACKET_COUNT_DEFAULT));
        pingEntry.setCiscoPingPacketSize(ParameterMap.getKeyedInteger(parameters, PARM_PACKET_SIZE, PARM_PACKET_SIZE_DEFAULT));
        pingEntry.setCiscoPingPacketTimeout(ParameterMap.getKeyedInteger(parameters, PARM_PACKET_TIMEOUT, PARM_PACKET_TIMEOUT_DEFAULT));
        pingEntry.setCiscoPingPacketDelay(ParameterMap.getKeyedInteger(parameters, PARM_PACKET_DELAY, PARM_PACKET_DELAY_DEFAULT));
        pingEntry.setCiscoPingEntryOwner(ParameterMap.getKeyedString(parameters, PARM_ENTRY_OWNER, PARM_ENTRY_OWNER_DEFAULT));
        pingEntry.setCiscoPingVrfName(ParameterMap.getKeyedString(parameters, PARM_VRF_NAME, PARM_VRF_NAME_DEFAULT));
        
        pingEntry.setCiscoPingSerialNumber(Double.valueOf(System.currentTimeMillis() / 1000d).intValue());
        pingEntry.setCiscoPingProtocol(pingProtocol);
        pingEntry.setCiscoPingAddress(targetIpAddr);
        pingEntry.setCiscoPingEntryStatus(ROWSTATUS_CREATE_AND_GO);
        
        int minSuccessPercent = ParameterMap.getKeyedInteger(parameters, PARM_SUCCESS_PERCENT, PARM_SUCCESS_PERCENT_DEFAULT);
        
        // FIXME: Should the cleanup stuff be fixed to actually use this? Not clear if it really matters.
        // int cleanupInterval = ParameterMap.getKeyedInteger(parameters, PARM_CLEANUP_INTERVAL, PARM_CLEANUP_INTERVAL_DEFAULT);

        // Retrieve the *proxy* interface's SNMP peer object
        //
        SnmpAgentConfig agentConfig = getAgentConfig(svc, parameters);
        LOG.debug("poll: setting SNMP peer attribute for interface {}", proxyIpAddr.getHostAddress());

        // set timeout and retries on SNMP peer object
        //
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));

        LOG.debug("Setting up CISCO-PING-MIB proxy poll for service {} on interface {} -- {}", svc.getSvcName(), targetIpAddr, pingEntry);

        PollStatus serviceStatus = null;
        TimeoutTracker timeoutTracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);
        
        // Send the SET-REQUEST PDU to create the ciscoPingEntry in createAndGo mode
        SnmpValue[] setResult = SnmpUtils.set(agentConfig, pingEntry.generateCreateOids(), pingEntry.generateCreateValues());
        if (setResult == null) {
		LOG.warn("SNMP SET operation unsuccessful for proxy-ping entry for target {} -- {}", targetIpAddr, pingEntry);
        	return PollStatus.unknown("SNMP SET failed for ciscoPingTable entry on proxy interface " + proxyIpAddr + " with instance ID " + pingEntry.getCiscoPingSerialNumber());
        }
        
        // With the ciscoPingEntry created, we now wait until the specified pings have had time to
        // complete.  Twice the time it would take assuming a zero response time per ping seems like
        // a good starting point.
        try {
			Thread.sleep(pingEntry.calculateMinInitialWait() * 2L);
		} catch (InterruptedException e) { }
        
        // Now check whether the ping has completed and, if so, whether it succeeded and its times
        SnmpValue[] statusValues = null;
        for (timeoutTracker.reset(); (timeoutTracker.shouldRetry() && (statusValues == null || statusValues.length < 6 || statusValues[5].toInt() != 1)); timeoutTracker.nextAttempt()) {
        	statusValues = SnmpUtils.get(agentConfig, pingEntry.generateResultsOids());
        }

        // If we didn't get the results back, mark the service as unknown
        if (statusValues == null || (statusValues.length == 1 && statusValues[0] == null)) {
		LOG.warn("SNMP GET operation unsuccessful for proxy-ping entry for target {} -- {}", targetIpAddr, pingEntry);
        	return PollStatus.unknown("SNMP GET failed for ciscoPingTable entry on proxy interface " + proxyIpAddr + " with instance ID " + pingEntry.getCiscoPingSerialNumber());
        }
        
        // If we got results back but they do not contain the pingCompleted column is missing,
        // mark the service unknown
        if (statusValues.length < 6) {
		LOG.warn("Proxy-ping entry did not indicate whether ping completed after retries exhausted for target {} -- {}", targetIpAddr, pingEntry);
        	return PollStatus.unknown("ciscoPingTable entry is missing pingCompleted column on proxy interface " + proxyIpAddr + " with instance ID " + pingEntry.getCiscoPingSerialNumber());        	        	
        }
        
        // If we got the results back but they indicate that the ping still has not completed,
        // mark the service unknown
        if (statusValues[5].toInt() != 1) {
		LOG.warn("Proxy-ping entry marked not completed after retries exhausted for target {} -- {}", targetIpAddr, pingEntry);
        	return PollStatus.unknown("ciscoPingTable entry marked not completed on proxy interface " + proxyIpAddr + " with instance ID " + pingEntry.getCiscoPingSerialNumber());        	
        }
        
        // If the ping has completed, verify that the percent of completed pings meets our minimum
        // success percent.  If not, mark the service down.
        double sentPings = statusValues[0].toInt();
        double receivedPings = statusValues[1].toInt();
        double successPct = receivedPings / sentPings * 100;
        if (receivedPings == 0) {
		LOG.info("Proxy-ping entry indicates no pings succeeded for target {} -- {}", targetIpAddr, pingEntry);
        	cleanupCurrentEntry(pingEntry, proxyIpAddr, agentConfig);
        	return PollStatus.unavailable("All remote pings (" + sentPings + " of " + sentPings + ") failed");
        } else if (successPct < minSuccessPercent) {
		LOG.info("Proxy-ping entry indicates {}% success, which misses the success-percent target of {}% for target {} -- {}", successPct, minSuccessPercent, targetIpAddr, pingEntry);
        	cleanupCurrentEntry(pingEntry, proxyIpAddr, agentConfig);
        	return PollStatus.unavailable(successPct + " percent (" + receivedPings + "/" + sentPings+ ") pings succeeded, less than target " + minSuccessPercent + " percent");
        }
        
        // If we've arrived here, then enough pings completed to consider the service up!
        Map<String,Number> pingProps = new HashMap<String,Number>();
        double minRtt = statusValues[2].toInt();
        double avgRtt = statusValues[3].toInt();
        double maxRtt = statusValues[4].toInt();
        LOG.debug("Logging successful poll: sent={}, received={}, minRtt={}, avgRtt={}, maxRtt={} for proxy-ping of target {} -- {}", sentPings, receivedPings, minRtt, avgRtt, maxRtt, targetIpAddr, pingEntry);
        pingProps.put("sent", sentPings);
        pingProps.put("received", receivedPings);
        pingProps.put("minRtt", minRtt);
        pingProps.put("avgRtt", avgRtt);
        pingProps.put("maxRtt", maxRtt);
        
        cleanupCurrentEntry(pingEntry, proxyIpAddr, agentConfig);
        
        // TODO: Find and clean up defunct rows before returning
        // Actually it's not clear that this is necessary, seems IOS cleans up old
        // entries on its own some minutes after their creation.  Need to investigate.
        
        serviceStatus = PollStatus.available(avgRtt);
        serviceStatus.setProperties(pingProps);
        return serviceStatus;
    }

	private void cleanupCurrentEntry(CiscoPingEntry pingEntry, InetAddress proxyIpAddr, SnmpAgentConfig agentConfig) {
        pingEntry.setCiscoPingEntryStatus(ROWSTATUS_DESTROY);
        SnmpValue[] destroyValues = SnmpUtils.set(agentConfig, pingEntry.generateRowStatusOids(), pingEntry.generateRowStatusValues());
        if (destroyValues == null) LOG.warn("SNMP SET failed to delete just-used ciscoPingEntry on proxy interface {} with instance ID {}", proxyIpAddr, pingEntry.getCiscoPingSerialNumber());
        if (destroyValues[0].toInt() != ROWSTATUS_DESTROY) LOG.warn("SNMP SET to delete just-used ciscoPingEntry indicated row not deleted on proxy interface {} with instance ID {}", proxyIpAddr, pingEntry.getCiscoPingSerialNumber());
	}

	private InetAddress determineTargetAddress(MonitoredService svc, Map<String, Object> parameters) {
		String rawOverrideTarget = ParameterMap.getKeyedString(parameters, PARM_TARGET_IP_ADDR, null);
		String overrideTarget = rawOverrideTarget;
		if (rawOverrideTarget != null) {
			overrideTarget = PropertiesUtils.substitute(rawOverrideTarget, getServiceProperties(svc));
			LOG.debug("Expanded value '{}' of parameter {} to '{}' for service {} on interface {}", rawOverrideTarget, PARM_TARGET_IP_ADDR, overrideTarget, svc.getSvcName(), svc.getAddress());
		}
		
		if (overrideTarget == null) return svc.getAddress();
		LOG.debug("Using user-specified override target IP address {} instead of service address {} for service {}", overrideTarget, svc.getAddress(), svc.getSvcName());
		try {
			final InetAddress overrideAddr = InetAddressUtils.addr(overrideTarget);
			LOG.debug("Overriding service address ({}) with user-specified target address ({}) for service {}", svc.getAddress(), overrideAddr, svc.getSvcName());
			return overrideAddr;
		} catch (final IllegalArgumentException e) {
			LOG.warn("Failed to look up {} override value {} for service {}. Using service interface {} instead", PARM_TARGET_IP_ADDR, overrideTarget, svc.getSvcName(), svc.getAddress());
		}
		return svc.getAddress();
	}

	private InetAddress determineProxyAddress(Map<String, Object> parameters, MonitoredService svc) {
		LOG.debug("Determining the proxy address on which to set up the ciscoPingEntry for target interface {}", svc.getAddress());
		OnmsNode proxyNode = null;
		InetAddress proxyAddress = null;
		String proxyNodeId = ParameterMap.getKeyedString(parameters, PARM_PROXY_NODE_ID, null);
		String proxyNodeFS = ParameterMap.getKeyedString(parameters, PARM_PROXY_FOREIGN_SOURCE, null);
		String proxyNodeFI = ParameterMap.getKeyedString(parameters, PARM_PROXY_FOREIGN_ID, null);
		
		String rawProxyIpAddr = ParameterMap.getKeyedString(parameters, PARM_PROXY_IP_ADDR, null);
		String proxyIpAddr = rawProxyIpAddr;
		if (rawProxyIpAddr != null) {
			proxyIpAddr = PropertiesUtils.substitute(rawProxyIpAddr, getServiceProperties(svc));
			LOG.debug("Expanded value '{}' of parameter {} to '{}' for service {} on interface {}", rawProxyIpAddr, PARM_PROXY_IP_ADDR, proxyIpAddr, svc.getSvcName(), svc.getAddress());
		}

		/* If we have a foreign-source and foreign-id, short circuit to use that */
		if (proxyNodeFS != null && !proxyNodeFS.equals("") && proxyNodeFI != null && !proxyNodeFI.equals("")) {
			LOG.debug("Trying to look up proxy node with foreign-source {}, foreign-id {} for target interface {}", proxyNodeFS, proxyNodeFI, svc.getAddress());
			proxyNode = nodeDao.get().findByForeignId(proxyNodeFS, proxyNodeFI);
			LOG.debug("Found a node via foreign-source / foreign-id '{}'/'{}' to use as proxy", proxyNodeFS, proxyNodeFI);
			if (proxyNode != null && proxyNode.getPrimaryInterface() != null) proxyAddress = proxyNode.getPrimaryInterface().getIpAddress();
		}
		if (proxyAddress != null) {
			LOG.info("Using address {} from node '{}':'{}' as proxy for service '{}' on interface {}", proxyAddress, proxyNodeFS, proxyNodeFI, svc.getSvcName(), svc.getIpAddr());
			return proxyAddress;
		}

		/* No match with foreign-source / foreign-id?  Try with a node ID */
		if (proxyNodeId != null && Integer.valueOf(proxyNodeId) != null) {
			LOG.debug("Trying to look up proxy node with database ID {} for target interface {}", proxyNodeId, svc.getAddress());
			proxyNode = nodeDao.get().get(Integer.valueOf(proxyNodeId));
			if (proxyNode != null && proxyNode.getPrimaryInterface() != null) proxyAddress = proxyNode.getPrimaryInterface().getIpAddress();
		}
		if (proxyAddress != null) {
			LOG.info("Using address {} from node with DBID {} as proxy for service '{}' on interface {}", proxyAddress, proxyNodeId, svc.getSvcName(), svc.getIpAddr());
			return proxyAddress;
		}
		
		/* No match with any node criteria?  Try for a plain old IP address. */
		LOG.info("Trying to use address {} as proxy-ping agent address for target interface {}", proxyIpAddr, svc.getAddress());
		try {
			if (!"".equals(proxyIpAddr)) {
				proxyAddress = InetAddressUtils.addr(proxyIpAddr);
			}
		} catch (final IllegalArgumentException e) {}
		if (proxyAddress != null) {
			LOG.info("Using address {} (user-specified) as proxy for service '{}' on interface {}", proxyAddress, svc.getSvcName(), svc.getIpAddr());
			return proxyAddress;
		}
		
		LOG.error("Unable to determine proxy address for service '{}' on interface '{}'. The poll will be unable to proceed.", svc.getSvcName(), svc.getIpAddr());
		return null;
	}

	private int determineAddrType(InetAddress ipaddr) {
		if (ipaddr instanceof Inet6Address) {
			LOG.debug("The address {} is IPv6", ipaddr);
			return PING_PROTOCOL_IPV6;
		} else if (ipaddr instanceof Inet4Address) {
			LOG.debug("The address {} is IPv4", ipaddr);
			return PING_PROTOCOL_IPV4;
		}
		LOG.error("The address {} is neither IPv4 nor IPv6. Don't know how to proceed, giving up.", ipaddr);
		throw new RuntimeException("Cannot work with address " + ipaddr + " because it is neither IPv4 nor IPv6.");
	}
}
