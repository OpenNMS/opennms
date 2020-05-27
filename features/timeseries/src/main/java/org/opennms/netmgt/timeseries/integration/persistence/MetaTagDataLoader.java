/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.integration.persistence;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.opennms.core.rpc.utils.mate.EmptyScope;
import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.core.rpc.utils.mate.FallbackScope;
import org.opennms.core.rpc.utils.mate.Interpolator;
import org.opennms.core.rpc.utils.mate.ObjectScope;
import org.opennms.core.rpc.utils.mate.Scope;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.timeseries.integration.MetaTagConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.Maps;

public class MetaTagDataLoader extends CacheLoader<CollectionResource, Map<String, String>> {

    private static final Logger LOG = LoggerFactory.getLogger(MetaTagDataLoader.class);

    private final NodeDao nodeDao;
    private final SessionUtils sessionUtils;
    private final EntityScopeProvider entityScopeProvider;
    private MetaTagConfiguration config;

    @Inject
    public MetaTagDataLoader(final NodeDao nodeDao, final SessionUtils sessionUtils, final EntityScopeProvider entityScopeProvider) {
        this.nodeDao = nodeDao;
        this.sessionUtils = sessionUtils;
        this.entityScopeProvider = entityScopeProvider;
        setConfig(new MetaTagConfiguration(Maps.fromProperties(System.getProperties())));
    }

    public void setConfig(final MetaTagConfiguration config) {
        this.config = config;
    }

    public Map<String, String> load(final CollectionResource resource) {
        return sessionUtils.withReadOnlyTransaction(() -> {

            final Map<String, String> tags = new HashMap<>();
            List<Scope> scopes = new ArrayList<>();

            // resource related scope
            scopes.add(getScopeForResource(resource));

            // node related scopes
            String nodeCriteria = getNodeCriteriaFromResource(resource);
            Optional<OnmsNode> nodeOptional = getNode(nodeCriteria);
            if(nodeOptional.isPresent()) {
                OnmsNode node = nodeOptional.get();
                String interfaceAddress = null; // TODO Patrick
                String serviceName = null; // TODO Patrick
                scopes.add(this.entityScopeProvider.getScopeForNode(node.getId()));
                scopes.add(this.entityScopeProvider.getScopeForInterface(node.getId(), interfaceAddress));
                scopes.add(this.entityScopeProvider.getScopeForService(node.getId(), InetAddressUtils.getInetAddress(interfaceAddress), serviceName));
            }

            if (resource.getResourceTypeName().equals(CollectionResource.RESOURCE_TYPE_LATENCY)) {
                // mapResponseTimeResource(resource, tags);
                // TODO: Patrick: check with @Jesse how we get the data
            }

            // create tags for scopes
            Scope scope = new FallbackScope(scopes);
            Map<String, String> configuredMetaTags = this.config.getConfiguredMetaTags();
            for(Map.Entry<String, String> entry: configuredMetaTags.entrySet()) {
                final String value = Interpolator.interpolate(entry.getValue(), scope);
                tags.put(entry.getKey(), value);
            }

            // create tags for categories
            if(nodeOptional.isPresent()) {
                mapCategories(tags, nodeOptional.get());
            }

            return tags;
        });
    }

    private void mapCategories(final Map<String, String> tags, final OnmsNode node) {
        Objects.requireNonNull(node);
        node.getCategories().stream()
                    .map(OnmsCategory::getName)
                    .filter(config::isCategoryEnabled)
                    .forEach(catName -> tags.put("cat_" + catName , catName));
    }

    public Scope getScopeForResource(final CollectionResource resource) {
        if (resource == null) {
            return EmptyScope.EMPTY;
        }
        return new ObjectScope<>(resource)
                .map("resource", "criteria", (r) -> Optional.ofNullable(getNodeCriteriaFromResource(resource)))
                .map("resource", "label", (r) -> Optional.ofNullable(r.getInterfaceLabel()));
    }

    private Optional<OnmsNode> getNode(String nodeCriteria) {
        if (nodeCriteria == null || nodeCriteria.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(nodeDao.get(nodeCriteria));
        } catch (Exception e) {
            LOG.error("error while trying to match node from {}", nodeCriteria);
        }
        return Optional.empty();
    }

    private String getNodeCriteriaFromResource(CollectionResource resource) {

        String nodeCriteria = null;
        if (resource.getParent() != null) {
            String[] resourcePathArray = resource.getParent().elements();
            if (ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY.equals(resourcePathArray[0])
                    && resourcePathArray.length == 3) {
                // parent denotes nodeCriteria, form fs:fid
                nodeCriteria = resourcePathArray[1] + ":" + resourcePathArray[2];
            } else if (checkNumeric(resourcePathArray[0])) {
                // parent denotes nodeId
                nodeCriteria = resourcePathArray[0];
            }
        }
        return nodeCriteria;
    }

    private boolean checkNumeric(String nodeCriteria) {
        try {
            Integer.parseInt(nodeCriteria);
            return true;
        } catch (NumberFormatException e) {
            // not a number
            return false;
        }
    }

    private void mapResponseTimeResource(final CollectionResource resource, final Map<String, String> tags) {
        boolean validIp = false;
        // Check if resource parent is an IpAddress.
        if (resource.getParent() != null && resource.getParent().elements().length == 1) {
            String[] resourcePathArray = resource.getParent().elements();
            validIp = checkForValidIpAddress(resourcePathArray[0]);
        }
        if (resource.getPath() != null && validIp) {
            // extract path which consists of location and IpAddress.
            String[] resourcePathArray = resource.getPath().elements();

            if (resourcePathArray.length == 2) {
                // first element is location, 2nd IpAddress.
                tags.put("location", resourcePathArray[0]);
                tags.put("ipAddress", resourcePathArray[1]);
            } else if (resourcePathArray.length == 1) {
                tags.put("ipAddress", resourcePathArray[0]);
            }
        }

        // TODO: Patrick: from where do we get the service?
        // tags.put(MetaTagKey.service.name(), );
    }

    private boolean checkForValidIpAddress(String resourcePath) {
        try {
            InetAddress.getByName(resourcePath);
            return true;
        } catch (UnknownHostException e) {
            // not an ipaddress.
            return false;
        }
    }
}

