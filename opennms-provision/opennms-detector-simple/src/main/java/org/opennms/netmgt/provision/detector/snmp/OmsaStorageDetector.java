/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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
 * <p>OmsaStorageDetector class.</p>
 *
 * @author agalue
 * @version $Id: $
 */
@Scope("prototype")
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
    public boolean isServiceDetected(InetAddress address) {
        try {
            SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(address);
            configureAgentPTR(agentConfig);
            configureAgentVersion(agentConfig);

            SnmpObjId virtualDiskRollUpStatusSnmpObject = SnmpObjId.get(virtualDiskRollUpStatus + '.' + m_virtualDiskNumber);
            SnmpValue virtualDiskRollUpStatus = SnmpUtils.get(agentConfig, virtualDiskRollUpStatusSnmpObject);

            if (virtualDiskRollUpStatus == null || virtualDiskRollUpStatus.isNull()) {
                LOG.debug("SNMP poll failed: no results, addr={} oid={}", address, virtualDiskRollUpStatusSnmpObject);
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
