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

/*
 *
 * OMSAStorageMonitor with per volume support.
 * Jason Aras <jason.aras@gmail.com>
 *
 */


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
 * <p>OmsaStorageMonitor class.</p>
 * <p>
 * This does SNMP and therefore relies on the SNMP configuration so it is not distributable.
 * </p>
 *
 * @author ranger
 * @version $Id: $
 */
@Distributable(DistributionContext.DAEMON)
final public class OmsaStorageMonitor extends SnmpMonitorStrategy {
    
    public static final Logger LOG = LoggerFactory.getLogger(OmsaStorageMonitor.class);
    
    private static final String m_serviceName = "OMSAStorage";
    
    private static final String virtualDiskRollUpStatus = ".1.3.6.1.4.1.674.10893.1.20.140.1.1.19";
    private static final String arrayDiskLogicalConnectionVirtualDiskNumber = ".1.3.6.1.4.1.674.10893.1.20.140.3.1.5";
    private static final String arrayDiskNexusID = ".1.3.6.1.4.1.674.10893.1.20.130.4.1.26";
    private static final String arrayDiskLogicalConnectionArrayDiskNumber =".1.3.6.1.4.1.674.10893.1.20.140.3.1.3";
    private static final String arrayDiskState=".1.3.6.1.4.1.674.10893.1.20.130.4.1.4";
    
    /**
     * <p>serviceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String serviceName() {
        return m_serviceName;
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(Map<String, Object> parameters) {
        try {
            SnmpPeerFactory.init();
        } catch (IOException ex) {
        	LOG.error("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        return;
    }


    /**
     * <p>initialize</p>
     *
     * @param svc a {@link org.opennms.netmgt.poller.MonitoredService} object.
     */
    @Override
    public void initialize(MonitoredService svc) {
        super.initialize(svc);
        return;
    }

    /** {@inheritDoc} */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();
        
        PollStatus status = PollStatus.available();
        InetAddress ipaddr = (InetAddress) iface.getAddress();
        
        final StringBuffer returnValue = new StringBuffer();
        
        SnmpAgentConfig agentConfig = configureAgent(parameters, iface, ipaddr);

        Integer virtualDiskNumber = ParameterMap.getKeyedInteger(parameters, "virtualDiskNumber", 1);
        
        LOG.debug("poll: service= SNMP address= {}", agentConfig);
        
        final String hostAddress = InetAddressUtils.str(ipaddr);
		try {
            LOG.debug("OMSAStorageMonitor.poll: SnmpAgentConfig address: {}", agentConfig);
            SnmpObjId virtualDiskRollUpStatusSnmpObject = SnmpObjId.get(virtualDiskRollUpStatus + "." + virtualDiskNumber);
            SnmpValue virtualDiskRollUpStatus = SnmpUtils.get(agentConfig, virtualDiskRollUpStatusSnmpObject);
            
            if(virtualDiskRollUpStatus == null || virtualDiskRollUpStatus.isNull()) {
                LOG.debug("SNMP poll failed: no results, addr={} oid={}", hostAddress, virtualDiskRollUpStatusSnmpObject);
                return PollStatus.unavailable();
            }

            if(virtualDiskRollUpStatus.toInt() != 3){
            	// array or one of its components is not happy lets find out which 	
            	
                returnValue.append("log vol(").append(virtualDiskNumber).append(") degraded"); // XXX should degraded be the virtualDiskState ? 
            
            	SnmpObjId arrayDiskLogicalConnectionVirtualDiskNumberSnmpObject = SnmpObjId.get(arrayDiskLogicalConnectionVirtualDiskNumber);	
            	Map<SnmpInstId,SnmpValue> arrayDisks = SnmpUtils.getOidValues(agentConfig, "OMSAStorageMonitor", arrayDiskLogicalConnectionVirtualDiskNumberSnmpObject);
            	
            	SnmpObjId arrayDiskLogicalConnectionArrayDiskNumberSnmpObject = SnmpObjId.get(arrayDiskLogicalConnectionArrayDiskNumber);
    			Map<SnmpInstId,SnmpValue> arrayDiskConnectionNumber = SnmpUtils.getOidValues(agentConfig,"OMSAStorageMonitor", arrayDiskLogicalConnectionArrayDiskNumberSnmpObject);
    			
    			
    			
            	
            	
            	for (Map.Entry<SnmpInstId, SnmpValue> disk: arrayDisks.entrySet()) {
            		
            		
					LOG.debug("OMSAStorageMonitor :: arrayDiskNembers=", disk.getValue());
            		if(disk.getValue().toInt()==virtualDiskNumber){
            			LOG.debug("OMSAStorageMonitor :: Disk Found! ");
            			          					
            			
				LOG.debug("OMSAStorageMonitor :: Found This Array Disk Value {}", disk.getKey());
            			
            			SnmpObjId arrayDiskStateSnmpObject = SnmpObjId.get(arrayDiskState + "." + arrayDiskConnectionNumber.get(disk.getKey()));
            			
            			SnmpValue diskValue = SnmpUtils.get(agentConfig,arrayDiskStateSnmpObject);
            			
				LOG.debug("OmsaStorageMonitor :: Disk State=", diskValue);
            			if(diskValue.toInt() != 3) {
            				
            			String arrayDiskState = getArrayDiskStatus(diskValue);
            			SnmpObjId arrayDiskNexusIDSnmpObject = SnmpObjId.get(arrayDiskNexusID + "." + disk.getKey().toString());
            			SnmpValue nexusValue =  SnmpUtils.get(agentConfig,arrayDiskNexusIDSnmpObject);
            			
            			returnValue.append(" phy drv(").append(nexusValue).append(") ").append(arrayDiskState);
            			}
            			
            		}
            		
				}
            	
            	return PollStatus.unavailable(returnValue.toString());
            	
            }
            
        } catch (NumberFormatException e) {
            String reason = "Number operator used on a non-number " + e.getMessage();
            LOG.debug(reason);
            status = PollStatus.unavailable(reason);
        } catch (IllegalArgumentException e) {
            String reason = "Invalid SNMP Criteria: " + e.getMessage();
            LOG.debug(reason);
            status = PollStatus.unavailable(reason);
        } catch (Throwable t) {
            String reason = "Unexpected exception during SNMP poll of interface " + hostAddress;
            LOG.debug(reason, t);
            status = PollStatus.unavailable(reason);
        }

        return status;
    }

	private SnmpAgentConfig configureAgent(Map<String, Object> parameters, NetworkInterface<InetAddress> iface, InetAddress ipaddr) throws RuntimeException {
        // Retrieve this interface's SNMP peer object
        //
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipaddr);
        if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available for interface " + ipaddr);
        LOG.debug("poll: setting SNMP peer attribute for interface {}", InetAddressUtils.str(ipaddr));
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));
		return agentConfig;
	}

	private String getArrayDiskStatus(SnmpValue diskValue) {
		switch(diskValue.toInt()){
			case 1:  return "Ready";
			case 2:  return "Failed";
			case 3:  return "Online";
			case 4:  return "Offline";	
			case 6:  return "Degraded";  // how does that happen for a disk and not a volume?
			case 7:  return "Recovering";
			case 11: return "Removed";
			case 15: return "Resynching";
			case 24: return "Rebuilding";
			case 25: return "noMedia";
			case 26: return "Formating";
			case 28: return "Running Diagnostics";
		 	case 35: return "Initializing";
			default: break;
		}		
		return null;
	}
}
