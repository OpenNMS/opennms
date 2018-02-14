/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.snmp.snmp4j;

import org.opennms.netmgt.alarmd.northbounder.snmptrap.TrapData;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier.Snmp4JV1TrapInformation;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier.Snmp4JV2TrapInformation;
import org.snmp4j.smi.VariableBinding;

/**
 * The Class TrapUtils.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class TrapUtils {

    /**
     * Gets the trap identity.
     *
     * @param trapNotification the trap notification
     * @return the identity
     */
    public static TrapData getTrapData(TrapInformation trapNotification) {
        if (trapNotification instanceof Snmp4JV1TrapInformation) {
            Snmp4JV1TrapInformation info = (Snmp4JV1TrapInformation) trapNotification;
            TrapIdentity identity = info.getTrapIdentity();
            TrapData data = new TrapData(identity.getEnterpriseId(), identity.getGeneric(), identity.getSpecific());
            for (int i=0; i < info.getPduLength(); i++) {
                VariableBinding v = info.getVarBindAt(i);
                SnmpValue value = new Snmp4JValue(v.getVariable());
                data.addParameter("." + v.getOid().toString(), value.toDisplayString());
            }
            return data;
        } else if (trapNotification instanceof Snmp4JV2TrapInformation) {
            Snmp4JV2TrapInformation info = (Snmp4JV2TrapInformation) trapNotification;
            TrapIdentity identity = info.getTrapIdentity();
            TrapData data = new TrapData(identity.getEnterpriseId(), identity.getGeneric(), identity.getSpecific());
            for (int i=0; i < info.getPduLength(); i++) {
                VariableBinding v = info.getVarBindAt(i);
                SnmpValue value = new Snmp4JValue(v.getVariable());
                data.addParameter("." + v.getOid().toString(), value.toDisplayString());
            }
            return data;
        }
        return null;
    }

}
