/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.proxy;

import java.util.List;

import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * Asynchronous SNMP client API that either executes the request locally, delegating
 * the request to the current {@link org.opennms.netmgt.snmp.SnmpStrategy}, or dispatches
 * the request to a Minion at the given location.
 *
 * @author jwhite
 */
public interface LocationAwareSnmpClient {

    SNMPRequestBuilder<List<SnmpResult>> walk(SnmpAgentConfig agent, String... oids);

    SNMPRequestBuilder<List<SnmpResult>> walk(SnmpAgentConfig agent, SnmpObjId... oids);

    SNMPRequestBuilder<List<SnmpResult>> walk(SnmpAgentConfig agent, List<SnmpObjId> oids);

    <T extends CollectionTracker> SNMPRequestBuilder<T> walk(SnmpAgentConfig agent, T tracker);

    SNMPRequestBuilder<SnmpValue> get(SnmpAgentConfig agent, String oid);

    SNMPRequestBuilder<SnmpValue> get(SnmpAgentConfig agent, SnmpObjId oid);

    SNMPRequestBuilder<List<SnmpValue>> get(SnmpAgentConfig agent, String... oids);

    SNMPRequestBuilder<List<SnmpValue>> get(SnmpAgentConfig agent, SnmpObjId... oids);

    SNMPRequestBuilder<List<SnmpValue>> get(SnmpAgentConfig agent, List<SnmpObjId> oids);
}
