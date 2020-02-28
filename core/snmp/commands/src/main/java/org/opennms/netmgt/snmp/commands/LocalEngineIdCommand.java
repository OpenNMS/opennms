/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.snmp4j.smi.OctetString;


@Command(scope = "snmp", name = "local-engine-id", description = "Display the Local Engine Id used for Traps/Informs")
@Service
public class LocalEngineIdCommand implements Action {


    @Reference
    private SnmpStrategy snmpStrategy;

    @Override
    public Object execute() throws Exception {

        if (snmpStrategy != null) {
            byte[] localEngineId = snmpStrategy.getLocalEngineID();
            if (localEngineId != null) {
                OctetString engineIdString = new OctetString(localEngineId);
                System.out.printf("localEngineId = %s \n", engineIdString);
                return engineIdString;
            } else {
                System.out.println("localEngineId is not configured in SnmpStrategy");
            }
        } else {
            System.out.println("SnmpStrategy is not registered");
        }
        return null;
    }
}
