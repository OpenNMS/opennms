/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.opennms.netmgt.snmp.TrapProcessorFactory;

/**
 * A factory for creating TrapProcessor objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class NullTrapProcessorFactory implements TrapProcessorFactory {

    /* (non-Javadoc)
     * @see org.opennms.netmgt.snmp.TrapProcessorFactory#createTrapProcessor()
     */
    @Override
    public TrapProcessor createTrapProcessor() {
        return new TrapProcessor() {

            @Override
            public void setCommunity(String community) {
            }

            @Override
            public void setTimeStamp(long timeStamp) {
            }

            @Override
            public void setVersion(String version) {
            }

            @Override
            public void setAgentAddress(InetAddress agentAddress) {
            }

            @Override
            public void processVarBind(SnmpObjId name, SnmpValue value) {
            }

            @Override
            public void setTrapAddress(InetAddress trapAddress) {
            }

            @Override
            public void setTrapIdentity(TrapIdentity trapIdentity) {
            }

        };
    }

}
