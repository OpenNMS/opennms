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

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>PercDetector class.</p>
 *
 * @author agalue
 * @version $Id: $
 */
@Scope("prototype")
public class PercDetector extends SnmpDetector {

	private static final Logger LOG = LoggerFactory.getLogger(PercDetector.class);

	/**
     * Name of monitored service.
     */
    private static final String PROTOCOL_NAME = "PERC";

    /**
     * The base OID for the logical device status information
     */
    private static final String LOGICAL_BASE_OID = ".1.3.6.1.4.1.3582.1.1.2.1.3";

    /**
     * PERC Array Number (defaults to 0.0)
     */
    private String m_arrayNumber = "0.0";

    /**
     * <p>Constructor for CiscoIpSlaDetector.</p>
     */
    public PercDetector(){
        setServiceName(PROTOCOL_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If
     * the protocol is not supported then a false value is returned to the
     * caller. The qualifier map passed to the method is used by the plugin to
     * return additional information by key-name. These key-value pairs can be
     * added to service events if needed.
     */
    @Override
    public boolean isServiceDetected(final InetAddress address, final SnmpAgentConfig agentConfig) {
        try {
            configureAgentPTR(agentConfig);
            configureAgentVersion(agentConfig);

            SnmpObjId snmpObjectId = SnmpObjId.get(LOGICAL_BASE_OID + '.' + m_arrayNumber);
            SnmpValue value = SnmpUtils.get(agentConfig, snmpObjectId);
            if (value.toInt() != 2) {
                LOG.debug("PercMonitor.poll: Bad Disk Found. Log vol({}) degraded", m_arrayNumber);
                return false;
            }
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
        return true;
    }

    public String getArrayNumber() {
        return m_arrayNumber;
    }

    public void setArrayNumber(String arrayNumber) {
        this.m_arrayNumber = arrayNumber;
    }

}
