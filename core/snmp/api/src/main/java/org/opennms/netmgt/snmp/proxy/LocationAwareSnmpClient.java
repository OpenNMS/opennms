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
package org.opennms.netmgt.snmp.proxy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

import com.google.common.collect.Lists;

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

    SNMPRequestBuilder<SnmpValue> set(SnmpAgentConfig agent, List<SnmpObjId> oids, List<SnmpValue> values);

    default SNMPRequestBuilder<SnmpValue> set(SnmpAgentConfig agent, SnmpObjId oid, SnmpValue value) {
        return set(agent, Collections.singletonList(oid), Collections.singletonList(value));
    }

    default SNMPRequestBuilder<SnmpValue> set(SnmpAgentConfig agent, SnmpObjId[] oids, SnmpValue[] values) {
        return set(agent, Lists.newArrayList(oids), Lists.newArrayList(values));
    }
}
