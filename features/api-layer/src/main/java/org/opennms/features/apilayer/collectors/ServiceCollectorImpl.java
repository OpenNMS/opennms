/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.collectors;

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


    public ServiceCollectorImpl(ServiceCollectorFactory<T> serviceCollectorFactory) {
        this.serviceCollectorFactory = serviceCollectorFactory;
    }

    @Override
    public void initialize() throws CollectionInitializationException {
        // initialize would be called in collect method.
    }

    @Override
    public void validateAgent(org.opennms.netmgt.collection.api.CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException {
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
        return null;
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(org.opennms.netmgt.collection.api.CollectionAgent agent, Map<String, Object> parameters) {
        return serviceCollectorFactory.getRuntimeAttributes(new CollectionRequestImpl(agent));
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
        public String getNodeCriteria() {
            return String.valueOf(collectionAgent.getNodeId());
        }
    }
}
