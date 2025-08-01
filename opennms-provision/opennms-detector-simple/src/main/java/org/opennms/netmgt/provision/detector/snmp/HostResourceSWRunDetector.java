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
package org.opennms.netmgt.provision.detector.snmp;

import java.net.InetAddress;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>HostResourceSWRunDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class HostResourceSWRunDetector extends SnmpDetector {

    private static final Logger LOG = LoggerFactory.getLogger(HostResourceSWRunDetector.class);

    /**
     * The protocol supported by this detector
     */
    private static final String PROTOCOL_NAME = "HOST-RESOURCES";

    /**
     * Default OID for the table that represents the name of the software running.
     */
    private static final String HOSTRESOURCE_SW_NAME_OID = ".1.3.6.1.2.1.25.4.2.1.2";

    /**
     * Interface attribute key used to store the interface's SnmpAgentConfig
     * object.
     */
    static final String SNMP_AGENTCONFIG_KEY = "org.opennms.netmgt.snmp.SnmpAgentConfig";
    
    private String m_serviceToDetect;
    
    private String m_serviceNameOid;
    /**
     * <p>Constructor for HostResourceSWRunDetector.</p>
     */
    public HostResourceSWRunDetector(){
        setServiceName(PROTOCOL_NAME);
        setServiceNameOid(HOSTRESOURCE_SW_NAME_OID);
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     */
    @Override
    public boolean isServiceDetected(final InetAddress address, final SnmpAgentConfig agentConfig) {
        
        boolean status = false;

        // Get configuration parameters
        //
        
        // This is the string that represents the service name to be monitored.
        String serviceName = getServiceToDetect(); 

        // set timeout and retries on SNMP peer object
        //
        configureAgentPTR(agentConfig);

        LOG.debug("capsd: service= SNMP address={}", agentConfig);

        // Establish SNMP session with interface
        //
        final String hostAddress = InetAddressUtils.str(agentConfig.getAddress());
		try {
            LOG.debug("HostResourceSwRunMonitor.poll: SnmpAgentConfig address: {}", agentConfig);

            if (serviceName == null) {
                LOG.warn("HostResourceSwRunMonitor.poll: No Service Name Defined! ");
                return status;
            }

            // This returns two maps: one of instance and service name, and one of instance and status.
            Map<SnmpInstId, SnmpValue> nameResults = SnmpUtils.getOidValues(agentConfig, "HostResourceSwRunMonitor", SnmpObjId.get(getServiceNameOid()));

            // Iterate over the list of running services
            for(Entry<SnmpInstId, SnmpValue> entry  : nameResults.entrySet()) {
                SnmpValue value = entry.getValue();

                // See if the service name is in the list of running services
                if (match(serviceName, StringUtils.stripExtraQuotes(value.toString())) && !status) {
                    LOG.debug("poll: HostResourceSwRunMonitor poll succeeded, addr={} service name={} value={}", hostAddress, serviceName, value);
                    status = true;
                    break;
                }
            }

        } catch (NumberFormatException e) {
            LOG.warn("Number operator used on a non-number {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid SNMP Criteria: {}", e.getMessage());
        } catch (Throwable t) {
            LOG.warn("Unexpected exception during SNMP poll of interface {}", hostAddress, t);
        }

        return status;
        
    }

    private boolean match(String expectedText, String currentText) {
        if (expectedText.startsWith("~")) {
            return currentText.matches(expectedText.replaceFirst("~", ""));
        }
        return currentText.equalsIgnoreCase(expectedText);
    }

    /**
     * <p>setServiceNameOid</p>
     *
     * @param serviceNameOid a {@link java.lang.String} object.
     */
    public void setServiceNameOid(String serviceNameOid) {
        m_serviceNameOid = serviceNameOid;
    }

    /**
     * <p>getServiceNameOid</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceNameOid() {
        return m_serviceNameOid;
    }

    /**
     * <p>setServiceToDetect</p>
     *
     * @param hostService a {@link java.lang.String} object.
     */
    public void setServiceToDetect(String hostService) {
        m_serviceToDetect = hostService;
    }

    /**
     * <p>getServiceToDetect</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceToDetect() {
        return m_serviceToDetect;
    }
}
