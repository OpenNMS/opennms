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

package org.opennms.netmgt.snmp.snmp4j;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpAgentTimeoutException;
import org.opennms.netmgt.snmp.SnmpException;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

public class Snmp4JWrongResponsesTest {


    @Test
    public void testWrongResponsesHandling() throws IOException, SnmpAgentTimeoutException, SnmpException {
        Snmp4JAgentConfig agentConfig = new Snmp4JAgentConfig(getAgentConfig());
        // Request with 4 oids.
        SnmpObjId[] oids = {SnmpObjId.get(".1.3.6.1.2.1.1.2.0"),
                SnmpObjId.get(".1.3.6.1.2.1.1.3.0"),
                SnmpObjId.get(".1.3.5.1.1.4.0"),
                SnmpObjId.get(".1.3.5.1.1.3.0"),};
        PDU pdu = buildPdu(agentConfig, PDU.GET, oids, null);

        // Response with 2 oids.
        SnmpObjId[] responseOids = {SnmpObjId.get(".1.3.6.1.2.1.1.2.0"),
                SnmpObjId.get(".1.3.5.1.1.3.0")};
        SnmpValue[] values = new SnmpValue[]{
                snmpValue("1st-Element"),
                snmpValue("4th-Element")
        };
        PDU responsePdu = buildPdu(agentConfig, PDU.SET, responseOids, values);
        ResponseEvent responseEvent = new ResponseEvent(this, null, responsePdu, responsePdu, null);

        // Verify response with two oids.
        SnmpValue[] retValues = Snmp4JStrategy.processResponse(agentConfig, responseEvent, pdu);
        assertTrue("All 4 elements should be present", retValues.length == 4);
        assertEquals("1st-Element", retValues[0].toDisplayString());
        assertEquals("4th-Element", retValues[3].toDisplayString());
        assertEquals(SnmpValue.SNMP_NULL, retValues[2].getType());
        assertEquals("", retValues[2].toString());

    }


    protected SnmpAgentConfig getAgentConfig() throws UnknownHostException {
        SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(InetAddress.getLocalHost());
        config.setPort(1234);
        config.setVersion(SnmpAgentConfig.VERSION1);
        return config;
    }

    SnmpValue snmpValue(String val) {
        return new Snmp4JValueFactory().getOctetString(val.getBytes());
    }

    protected static PDU buildPdu(Snmp4JAgentConfig agentConfig, int pduType, SnmpObjId[] oids, SnmpValue[] values) {
        PDU pdu = agentConfig.createPdu(pduType);
        if (values == null) {
            for (SnmpObjId oid : oids) {
                pdu.add(new VariableBinding(new OID(oid.toString())));
            }
        } else {
            // Always assume responses are less than request oids.
            for (int i = 0; i < values.length; i++) {
                pdu.add(new VariableBinding(new OID(oids[i].toString()), new Snmp4JValue(values[i].getType(), values[i].getBytes()).getVariable()));
            }
        }

        return pdu;
    }
}
