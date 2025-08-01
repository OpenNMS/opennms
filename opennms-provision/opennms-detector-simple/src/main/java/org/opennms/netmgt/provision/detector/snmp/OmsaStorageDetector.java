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


/**
 * <p>OmsaStorageDetector class.</p>
 *
 * @author agalue
 * @version $Id: $
 */

public class OmsaStorageDetector extends SnmpDetector {

    private static final Logger LOG = LoggerFactory.getLogger(OmsaStorageDetector.class);

    /**
     * Name of monitored service.
     */
    private static final String PROTOCOL_NAME = "OMSAStorage";

    private static final String virtualDiskRollUpStatus = ".1.3.6.1.4.1.674.10893.1.20.140.1.1.19";

    /**
     * Virtual Disk Number (defaults to 1)
     */
    private String m_virtualDiskNumber = "1";

    /**
     * <p>Constructor for CiscoIpSlaDetector.</p>
     */
    public OmsaStorageDetector(){
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

            SnmpObjId virtualDiskRollUpStatusSnmpObject = SnmpObjId.get(virtualDiskRollUpStatus + '.' + m_virtualDiskNumber);
            SnmpValue virtualDiskRollUpStatus = SnmpUtils.get(agentConfig, virtualDiskRollUpStatusSnmpObject);

            if (virtualDiskRollUpStatus == null || virtualDiskRollUpStatus.isNull()) {
                LOG.debug("SNMP poll failed: no results, addr={} oid={}", agentConfig.getAddress(), virtualDiskRollUpStatusSnmpObject);
                return false;
            }
            if (virtualDiskRollUpStatus.toInt() != 3) { // 3 means Online
                LOG.debug("OMSAStorageMonitor.poll: Bad Disk Found. Log vol({}) degraded", m_virtualDiskNumber);
                return false;
            }
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
        return true;
    }

    public String getVirtualDiskNumber() {
        return m_virtualDiskNumber;
    }

    public void setVirtualDiskNumber(String virtualDiskNumber) {
        this.m_virtualDiskNumber = virtualDiskNumber;
    }

}
