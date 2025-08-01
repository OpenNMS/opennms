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
package org.opennms.netmgt.snmp.snmp4j;

import org.opennms.netmgt.alarmd.northbounder.snmptrap.TrapData;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier.Snmp4JV1TrapInformation;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier.Snmp4JV2V3TrapInformation;
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
        } else if (trapNotification instanceof Snmp4JV2V3TrapInformation) {
            Snmp4JV2V3TrapInformation info = (Snmp4JV2V3TrapInformation) trapNotification;
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
