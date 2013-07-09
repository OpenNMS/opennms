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
    public boolean isServiceDetected(InetAddress address) {
        try {
            SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(address);
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
