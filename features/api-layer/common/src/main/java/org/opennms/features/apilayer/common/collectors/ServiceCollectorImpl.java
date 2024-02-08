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
package org.opennms.features.apilayer.common.collectors;

import java.net.InetAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.opennms.integration.api.v1.collectors.CollectionRequest;
import org.opennms.integration.api.v1.collectors.CollectionSet;
import org.opennms.integration.api.v1.collectors.ServiceCollector;
import org.opennms.integration.api.v1.collectors.ServiceCollectorFactory;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceCollectorImpl<T extends ServiceCollector> implements org.opennms.netmgt.collection.api.ServiceCollector {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceCollectorImpl.class);

    private final ServiceCollectorFactory<T> serviceCollectorFactory;
    private final RrdRepository rrdRepo;


    public ServiceCollectorImpl(ServiceCollectorFactory<T> serviceCollectorFactory, RrdRepository rrdRepo) {
        this.serviceCollectorFactory = serviceCollectorFactory;
        this.rrdRepo = rrdRepo;
    }

    @Override
    public void initialize() throws CollectionInitializationException {
        // initialize would be called in collect method.
    }

    @Override
    public void validateAgent(org.opennms.netmgt.collection.api.CollectionAgent agent, Map<String, Object> parameters) {
        // not implemented in Integration API
    }

    @Override
    public org.opennms.netmgt.collection.api.CollectionSet collect(org.opennms.netmgt.collection.api.CollectionAgent agent, Map<String, Object> parameters) throws CollectionException {
        // Instantiate a new collector each time there is a call to collect.
        ServiceCollector serviceCollector = serviceCollectorFactory.createCollector();
        serviceCollector.initialize();
        try {
            // build collection request for service collector
            CollectionRequest collectionRequest = new CollectionRequestImpl(agent);
            // Call collect on Service collector that implements integration api.
            CompletableFuture<CollectionSet> future = serviceCollector.collect(collectionRequest, parameters);
            CollectionSetBuilder builder = new CollectionSetBuilder(agent);
            CollectionSet collectionSet = future.get();
            if (collectionSet.getStatus().equals(CollectionSet.Status.FAILED)) {
                return  builder.withTimestamp(new Date(collectionSet.getTimeStamp()))
                        .withStatus(CollectionStatus.FAILED).build();
            }
            // Map CollectionSet from Integration API and build CollectionSet.
            return CollectionSetMapper.buildCollectionSet(builder, collectionSet);
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Collection failed", e);
            throw new CollectionException("Collection failed", e);
        }
    }


    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        return rrdRepo;
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(org.opennms.netmgt.collection.api.CollectionAgent agent, Map<String, Object> parameters) {
        return serviceCollectorFactory.getRuntimeAttributes(new CollectionRequestImpl(agent), parameters);
    }

    @Override
    public String getEffectiveLocation(String location) {
        return null;
    }

    @Override
    public Map<String, String> marshalParameters(Map<String, Object> parameters) {
        return serviceCollectorFactory.marshalParameters(parameters);
    }

    @Override
    public Map<String, Object> unmarshalParameters(Map<String, String> parameters) {
        return serviceCollectorFactory.unmarshalParameters(parameters);
    }

    private class CollectionRequestImpl implements CollectionRequest {

        private org.opennms.netmgt.collection.api.CollectionAgent collectionAgent;

        public CollectionRequestImpl(org.opennms.netmgt.collection.api.CollectionAgent collectionAgent) {
           this.collectionAgent = collectionAgent;
        }

        @Override
        public InetAddress getAddress() {
            return collectionAgent.getAddress();
        }

        @Override
        public int getNodeId() {
            return collectionAgent.getNodeId();
        }

    }
}
