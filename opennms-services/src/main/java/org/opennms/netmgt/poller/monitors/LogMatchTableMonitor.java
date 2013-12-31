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

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.ArrayList;

import org.apache.log4j.Level;
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

/**
 * <p>
 * Check for Log matches via UCD-SNMP-MIB .
 * </p>
 * <p>
 * This does SNMP and therefore relies on the SNMP configuration so it is not distributable.
 * </p>
 *
 * @author <A HREF="mailto:cliles@capario.com">Chris Liles</A>
 * @version $Id: $
 */

@Distributable(DistributionContext.DAEMON)
final public class LogMatchTableMonitor extends SnmpMonitorStrategy {
    private static final String m_serviceName = "LogMatch-Table";
    
    private static final String lmTableErrorFlag = "1.3.6.1.4.1.2021.16.2.1.100";
    private static final String lmTableFileName = "1.3.6.1.4.1.2021.16.2.1.3";
    private static final String lmTableRegEx = "1.3.6.1.4.1.2021.16.2.1.4";
    private static final String lmTableCount = "1.3.6.1.4.1.2021.16.2.1.10";
    
    /**
     * <P>
     * Returns the name of the service that the plug-in monitors ("LogMatch-Table").
     * </P>
     *
     * @return The service that the plug-in monitors.
     */
    public String serviceName() {
        return m_serviceName;
    }

    /**
     * {@inheritDoc}
     *
     * <P>
     * Initialize the service monitor.
     * </P>
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     */
    public void initialize(Map<String, Object> parameters) {
        // Initialize the SnmpPeerFactory
        //
        try {
            SnmpPeerFactory.init();
        } catch (IOException ex) {
        	log().fatal("initialize: Failed to load SNMP configuration", ex);
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
     *                Thrown if an unrecoverable error occurs that prevents the
     *                interface from being monitored.
     * @param svc a {@link org.opennms.netmgt.poller.MonitoredService} object.
     */
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
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        PollStatus status = PollStatus.available();
        InetAddress ipaddr = (InetAddress) iface.getAddress();

        ArrayList errorStringReturn = new ArrayList();
        
        // Retrieve this interface's SNMP peer object
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipaddr);
        if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available for interface " + ipaddr);
        final String hostAddress = InetAddressUtils.str(ipaddr);
        log().debug("poll: setting SNMP peer attribute for interface " + hostAddress);
        
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));
        
        if (log().isDebugEnabled()) log().debug("poll: service= SNMP address= " + agentConfig);
        
        try {
            if (log().isDebugEnabled()) {
                log().debug("PrTableMonitor.poll: SnmpAgentConfig address: " +agentConfig);
            }
            SnmpObjId lmTableErrorSnmpObject = SnmpObjId.get(lmTableErrorFlag);
            
            Map<SnmpInstId, SnmpValue> flagResults = SnmpUtils.getOidValues(agentConfig, "LogMatchTableMonitor", lmTableErrorSnmpObject);
            
            if(flagResults.size() == 0) {
                log().debug("SNMP poll failed: no results, addr=" + hostAddress + " oid=" + lmTableErrorSnmpObject);
                return PollStatus.unavailable();
            }

            for (Map.Entry<SnmpInstId, SnmpValue> e : flagResults.entrySet()) { 
                log().debug("poll: SNMPwalk poll succeeded, addr=" + hostAddress + " oid=" + lmTableErrorSnmpObject + " instance=" + e.getKey() + " value=" + e.getValue());
                
                if (e.getValue().toString().equals("1")) {
                	log().debug("LogMatchTableMonitor.poll: found errorFlag=1");

                	SnmpObjId lmTableFilenameSnmpObject = SnmpObjId.get(lmTableFileName + "." + e.getKey().toString());
                	SnmpObjId lmTableRegExSnmpObject = SnmpObjId.get(lmTableRegEx + "." + e.getKey().toString());
                	SnmpObjId lmTableCountSnmpObject = SnmpObjId.get(lmTableCount + "." + e.getKey().toString());

                  String lmErrorMsg = "Rexeg " + SnmpUtils.get(agentConfig,lmTableRegExSnmpObject).toDisplayString() + ", for log file " + SnmpUtils.get(agentConfig,lmTableFilenameSnmpObject).toDisplayString() + " has matched " + SnmpUtils.get(agentConfig,lmTableCountSnmpObject).toDisplayString() + "time(s).";

                  //Stash the error in an ArrayList to then enumerate over later
                  errorStringReturn.add(lmErrorMsg);
                }
            }

            //Check the arraylist and construct return value
            if (errorStringReturn.size() > 0) {
              return PollStatus.unavailable(errorStringReturn.toString());
            }
            else {
              return status;
            }

        } catch (NumberFormatException e) {
            status = logDown(Level.ERROR, "Number operator used on a non-number " + e.getMessage());
        } catch (IllegalArgumentException e) {
            status = logDown(Level.ERROR, "Invalid SNMP Criteria: " + e.getMessage());
        } catch (Throwable t) {
            status = logDown(Level.WARN, "Unexpected exception during SNMP poll of interface " + hostAddress, t);
        }

        return status;
    }
    
}
