/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
/*
 *
 * OMSAStorageMonitor with per volume support.
 * Jason Aras <jason.aras@gmail.com>
 *
 */


package org.opennms.netmgt.poller.monitors;


import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
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
final public class OmsaStorageMonitor extends SnmpMonitorStrategy {
    
    public static final Logger LOG = LoggerFactory.getLogger(OmsaStorageMonitor.class);

    private static final String virtualDiskRollUpStatus = ".1.3.6.1.4.1.674.10893.1.20.140.1.1.19";
    private static final String arrayDiskLogicalConnectionVirtualDiskNumber = ".1.3.6.1.4.1.674.10893.1.20.140.3.1.5";
    private static final String arrayDiskNexusID = ".1.3.6.1.4.1.674.10893.1.20.130.4.1.26";
    private static final String arrayDiskLogicalConnectionArrayDiskNumber =".1.3.6.1.4.1.674.10893.1.20.140.3.1.3";
    private static final String arrayDiskState=".1.3.6.1.4.1.674.10893.1.20.130.4.1.4";

    /** {@inheritDoc} */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        PollStatus status = PollStatus.available();
        InetAddress ipaddr = svc.getAddress();

        final StringBuilder returnValue = new StringBuilder();
        
        SnmpAgentConfig agentConfig = configureAgent(svc, parameters);

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

	private SnmpAgentConfig configureAgent(MonitoredService svc, Map<String, Object> parameters) {
        // Retrieve this interface's SNMP peer object
        //
	    final SnmpAgentConfig agentConfig = getAgentConfig(svc, parameters);
        LOG.debug("poll: setting SNMP peer attribute for interface {}", InetAddressUtils.str(svc.getAddress()));
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
