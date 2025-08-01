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
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * status of PERC raid controllers on Dell Servers. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 * <p>
 * This does SNMP and therefore relies on the SNMP configuration so it is not distributable.
 * </p>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
final public class PercMonitor extends SnmpMonitorStrategy {
    
    
    public static final Logger LOG = LoggerFactory.getLogger(PercMonitor.class);
    
    /**
     * Name of monitored service.
     */
    private static final String SERVICE_NAME = "PERC";

    /**
     * The base OID for the logical device status information
     */
    private static final String LOGICAL_BASE_OID = ".1.3.6.1.4.1.3582.1.1.2.1.3";

    /**
     * The base OID for the physical device status information
     */
    private static final String PHYSICAL_BASE_OID = ".1.3.6.1.4.1.3582.1.1.3.1.4";

    private static final String ARRAY_POSITION_BASE_OID = ".1.3.6.1.4.1.3582.1.1.3.1.5";
    
    /**
     * <P>
     * Returns the name of the service that the plug-in monitors ("SNMP").
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
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     * @exception RuntimeException
     *                Thrown for any uncrecoverable errors.
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        PollStatus status = PollStatus.unavailable();
        InetAddress ipaddr = svc.getAddress();

        // Retrieve this interface's SNMP peer object
        //
        final SnmpAgentConfig agentConfig = getAgentConfig(svc, parameters);
        final String hostAddress = InetAddressUtils.str(ipaddr);
		LOG.debug("poll: setting SNMP peer attribute for interface {}", hostAddress);

        // Get configuration parameters
        //
        // set timeout and retries on SNMP peer object
        //
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));
        
        String arrayNumber = ParameterMap.getKeyedString(parameters,"array","0.0");

        LOG.debug("poll: service= SNMP address= {}", agentConfig);

        // Establish SNMP session with interface
        //
        try {
            LOG.debug("PercMonitor.poll: SnmpAgentConfig address: {}", agentConfig);
            SnmpObjId snmpObjectId = SnmpObjId.get(LOGICAL_BASE_OID + "." + arrayNumber);

            // First walk the physical OID Tree and check the returned values 

            String returnValue = ""; 
          
            SnmpValue value = SnmpUtils.get(agentConfig,snmpObjectId);
            
            if (value.toInt()!=2){
            	LOG.debug("PercMonitor.poll: Bad Disk Found");
            	returnValue = "log vol(" + arrayNumber + ") degraded"; // XXX should degraded be the virtualDiskState ?
            	// array is bad
            	// lets find out which disks are bad in the array
            	
            	// first we need to fetch the arrayPosition table.
            	SnmpObjId arrayPositionSnmpObject = SnmpObjId.get(ARRAY_POSITION_BASE_OID);
            	SnmpObjId diskStatesSnmpObject = SnmpObjId.get(PHYSICAL_BASE_OID); 
            	
            	Map<SnmpInstId,SnmpValue> arrayDisks = SnmpUtils.getOidValues(agentConfig, "PercMonitor", arrayPositionSnmpObject);
            	Map<SnmpInstId,SnmpValue> diskStates = SnmpUtils.getOidValues(agentConfig, "PercMonitor", diskStatesSnmpObject);
            	
            	for (Map.Entry<SnmpInstId, SnmpValue> disk: arrayDisks.entrySet()) {
            		
            		if (disk.getValue().toString().contains("A" + arrayNumber + "-")) {
            			// this is a member of the array
            			
            			if ( diskStates.get(disk.getKey()).toInt() !=3 ){
            				// this is bad disk.
            				
            				returnValue  += "phy drv(" + disk.getKey() + ")";
            				
            			}
            			
            		}
            
            		return PollStatus.unavailable(returnValue);
            	}
            	
            	
            }
        
            status = PollStatus.available();
            

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

}
