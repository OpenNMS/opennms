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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.opennms.integration.api.v1.collectors.CollectionRequest;
import org.opennms.integration.api.v1.collectors.CollectionSet;
import org.opennms.integration.api.v1.collectors.ServiceCollector;
import org.opennms.integration.api.v1.collectors.ServiceCollectorFactory;
import org.opennms.integration.api.v1.collectors.resource.CollectionSetResource;
import org.opennms.integration.api.v1.collectors.resource.GenericTypeResource;
import org.opennms.integration.api.v1.collectors.resource.IpInterfaceResource;
import org.opennms.integration.api.v1.collectors.resource.NodeResource;
import org.opennms.integration.api.v1.collectors.resource.NumericAttribute;
import org.opennms.integration.api.v1.collectors.resource.Resource;
import org.opennms.integration.api.v1.collectors.resource.StringAttribute;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Enums;

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
        ServiceCollector serviceCollector = serviceCollectorFactory.createCollector();
        serviceCollector.initialize();
        CollectionRequest collectionRequest = new CollectionRequestImpl(agent);
        CompletableFuture<CollectionSet> future = serviceCollector.collect(collectionRequest, parameters);
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        try {
            CollectionSet collectionSet = future.get();
            if (collectionSet.getStatus().equals(CollectionSet.Status.FAILED)) {
                return  builder.withTimestamp(new Date(collectionSet.getTimeStamp()))
                        .withStatus(CollectionStatus.FAILED).build();
            }
            for (CollectionSetResource collectionSetResource : collectionSet.getCollectionSetResources()) {
                Resource resource = collectionSetResource.getResource();
                if (resource == null) {
                    continue;
                }
                if (resource.getResourceType().equals(Resource.Type.NODE)) {
                    NodeResource nodeResource = (NodeResource) resource;
                    NodeLevelResource nodeLevelResource = new NodeLevelResource(nodeResource.getNodeId());
                    addAttributes(collectionSetResource, builder, nodeLevelResource);
                } else if (resource.getResourceType().equals(Resource.Type.INTERFACE)) {
                    IpInterfaceResource ipResource = (IpInterfaceResource) resource;
                    NodeLevelResource nodeLevelResource = new NodeLevelResource(ipResource.getNodeResource().getNodeId());
                    InterfaceLevelResource interfaceLevelResource = new InterfaceLevelResource(nodeLevelResource, ipResource.getInstance());
                    addAttributes(collectionSetResource, builder, interfaceLevelResource);
                } else if (resource.getResourceType().equals(Resource.Type.GENERIC)) {
                    GenericTypeResource genericTypeResource = (GenericTypeResource) resource;
                    NodeLevelResource nodeLevelResource = new NodeLevelResource(genericTypeResource.getNodeResource().getNodeId());
                    // TODO: Get ResourceType from ResourceTypesDao
                    org.opennms.netmgt.collection.support.builder.GenericTypeResource genericResource =
                            new org.opennms.netmgt.collection.support.builder.GenericTypeResource(nodeLevelResource, null, genericTypeResource.getInstance());
                    addAttributes(collectionSetResource, builder, genericResource);
                }
            }
            builder.withTimestamp(new Date(collectionSet.getTimeStamp()));
            return builder.build();

        } catch (InterruptedException | ExecutionException e) {
            LOG.error("collect failed with ", e);
        }
        return builder.withStatus(CollectionStatus.UNKNOWN).build();
    }

    public void addAttributes(CollectionSetResource collectionSetResource, CollectionSetBuilder builder, org.opennms.netmgt.collection.support.builder.Resource resource) {
        List<NumericAttribute> numericAttributes = collectionSetResource.getNumericAttributes();
        for (NumericAttribute numericAttribute : numericAttributes) {
            AttributeType attributeType = Enums.getIfPresent(AttributeType.class, numericAttribute.getType().name()).or(AttributeType.GAUGE);
            builder.withNumericAttribute(resource, numericAttribute.getGroup(), numericAttribute.getName(), numericAttribute.getValue(), attributeType);
        }
        List<StringAttribute> stringAttributes = collectionSetResource.getStringAttributes();
        for (StringAttribute stringAttribute : stringAttributes) {
            builder.withStringAttribute(resource, stringAttribute.getGroup(), stringAttribute.getName(), stringAttribute.getValue());
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
