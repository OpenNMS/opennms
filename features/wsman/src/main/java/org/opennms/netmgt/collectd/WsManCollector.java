/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.core.wsman.exceptions.InvalidResourceURI;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.utils.ResponseHandlingUtils;
import org.opennms.core.wsman.utils.RetryNTimesLoop;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.GenericTypeResourceWithoutInstance;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.wsman.Attrib;
import org.opennms.netmgt.config.wsman.Collection;
import org.opennms.netmgt.config.wsman.Group;
import org.opennms.netmgt.config.wsman.WsmanAgentConfig;
import org.opennms.netmgt.config.wsman.WsmanDatacollectionConfig;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.dao.WSManDataCollectionConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

/**
 * WS-Man Collector
 *
 * @author jwhite
 */
public class WsManCollector implements ServiceCollector {
    private static final Logger LOG = LoggerFactory.getLogger(WsManCollector.class);

    private WSManClientFactory m_factory = new CXFWSManClientFactory();

    private WSManDataCollectionConfigDao m_wsManDataCollectionConfigDao;

    private WSManConfigDao m_wsManConfigDao;

    private ResourceTypesDao m_resourceTypesDao;

    private NodeDao m_nodeDao;

    @Override
    public void initialize(Map<String, String> parameters) throws CollectionInitializationException {
        LOG.debug("initialize({})", parameters);
        // Retrieve the configuration DAOs
        m_wsManConfigDao = BeanUtils.getBean("daoContext", "wsManConfigDao", WSManConfigDao.class);
        m_wsManDataCollectionConfigDao = BeanUtils.getBean("daoContext", "wsManDataCollectionConfigDao", WSManDataCollectionConfigDao.class);
        m_resourceTypesDao = BeanUtils.getBean("daoContext", "resourceTypesDao", ResourceTypesDao.class);
        m_nodeDao = BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class);
    }

    @Override
    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> parameters) throws CollectionException {
        LOG.debug("collect({}, {}, {})", agent, eproxy, parameters);

        final Object collectionNameParam = parameters.get("collection");
        if (collectionNameParam == null || !(collectionNameParam instanceof String)) {
            throw new CollectionException("Collector configuration does not include the required 'collection' parameter.");
        }

        final String collectionName = (String)collectionNameParam;

        final Collection collection = m_wsManDataCollectionConfigDao.getCollectionByName(collectionName);
        if (collection == null) {
            throw new CollectionException("No collection found with name: " + collectionName);
        }

        final OnmsNode node = m_nodeDao.get(agent.getNodeId());
        if (node == null) {
            throw new CollectionException("Could not find node with id: " + agent.getNodeId());
        }

        final WsmanAgentConfig config = m_wsManConfigDao.getConfig(agent.getAddress());
        final WSManEndpoint endpoint = m_wsManConfigDao.getEndpoint(config, agent.getAddress());
        final WSManClient client = m_factory.getClient(endpoint);
        final CollectionSetBuilder collectionSetBuilder = new CollectionSetBuilder(agent);
        final List<Group> groups = m_wsManDataCollectionConfigDao.getGroupsForAgent(collection, agent, config, node);

        if (LOG.isDebugEnabled()) {
            String groupNames = groups.stream().map(g -> g.getName()).collect(Collectors.joining(", "));
            LOG.debug("Collecting attributes on {} from groups: {}", agent, groupNames);
        }

        for (Group group : groups) {
            try {
                collectGroupUsing(group, agent, client, config.getRetry() != null ? config.getRetry() : 0, collectionSetBuilder);
            } catch (InvalidResourceURI e) {
                LOG.info("Resource URI {} in group named {} is not available on {}.", group.getResourceUri(), group.getName(), agent);
            } catch (WSManException e) {
                // If collecting any individual group fails, mark the collection set as
                // failed, and abort trying to collect any other groups
                throw new CollectionException(String.format("Collecting group '%s' on %s failed with '%s'. See logs for details.",
                        group.getName(), agent, e.getMessage()), e);
            }
        }

        return collectionSetBuilder.build();
    }

    private void collectGroupUsing(Group group, CollectionAgent agent, WSManClient client, int retries, CollectionSetBuilder builder) throws CollectionException {
        // Determine the appropriate resource type
        final NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());
        Supplier<Resource> resourceSupplier = () -> nodeResource;
        if (!"node".equalsIgnoreCase(group.getResourceType())) {
            final ResourceType resourceType = m_resourceTypesDao.getResourceTypeByName(group.getResourceType());
            if (resourceType == null) {
                throw new CollectionException("No resource type found with name '" + group.getResourceType() + "'.");
            }
            resourceSupplier = () -> new GenericTypeResourceWithoutInstance(nodeResource, resourceType);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using resource {} for group named {}", resourceSupplier.get(), group.getName());
        }

        // Enumerate
        List<Node> nodes = Lists.newLinkedList();
        RetryNTimesLoop retryLoop = new RetryNTimesLoop(retries);
        while (retryLoop.shouldContinue()) {
            try {
                if (group.getFilter() == null) {
                    LOG.debug("Enumerating and pulling {} on {}.", group.getResourceUri(), client);
                    client.enumerateAndPull(group.getResourceUri(), nodes, true);
                } else {
                    LOG.debug("Enumerating and pulling {} with dialect {} and filter {} on {}.", group.getResourceUri(),
                            group.getDialect(), group.getFilter(), client);
                    client.enumerateAndPullUsingFilter(group.getResourceUri(), group.getDialect(), group.getFilter(), nodes, true);
                }
                break;
            } catch (WSManException e) {
                retryLoop.takeException(e);
            }
        }
        LOG.debug("Found {} nodes.", nodes.size());

        // Process the results
        processEnumerationResults(group, builder, resourceSupplier, nodes);
    }

    /**
     * Used to build a {@link CollectionSet} from the enumeration results.
     */
    public static void processEnumerationResults(Group group, CollectionSetBuilder builder, Supplier<Resource> resourceSupplier, List<Node> nodes) {
        for (Node node : nodes) {
            // Call the resource supplier for every node process, this may create a new
            // resource, or use the instance that was last returned when processing this group
            final Resource resource = resourceSupplier.get();
            final ListMultimap<String, String> elementValues = ResponseHandlingUtils.toMultiMap(node);
            LOG.debug("Element values: {}", elementValues);

            // Associate the values with the configured attributes
            for (Attrib attrib : group.getAttrib()) {
                if (attrib.getFilter() != null && !ResponseHandlingUtils.matchesFilter(attrib.getFilter(), elementValues)) {
                    continue;
                }

                String valueAsString = null;
                final List<String> attributeValues = elementValues.get(attrib.getName());
                if (attributeValues.size() > 1 && attrib.getIndexOf() != null) {
                    try {
                        int index = ResponseHandlingUtils.getMatchingIndex(attrib.getIndexOf(), elementValues);
                        valueAsString = attributeValues.get(index);
                    } catch (NoSuchElementException e) {
                        LOG.warn("No index was matched by index-of rule '{}' for attribute {} with values: {}.",
                                attrib.getIndexOf(), attrib.getName(), elementValues);
                    }
                } else {
                    // Grab the first value, defaulting to null is there are no values
                    valueAsString = Iterables.getFirst(elementValues.get(attrib.getName()), null);
                }

                if (valueAsString == null) {
                    LOG.warn("No value found for attribute: {} in group: {}", attrib.getName(), group.getName());
                    continue;
                }

                builder.withAttribute(resource, group.getName(), attrib.getAlias(), valueAsString, attrib.getType());
            }
        }
    }

    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        LOG.debug("getRrdRepository({})", collectionName);

        WsmanDatacollectionConfig config = m_wsManDataCollectionConfigDao.getConfig();
        Collection collection = m_wsManDataCollectionConfigDao.getCollectionByName(collectionName);
        if (collection == null) {
            throw new IllegalArgumentException("No configuration found for collection with name: " + collectionName);
        }

        RrdRepository rrdRepository = new RrdRepository();
        rrdRepository.setStep(collection.getRrd().getStep());
        rrdRepository.setHeartBeat(2 * rrdRepository.getStep());
        rrdRepository.setRraList(collection.getRrd().getRra());
        rrdRepository.setRrdBaseDir(new File(config.getRrdRepository()));

        LOG.debug("Using RRD repository: {} for collection: {}", rrdRepository, collectionName);
        return rrdRepository;
    }

    @Override
    public void release() {
        // pass
    }

    @Override
    public void initialize(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException {
        // pass
    }

    @Override
    public void release(CollectionAgent agent) {
        // pass
    }

    public void setWSManConfigDao(WSManConfigDao wsManConfigDao) {
        m_wsManConfigDao = Objects.requireNonNull(wsManConfigDao);
    }

    public void setWSManDataCollectionConfigDao(WSManDataCollectionConfigDao wsManDataCollectionConfigDao) {
        m_wsManDataCollectionConfigDao = Objects.requireNonNull(wsManDataCollectionConfigDao);
    }

    public void setWSManClientFactory(WSManClientFactory factory) {
        m_factory = Objects.requireNonNull(factory);
    }

    public void setResourceTypesDao(ResourceTypesDao resourceTypesDao) {
        m_resourceTypesDao = Objects.requireNonNull(resourceTypesDao);
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = Objects.requireNonNull(nodeDao);
    }
}
