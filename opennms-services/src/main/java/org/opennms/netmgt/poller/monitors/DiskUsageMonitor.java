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
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
// 2003 Jan 31: Cleaned up some unused imports.
// 2003 Jan 29: Added response times to certain monitors.
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
// Tab Size = 8
//

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
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
 * Check for disks via HOST-RESOURCES-MIB.  This should be extended to
 * support BOTH UCD-SNMP-MIB and HOST-RESOURCES-MIB
 * </p>
 *
 * @author <A HREF="mailto:jason.aras@gmail.com">Jason Aras</A>
 * @version $Id: $
 */

//this does snmp and there relies on the snmp configuration so it is not distributable
@Distributable(DistributionContext.DAEMON)
final public class DiskUsageMonitor extends SnmpMonitorStrategy {
    private static final String m_serviceName = "DISK-USAGE";
    
    private static final String hrStorageDescr = ".1.3.6.1.2.1.25.2.3.1.3";
    private static final String hrStorageSize  = ".1.3.6.1.2.1.25.2.3.1.5";
    private static final String hrStorageUsed  = ".1.3.6.1.2.1.25.2.3.1.6";
    
    /**
     * The available match-types for this monitor
     */
    private static final int MATCH_TYPE_EXACT = 0;
    private static final int MATCH_TYPE_STARTSWITH = 1;
    private static final int MATCH_TYPE_ENDSWITH = 2;
    private static final int MATCH_TYPE_REGEX = 3;

    /**
     * <P>
     * Returns the name of the service that the plug-in monitors ("DISK-USAGE").
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
        } catch (MarshalException ex) {
        	log().fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
        	log().fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
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
        int matchType = MATCH_TYPE_EXACT;
        
        NetworkInterface iface = svc.getNetInterface();

        PollStatus status = PollStatus.available();
        InetAddress ipaddr = (InetAddress) iface.getAddress();
        
        // Retrieve this interface's SNMP peer object
        //
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipaddr);
        if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available for interface " + ipaddr);
        log().debug("poll: setting SNMP peer attribute for interface " + ipaddr.getHostAddress());
        
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));
        
        String diskName = ParameterMap.getKeyedString(parameters, "disk", null);
        Integer percentFree = ParameterMap.getKeyedInteger(parameters, "free", 15);
        
        String matchTypeStr = ParameterMap.getKeyedString(parameters, "match-type", "exact");
        if (matchTypeStr.equalsIgnoreCase("exact")) {
            matchType = MATCH_TYPE_EXACT; 
        } else if (matchTypeStr.equalsIgnoreCase("startswith")) {
            matchType = MATCH_TYPE_STARTSWITH;
        } else if (matchTypeStr.equalsIgnoreCase("endswith")) {
            matchType = MATCH_TYPE_ENDSWITH;
        } else if (matchTypeStr.equalsIgnoreCase("regex")) {
            matchType = MATCH_TYPE_REGEX;
        } else {
            throw new RuntimeException("Unknown value '" + matchTypeStr + "' for parameter 'match-type'");
        }
        
        log().debug("diskName=" + diskName);
        log().debug("percentfree=" + percentFree);
        log().debug("matchType=" + matchTypeStr);
        
        if (log().isDebugEnabled()) log().debug("poll: service= SNMP address= " + agentConfig);

        
        try {
            if (log().isDebugEnabled()) {
                log().debug("DiskUsageMonitor.poll: SnmpAgentConfig address: " +agentConfig);
            }
            SnmpObjId hrStorageDescrSnmpObject = SnmpObjId.get(hrStorageDescr);
            
            
            
            Map<SnmpInstId, SnmpValue> flagResults = SnmpUtils.getOidValues(agentConfig, "DiskUsagePoller", hrStorageDescrSnmpObject);
            
            if(flagResults.size() == 0) {
                log().debug("SNMP poll failed: no results, addr=" + ipaddr.getHostAddress() + " oid=" + hrStorageDescrSnmpObject);
                return PollStatus.unavailable();
            }

            for (Map.Entry<SnmpInstId, SnmpValue> e : flagResults.entrySet()) { 
                log().debug("poll: SNMPwalk poll succeeded, addr=" + ipaddr.getHostAddress() + " oid=" + hrStorageDescrSnmpObject + " instance=" + e.getKey() + " value=" + e.getValue());
                
                if (isMatch(e.getValue().toString(), diskName, matchType)) {
                	log().debug("DiskUsageMonitor.poll: found disk=" + diskName);
                	
                	SnmpObjId hrStorageSizeSnmpObject = SnmpObjId.get(hrStorageSize + "." + e.getKey().toString());
                	SnmpObjId hrStorageUsedSnmpObject = SnmpObjId.get(hrStorageUsed + "." + e.getKey().toString());
                	
                	
                	SnmpValue snmpSize = SnmpUtils.get(agentConfig, hrStorageSizeSnmpObject);
                	SnmpValue snmpUsed = SnmpUtils.get(agentConfig, hrStorageUsedSnmpObject);
                	float calculatedPercentage = ( (( (float)snmpSize.toLong() - (float)snmpUsed.toLong() ) / (float)snmpSize.toLong() ) ) * 100;
                
                	log().debug("DiskUsageMonitor: calculatedPercentage=" + calculatedPercentage + " percentFree="+percentFree);
                	
                	if (calculatedPercentage < percentFree) {
                	
                		return PollStatus.unavailable(diskName + " usage high (" + (100 - (int)calculatedPercentage)  + "%)");
                		
                	}
                	else {
                		return status;
                	}
                }
            
                 
            }

            // if we get here.. it means we did not find the disk...  which means we should not be monitoring it.
            log().debug("DiskUsageMonitor: no disks found");
            return PollStatus.unavailable("could not find " + diskName + "in table");
            
            
        } catch (NumberFormatException e) {
            status = logDown(Level.ERROR, "Number operator used on a non-number " + e.getMessage());
        } catch (IllegalArgumentException e) {
            status = logDown(Level.ERROR, "Invalid Snmp Criteria: " + e.getMessage());
        } catch (Throwable t) {
            status = logDown(Level.WARN, "Unexpected exception during SNMP poll of interface " + ipaddr.getHostAddress(), t);
        }

        return status;
    }
    
    private boolean isMatch(String candidate, String target, int matchType) {
        boolean matches = false;
        log().debug("isMessage: candidate is '" + candidate + "', matching against target '" + target + "'");
        if (matchType == MATCH_TYPE_EXACT) {
            log().debug("Attempting equality match: candidate '" + candidate + "', target '" + target + "'");
            matches = candidate.equals(target);
        } else if (matchType == MATCH_TYPE_STARTSWITH) {
            log().debug("Attempting startsWith match: candidate '" + candidate + "', target '" + target + "'");
            matches = candidate.startsWith(target);
        } else if (matchType == MATCH_TYPE_ENDSWITH) {
            log().debug("Attempting endsWith match: candidate '" + candidate + "', target '" + target + "'");
            matches = candidate.endsWith(target);
        } else if (matchType == MATCH_TYPE_REGEX) {
            log().debug("Attempting endsWith match: candidate '" + candidate + "', target '" + target + "'");
            matches = Pattern.compile(target).matcher(candidate).find();
        }
        log().debug("isMatch: Match is positive");
        return matches;
    }
}
