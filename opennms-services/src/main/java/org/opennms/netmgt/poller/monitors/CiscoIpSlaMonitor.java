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

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class is used to monitor if a particular Cisco IP-SLA runs within
 * thresholds and has no timeouts. The configured IP-SLA is monitored by the
 * specified "ip sla tag"
 * </p>
 * <p>
 * This does SNMP and therefore relies on the SNMP configuration so it is not distributable.
 * </p>
 *
 * @author <A HREF="mailto:r.trommer@open-factory.org">Ronny Trommer</A>
 * @version $Id: $
 */

@Distributable(DistributionContext.DAEMON)
final public class CiscoIpSlaMonitor extends SnmpMonitorStrategy {
    
    public static final Logger LOG = LoggerFactory.getLogger(CiscoIpSlaMonitor.class);
    
    /**
     * Name of monitored service.
     */
    private static final String SERVICE_NAME = "Cisco_IP_SLA";

    /**
     * A string which is used by a managing application to identify the RTT
     * target.
     */
    private static final String RTT_ADMIN_TAG_OID = ".1.3.6.1.4.1.9.9.42.1.2.1.1.3";

    /**
     * The RttMonOperStatus object is used to manage the state.
     */
    private static final String RTT_OPER_STATE_OID = ".1.3.6.1.4.1.9.9.42.1.2.9.1.10";

    /**
     * A sense code for the completion status of the latest RTT operation.
     */
    private static final String RTT_LATEST_OPERSENSE_OID = ".1.3.6.1.4.1.9.9.42.1.2.10.1.2";

    /**
     * his object defines an administrative threshold limit. If the RTT operation 
     * time exceeds this limit and if the conditions specified in 
     * rttMonReactAdminThresholdType or rttMonHistoryAdminFilter are satisfied, 
     * a threshold is generated.
     */
    private static final String RTT_ADMIN_THRESH_OID = ".1.3.6.1.4.1.9.9.42.1.2.1.1.5";
    
    /**
     * The type of RTT operation to be performed.
     */
    private static final String RTT_ADMIN_TYPE_OID = ".1.3.6.1.4.1.9.9.42.1.2.1.1.4";
    
    /**
     * The completion time of the latest RTT operation successfully completed.
     */
    private static final String RTT_LATEST_OID = ".1.3.6.1.4.1.9.9.42.1.2.10.1.1";
    
    
    /**
     * Implement the rttMonCtrlOperState
     */
    private enum RTT_MON_OPER_STATE {
        RESET(1), ORDERLY_STOP(2), IMMEDIATE_STOP(3), PENDING(4), INACTIVE(5), ACTIVE(
                6), RESTART(7);

        private final int state; // state code

        RTT_MON_OPER_STATE(int s) {
            this.state = s;
        }

        private int value() {
            return this.state;
        }
    };

    /**
     * Implement the rttMonCtrlOperSense
     */
    private enum RTT_MON_OPER_SENSE {
        OTHER(0), OK(1), DISCONNECTED(2), OVER_THRESHOLD(3), TIMEOUT(4), BUSY(
                5), NOT_CONNECTED(6), DROPPED(7), SEQUENCE_ERROR(8), VERIFY_ERROR(
                9), APPLICATION_SPECIFIC(10), DNS_SERVER_TIMEOUT(11), TCP_CONNECT_TIMEOUT(
                12), HTTP_TRANSACTION_TIMEOUT(13), DNS_QUERY_ERROR(14), HTTP_ERROR(
                15), ERROR(16);

        private final int state; // state code

        RTT_MON_OPER_SENSE(int s) {
            this.state = s;
        }

        private int value() {
            return this.state;
        }
    };

    /**
     * Implement the rttMonCtrlAdminRttType
     */
    private enum RTT_MON_ADMIN_TYPE {
        ECHO(1), PATH_ECHO(2), FILE_IO(3), SCRIPT(4), UDP_ECHO(5),TCP_CONNECT(
                6), HTTP(7), DNS(8), JITTER(9), DLSW(10), DHCP(11), FTP(12);

        private final int state; // state code

