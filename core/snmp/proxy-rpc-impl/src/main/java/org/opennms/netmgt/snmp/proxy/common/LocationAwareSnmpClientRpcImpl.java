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
package org.opennms.netmgt.snmp.proxy.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmp.proxy.SNMPRequestBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Location-aware SNMP client that builds a {@link SnmpRequestDTO} and delegates
 * the request to either a local, or a remote @{link SnmpRequestExecutor}.
 *
 * @author jwhite
 */
public class LocationAwareSnmpClientRpcImpl implements LocationAwareSnmpClient, InitializingBean {

    @Autowired
    private RpcClientFactory rpcClientFactory;

    private RpcClient<SnmpRequestDTO, SnmpMultiResponseDTO> delegate;

    public LocationAwareSnmpClientRpcImpl() { }

    public LocationAwareSnmpClientRpcImpl(RpcClientFactory rpcClientFactory) {
        this.rpcClientFactory = Objects.requireNonNull(rpcClientFactory);
        afterPropertiesSet();
    }

    @Override
    public void afterPropertiesSet() {
        delegate = rpcClientFactory.getClient(SnmpProxyRpcModule.INSTANCE);
    }

    @Override
    public SNMPRequestBuilder<List<SnmpResult>> walk(SnmpAgentConfig agent, String... oids) {
        final List<SnmpObjId> snmpObjIds = Arrays.stream(oids)
                .map(SnmpObjId::get)
                .collect(Collectors.toList());
        return walk(agent, snmpObjIds);
    }

    @Override
    public SNMPRequestBuilder<List<SnmpResult>> walk(SnmpAgentConfig agent, SnmpObjId... oids) {
        return walk(agent, Arrays.asList(oids));
    }

    @Override
    public SNMPRequestBuilder<List<SnmpResult>> walk(SnmpAgentConfig agent, List<SnmpObjId> oids) {
        return new SNMPWalkBuilder(this, agent, oids);
    }

    @Override
    public SNMPRequestBuilder<CollectionTracker> walk(SnmpAgentConfig agent, CollectionTracker tracker) {
        return new SNMPWalkWithTrackerBuilder(this, agent, tracker);
    }

    @Override
    public SNMPRequestBuilder<SnmpValue> get(SnmpAgentConfig agent, String oid) {
        return get(agent, SnmpObjId.get(oid));
    }

    @Override
    public SNMPRequestBuilder<SnmpValue> get(SnmpAgentConfig agent, SnmpObjId oid) {
        return new SNMPSingleGetBuilder(this, agent, oid);
    }

    @Override
    public SNMPRequestBuilder<List<SnmpValue>> get(SnmpAgentConfig agent, String... oids) {
        final List<SnmpObjId> snmpObjIds = Arrays.stream(oids)
                .map(SnmpObjId::get)
                .collect(Collectors.toList());
        return get(agent, snmpObjIds);
    }

    @Override
    public SNMPRequestBuilder<List<SnmpValue>> get(SnmpAgentConfig agent, SnmpObjId... oids) {
        return get(agent, Arrays.asList(oids));
    }

    @Override
    public SNMPRequestBuilder<List<SnmpValue>> get(SnmpAgentConfig agent, List<SnmpObjId> oids) {
        return new SNMPMultiGetBuilder(this, agent, oids);
    }

    @Override
    public SNMPRequestBuilder<SnmpValue> set(SnmpAgentConfig agent, List<SnmpObjId> oids, List<SnmpValue> values) {
        return new SNMPSetBuilder(this, agent, oids, values);
    }

    public CompletableFuture<SnmpMultiResponseDTO> execute(SnmpRequestDTO request) {
        return delegate.execute(request);
    }
}
