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
package org.opennms.core.daemon.loader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmp.proxy.SNMPRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A local implementation of {@link LocationAwareSnmpClient} for standalone
 * daemon containers that don't have the RPC infrastructure. Delegates to
 * {@link SnmpUtils} for all SNMP operations (local execution only, location
 * parameter is ignored).
 */
public class LocalSnmpClient implements LocationAwareSnmpClient {

    private static final Logger LOG = LoggerFactory.getLogger(LocalSnmpClient.class);

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
        return new LocalSNMPRequestBuilder<>(() -> {
            final List<SnmpResult> results = new java.util.ArrayList<>();
            CollectionTracker[] trackers = oids.stream()
                    .map(oid -> (CollectionTracker) new ResultCapturingColumnTracker(oid, results))
                    .toArray(CollectionTracker[]::new);
            try (SnmpWalker walker = SnmpUtils.createWalker(agent, "localWalk", trackers)) {
                walker.start();
                walker.waitFor();
            }
            return results;
        });
    }

    @Override
    public <T extends CollectionTracker> SNMPRequestBuilder<T> walk(SnmpAgentConfig agent, T tracker) {
        return new LocalSNMPRequestBuilder<>(() -> {
            try (SnmpWalker walker = SnmpUtils.createWalker(agent, "localWalk", tracker)) {
                walker.start();
                walker.waitFor();
            }
            return tracker;
        });
    }

    @Override
    public SNMPRequestBuilder<SnmpValue> get(SnmpAgentConfig agent, String oid) {
        return get(agent, SnmpObjId.get(oid));
    }

    @Override
    public SNMPRequestBuilder<SnmpValue> get(SnmpAgentConfig agent, SnmpObjId oid) {
        return new LocalSNMPRequestBuilder<>(() -> SnmpUtils.get(agent, oid));
    }

    @Override
    public SNMPRequestBuilder<List<SnmpValue>> get(SnmpAgentConfig agent, String... oids) {
        final SnmpObjId[] snmpObjIds = Arrays.stream(oids)
                .map(SnmpObjId::get)
                .toArray(SnmpObjId[]::new);
        return get(agent, snmpObjIds);
    }

    @Override
    public SNMPRequestBuilder<List<SnmpValue>> get(SnmpAgentConfig agent, SnmpObjId... oids) {
        return get(agent, Arrays.asList(oids));
    }

    @Override
    public SNMPRequestBuilder<List<SnmpValue>> get(SnmpAgentConfig agent, List<SnmpObjId> oids) {
        return new LocalSNMPRequestBuilder<>(() -> {
            SnmpValue[] values = SnmpUtils.get(agent, oids.toArray(new SnmpObjId[0]));
            return values != null ? Arrays.asList(values) : Collections.emptyList();
        });
    }

    @Override
    public SNMPRequestBuilder<SnmpValue> set(SnmpAgentConfig agent, List<SnmpObjId> oids, List<SnmpValue> values) {
        return new LocalSNMPRequestBuilder<>(() -> {
            if (oids.size() == 1) {
                return SnmpUtils.set(agent, oids.get(0), values.get(0));
            }
            SnmpValue[] result = SnmpUtils.set(agent, oids.toArray(new SnmpObjId[0]), values.toArray(new SnmpValue[0]));
            return (result != null && result.length > 0) ? result[0] : null;
        });
    }

    /**
     * A ColumnTracker that captures results in an external list as they are received.
     */
    private static class ResultCapturingColumnTracker extends org.opennms.netmgt.snmp.ColumnTracker {
        private final List<SnmpResult> results;

        ResultCapturingColumnTracker(SnmpObjId base, List<SnmpResult> results) {
            super(base);
            this.results = results;
        }

        @Override
        protected void storeResult(SnmpResult res) {
            super.storeResult(res);
            results.add(res);
        }
    }

    /**
     * A simple local SNMPRequestBuilder that ignores location/systemId and
     * executes SNMP operations directly via SnmpUtils.
     */
    private static class LocalSNMPRequestBuilder<T> implements SNMPRequestBuilder<T> {
        private final java.util.concurrent.Callable<T> operation;

        LocalSNMPRequestBuilder(java.util.concurrent.Callable<T> operation) {
            this.operation = operation;
        }

        @Override
        public SNMPRequestBuilder<T> withLocation(String location) {
            return this;
        }

        @Override
        public SNMPRequestBuilder<T> withSystemId(String systemId) {
            return this;
        }

        @Override
        public SNMPRequestBuilder<T> withDescription(String string) {
            return this;
        }

        @Override
        public SNMPRequestBuilder<T> withTimeToLive(Long ttlInMs) {
            return this;
        }

        @Override
        public SNMPRequestBuilder<T> withTimeToLive(long duration, TimeUnit unit) {
            return this;
        }

        @Override
        public CompletableFuture<T> execute() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return operation.call();
                } catch (Exception e) {
                    throw new java.util.concurrent.CompletionException(e);
                }
            });
        }
    }
}
