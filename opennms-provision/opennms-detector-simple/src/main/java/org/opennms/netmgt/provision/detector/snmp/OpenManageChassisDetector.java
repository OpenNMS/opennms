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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>OpenManageChassisDetector class.</p>
 * 
 * @author agalue
 * @version $Id: $
 */
@Scope("prototype")
public class OpenManageChassisDetector extends SnmpDetector {

    private static final Logger LOG = LoggerFactory.getLogger(OpenManageChassisDetector.class);

    /**
     * Name of monitored service.
     */
    private static final String PROTOCOL_NAME = "Dell_OpenManageChassis";

    /**
     * This attribute defines the status of this chassis.
     */
    private static final String CHASSIS_STATUS_OID = ".1.3.6.1.4.1.674.10892.1.200.10.1.4.1";

    /**
     * Implement the chassis status
     */
    private enum DELL_STATUS {
        OTHER(1), UNKNOWN(2), OK(3), NON_CRITICAL(4), CRITICAL(5), NON_RECOVERABLE(6);

        private final int state; // state code

        DELL_STATUS(int s) {
            this.state = s;
        }

        private int value() {
            return this.state;
        }
    };

    /**
     * <p>Constructor for CiscoIpSlaDetector.</p>
     */
    public OpenManageChassisDetector(){
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

            // Get the OpenManage chassis status
            String chassisStatus = getValue(agentConfig, CHASSIS_STATUS_OID);

            // If no chassis status received, do not detect the protocol and quit
            if (chassisStatus == null) {
                LOG.warn("isServiceDetected: Cannot receive chassis status");
                return false;
            } else {
                LOG.debug("isServiceDetected: OpenManageChassis: {}", chassisStatus);
            }

            // Validate chassis status, check status is somewhere between OTHER and NON_RECOVERABLE
            if  (Integer.parseInt(chassisStatus.toString()) >= DELL_STATUS.OTHER.value() && 
                    Integer.parseInt(chassisStatus.toString()) <= DELL_STATUS.NON_RECOVERABLE.value()) {
                // OpenManage chassis status detected
                LOG.debug("isServiceDetected: OpenManageChassis: is valid, protocol supported.");
                return true;
            }
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
        return false;
    }

}