        RTT_MON_ADMIN_TYPE(int s) {
            this.state = s;
        }

        private int value() {
            return this.state;
        }
    };
    
    /**
     * <P>
     * Returns the name of the service that the plug-in monitors
     * ("Cisco IP-SLA monitor").
     * </P>
     *
     * @return The service that the plug-in monitors.
     */
    public String serviceName() {
        return SERVICE_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * <P>
     * Initialize the service monitor.
     * </P>
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents
     *                the plug-in from functioning.
     */
    @Override
    public void initialize(Map<String,Object> parameters) {
        // Initialize the SnmpPeerFactory
        //
        try {
            SnmpPeerFactory.init();
        } catch (IOException ex) {
            LOG.error("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        return;
    }

    /**
     * <P>
     * Called by the poller framework when an interface is being added to the
     * scheduler. Here we perform any necessary initialization to prepare the
     * NetworkInterface object for polling.
     * </P>
     *
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents
     *                the interface from being monitored.
     * @param svc a {@link org.opennms.netmgt.poller.MonitoredService} object.
     */
    @Override
    public void initialize(MonitoredService svc) {
        super.initialize(svc);
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <P>
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     * @exception RuntimeException
     *                Thrown for any uncrecoverable errors.
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String,Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        String returnValue = "SNMP request failed or Cisco IP SLA tag ";
        boolean monitorThresh = false;
        
        PollStatus status = PollStatus.unavailable();
        InetAddress ipaddr = (InetAddress) iface.getAddress();

        // Retrieve this interface's SNMP peer object
        //
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipaddr);
        if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available for interface " + ipaddr);
        final String hostAddress = InetAddressUtils.str(ipaddr);
		LOG.debug("poll: setting SNMP peer attribute for interface {}", hostAddress);

        // Get configuration parameters for tag to monitor
        String adminTag = ParameterMap.getKeyedString(parameters,"admin-tag", null);
        if (adminTag == null) {
            LOG.debug("No IP SLA admin-tag parameter defined! ");
            status = PollStatus.unavailable("No IP SLA admin-tag parameter defined! ");
            return status;
        }
        
        // If no ip sla tag found, tell which ip sla tag is configured
        returnValue += adminTag + " not found";
        
        // Get configuration parameter to check if threshold should monitor
        String ignoreThreshold = ParameterMap.getKeyedString(parameters,"ignore-thresh",null);
        if (ignoreThreshold == null) {
            LOG.debug("No ignoreThreshold parmater defined! ");
            status = PollStatus.unavailable("No ignoreThreshold parmater defined! ");
            return status;
        }

        // Convert threshold parameter to boolean
        if (ignoreThreshold.equals("false")) {
            monitorThresh = true;
        } 

        // set timeout and retries on SNMP peer object
        //
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters,"timeout",agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters,"retry",ParameterMap.getKeyedInteger(parameters,"retries",agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters,"port",agentConfig.getPort()));

        // Establish SNMP session with interface
        try {
            LOG.debug("poll: SnmpAgentConfig address: {}", agentConfig);

            // Get all configured ip sla tags
            Map<SnmpInstId, SnmpValue> tagResults = SnmpUtils.getOidValues(agentConfig,"CiscoIpSlaMonitor",SnmpObjId.get(RTT_ADMIN_TAG_OID));
            if (tagResults == null) {
                LOG.debug("No admin tags received! ");
                status = PollStatus.unavailable("No admin tags received! ");
                return status;
            }
            
            // Iterate over the list of configured IP SLAs
            for (SnmpInstId ipslaInstance : tagResults.keySet()) {
                
                // Check if a given IP-SLA with the specific "tag" exist
                if (tagResults.get(ipslaInstance).toString().equals(adminTag)) {

                    // Get all operation sense
                    Map<SnmpInstId, SnmpValue> operSenseResults = SnmpUtils.getOidValues(agentConfig,"CiscoIpSlaMonitor",SnmpObjId.get(RTT_LATEST_OPERSENSE_OID));
                    if (operSenseResults == null) {
                        LOG.debug("No latest oper sense received! ");
                        status = PollStatus.unavailable("No latest oper sense received! ");
                        return status;
                    }
            
                    // Get all operation states
                    Map<SnmpInstId, SnmpValue> operStateResults = SnmpUtils.getOidValues(agentConfig,"CiscoIpSlaMonitor",SnmpObjId.get(RTT_OPER_STATE_OID));
                    if (operStateResults == null) {
                        LOG.debug("No oper state received! ");
                        status = PollStatus.unavailable("No oper state received! ");
                        return status;
                    }
                    
                    // Get all configured ip sla types
                    Map<SnmpInstId, SnmpValue> adminTypeResults = SnmpUtils.getOidValues(agentConfig,"CiscoIpSlaMonitor",SnmpObjId.get(RTT_ADMIN_TYPE_OID));
                    if (adminTypeResults == null) {
                        LOG.debug("No ip sla types received! ");
                        status = PollStatus.unavailable("No ip sla types received! ");
                        return status;
                    }
                    
                    // Get all configured ip sla latest RTT
                    Map<SnmpInstId, SnmpValue> latestRttResults = SnmpUtils.getOidValues(agentConfig,"CiscoIpSlaMonitor",SnmpObjId.get(RTT_LATEST_OID));
                    if (latestRttResults == null) {
                        LOG.debug("No ip sla latest RTT received! ");
                        status = PollStatus.unavailable("No ip sla latest RTT received! ");
                        return status;
                    }
                    
                    LOG.debug("poll: instance={} admin tag={} value={} oper state={} ignoreThreshold={} latest RTT{}", ipslaInstance.toInt(), adminTag, tagResults.get(ipslaInstance), operStateResults.get(ipslaInstance), ignoreThreshold, latestRttResults.get(ipslaInstance));
                    
                    // Build return value for service down
                    returnValue = "Cisco IP SLA tag "
                        + adminTag
                        + " with oper state "
                        + resolveOperSate(operStateResults.get(ipslaInstance).toInt())
                        + " returned with oper sense "
                        + resolveOperSense(operSenseResults.get(ipslaInstance).toInt())
                        + ". Configured IP SLA type is " + resolveAdminType(adminTypeResults.get(ipslaInstance).toInt())
                        + ". Latest RTT is " + latestRttResults.get(ipslaInstance);
                    LOG.debug(returnValue);
                    
                    // Check if thresholding is relevant
                    if (monitorThresh
                            && (operSenseResults.get(ipslaInstance).toInt() == RTT_MON_OPER_SENSE.OVER_THRESHOLD.value())) {
                        
                        // Get all configured ip sla thresholds
                        Map<SnmpInstId, SnmpValue> threshResults = SnmpUtils.getOidValues(agentConfig,"CiscoIpSlaMonitor",SnmpObjId.get(RTT_ADMIN_THRESH_OID));
                        if (monitorThresh && threshResults == null) {
                            LOG.debug("No ip sla thresholds received! ");
                            status = PollStatus.unavailable("No ip sla thresholds received! ");
                            return status;
                        }

                        // Threshold monitoring
                        LOG.debug("IP SLA: {} threshold exceeded.", tagResults.get(ipslaInstance));
                        returnValue += ". Monitoring threshold is enabled. Threshold value is " 
                            + threshResults.get(ipslaInstance);
                        // Configured threshold is exceeded, service unavailable
                        return PollStatus.unavailable(returnValue);
                    } else {
                        /*
                         *  Threshold should be ignored, check if OK or
                         *  OVER_THRESHOLD.
                         *  Over threshold means also OK, no timeout or other 
                         *  error occurred.
                         */
                        if (operSenseResults.get(ipslaInstance).toInt() == RTT_MON_OPER_SENSE.OK.value()
                                || operSenseResults.get(ipslaInstance).toInt() == RTT_MON_OPER_SENSE.OVER_THRESHOLD.value()) {
                            LOG.debug("Threshold is ignored rttMonLatestOperSense: {}", operSenseResults.get(ipslaInstance).toInt());
                            LOG.debug(returnValue);
                            status = PollStatus.available(Double.parseDouble(latestRttResults.get(ipslaInstance).toString()));
                            // No error or connection timeout, service available
                            return status;
                        }
                    }
                } // else no configured IP SLA tag exist
            }

            // Otherwise set service down
            status = PollStatus.unavailable(returnValue);

        } catch (NullPointerException e) {
            String reason = "Unexpected error during SNMP poll of interface "
                     + hostAddress;
            LOG.debug(reason, e);
            status = PollStatus.unavailable(reason);
        } catch (NumberFormatException e) {
            String reason = "Number operator used on a non-number "
                     + e.getMessage();
            LOG.debug(reason);
            status = PollStatus.unavailable(reason);
        } catch (IllegalArgumentException e) {
            String reason = "Invalid SNMP Criteria: "
                    + e.getMessage();
            LOG.debug(reason);
            status = PollStatus.unavailable(reason);
        } catch (Throwable t) {
            String reason = "Unexpected exception during SNMP poll of interface "
                     + hostAddress;
            LOG.debug(reason, t);
            status = PollStatus.unavailable(reason);
        }

        // Otherwise, the service will be unavailable.
        return status;
    }

    /**
     * Method to resolve a given ip sla operation state code to human readable 
     * string. 
     * TODO: Check if there is a better way to resolve the states backward
     * 
     * @param sc
     *            operation state code
     * @return Human readable operation state
     */
    private String resolveOperSate(int sc) {
        String name = "UNKNOWN";
        if (RTT_MON_OPER_STATE.RESET.value() == sc)
            name = RTT_MON_OPER_STATE.RESET.name();
        if (RTT_MON_OPER_STATE.ORDERLY_STOP.value() == sc)
            name = RTT_MON_OPER_STATE.ORDERLY_STOP.name();
        if (RTT_MON_OPER_STATE.IMMEDIATE_STOP.value() == sc)
            name = RTT_MON_OPER_STATE.IMMEDIATE_STOP.name();
        if (RTT_MON_OPER_STATE.PENDING.value() == sc)
            name = RTT_MON_OPER_STATE.PENDING.name();
        if (RTT_MON_OPER_STATE.INACTIVE.value() == sc)
            name = RTT_MON_OPER_STATE.INACTIVE.name();
        if (RTT_MON_OPER_STATE.ACTIVE.value() == sc)
            name = RTT_MON_OPER_STATE.ACTIVE.name();
        if (RTT_MON_OPER_STATE.RESTART.value() == sc)
            name = RTT_MON_OPER_STATE.RESTART.name();
        return name;
    }

    /**
     * Method to resolve a given ip sla operation sense code to human readable string. 
     * TODO: Check if there is a better way to resolve the states backward
     * 
     * @param sc
     *            operation sense code
     * @return Human readable operation sense
     */
    private String resolveOperSense(int sc) {
        String name = "UNKNOWN";
        if (RTT_MON_OPER_SENSE.OTHER.value() == sc)
            name = RTT_MON_OPER_SENSE.OTHER.name();
        if (RTT_MON_OPER_SENSE.OK.value() == sc)
            name = RTT_MON_OPER_SENSE.OK.name();
        if (RTT_MON_OPER_SENSE.DISCONNECTED.value() == sc)
            name = RTT_MON_OPER_SENSE.DISCONNECTED.name();
        if (RTT_MON_OPER_SENSE.OVER_THRESHOLD.value() == sc)
            name = RTT_MON_OPER_SENSE.OVER_THRESHOLD.name();
        if (RTT_MON_OPER_SENSE.TIMEOUT.value() == sc)
            name = RTT_MON_OPER_SENSE.TIMEOUT.name();
        if (RTT_MON_OPER_SENSE.BUSY.value() == sc)
            name = RTT_MON_OPER_SENSE.BUSY.name();
        if (RTT_MON_OPER_SENSE.NOT_CONNECTED.value() == sc)
            name = RTT_MON_OPER_SENSE.NOT_CONNECTED.name();
        if (RTT_MON_OPER_SENSE.DROPPED.value() == sc)
            name = RTT_MON_OPER_SENSE.DROPPED.name();
        if (RTT_MON_OPER_SENSE.SEQUENCE_ERROR.value() == sc)
            name = RTT_MON_OPER_SENSE.SEQUENCE_ERROR.name();
        if (RTT_MON_OPER_SENSE.VERIFY_ERROR.value() == sc)
            name = RTT_MON_OPER_SENSE.VERIFY_ERROR.name();
        if (RTT_MON_OPER_SENSE.APPLICATION_SPECIFIC.value() == sc)
            name = RTT_MON_OPER_SENSE.APPLICATION_SPECIFIC.name();
        if (RTT_MON_OPER_SENSE.DNS_SERVER_TIMEOUT.value() == sc)
            name = RTT_MON_OPER_SENSE.DNS_SERVER_TIMEOUT.name();
        if (RTT_MON_OPER_SENSE.TCP_CONNECT_TIMEOUT.value() == sc)
            name = RTT_MON_OPER_SENSE.TCP_CONNECT_TIMEOUT.name();
        if (RTT_MON_OPER_SENSE.HTTP_TRANSACTION_TIMEOUT.value() == sc)
            name = RTT_MON_OPER_SENSE.HTTP_TRANSACTION_TIMEOUT.name();
        if (RTT_MON_OPER_SENSE.DNS_QUERY_ERROR.value() == sc)
            name = RTT_MON_OPER_SENSE.DNS_QUERY_ERROR.name();
        if (RTT_MON_OPER_SENSE.HTTP_ERROR.value() == sc)
            name = RTT_MON_OPER_SENSE.HTTP_ERROR.name();
        if (RTT_MON_OPER_SENSE.ERROR.value() == sc)
            name = RTT_MON_OPER_SENSE.ERROR.name();
        return name;
    }
    
    /**
     * Method to resolve a given ip sla admin type to human readable string. 
     * TODO: Check if there is a better way to resolve the states backward
     * 
     * @param sc
     *            oper state code
     * @return Human readable oper state
     */
    private String resolveAdminType(int sc) {
        String name = "UNKNOWN";
        if (RTT_MON_ADMIN_TYPE.ECHO.value() == sc)
            name = RTT_MON_ADMIN_TYPE.ECHO.name();
        if (RTT_MON_ADMIN_TYPE.PATH_ECHO.value() == sc)
            name = RTT_MON_ADMIN_TYPE.PATH_ECHO.name();
        if (RTT_MON_ADMIN_TYPE.FILE_IO.value() == sc)
            name = RTT_MON_ADMIN_TYPE.FILE_IO.name();
        if (RTT_MON_ADMIN_TYPE.SCRIPT.value() == sc)
            name = RTT_MON_ADMIN_TYPE.SCRIPT.name();
        if (RTT_MON_ADMIN_TYPE.UDP_ECHO.value() == sc)
            name = RTT_MON_ADMIN_TYPE.UDP_ECHO.name();
        if (RTT_MON_ADMIN_TYPE.TCP_CONNECT.value() == sc)
            name = RTT_MON_ADMIN_TYPE.TCP_CONNECT.name();
        if (RTT_MON_ADMIN_TYPE.HTTP.value() == sc)
            name = RTT_MON_ADMIN_TYPE.HTTP.name();
        if (RTT_MON_ADMIN_TYPE.DNS.value() == sc)
            name = RTT_MON_ADMIN_TYPE.DNS.name();
        if (RTT_MON_ADMIN_TYPE.JITTER.value() == sc)
            name = RTT_MON_ADMIN_TYPE.JITTER.name();
        if (RTT_MON_ADMIN_TYPE.DLSW.value() == sc)
            name = RTT_MON_ADMIN_TYPE.DLSW.name();
        if (RTT_MON_ADMIN_TYPE.DHCP.value() == sc)
            name = RTT_MON_ADMIN_TYPE.DHCP.name();
        if (RTT_MON_ADMIN_TYPE.FTP.value() == sc)
            name = RTT_MON_ADMIN_TYPE.FTP.name();
        return name;
    }
}
