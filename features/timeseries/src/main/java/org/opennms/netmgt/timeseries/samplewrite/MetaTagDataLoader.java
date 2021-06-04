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

package org.opennms.netmgt.timeseries.samplewrite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.core.rpc.utils.mate.FallbackScope;
import org.opennms.core.rpc.utils.mate.Interpolator;
import org.opennms.core.rpc.utils.mate.Scope;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTag;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Maps;

/** Loads meta data from OpenNMS, to be exposed to the TimeseriesStorage. This data is not relevant for the operation of
 * OpenNMS but can be used to enrich the data in the timeseries database to be used externally. */
public class MetaTagDataLoader extends CacheLoader<CollectionResource, Set<Tag>> {

    private static final Logger LOG = LoggerFactory.getLogger(MetaTagDataLoader.class);

    private final NodeDao nodeDao;
    private final SessionUtils sessionUtils;
    private final EntityScopeProvider entityScopeProvider;
    private MetaTagConfiguration config;

    @Inject
    public MetaTagDataLoader(final NodeDao nodeDao, final SessionUtils sessionUtils, final EntityScopeProvider entityScopeProvider) {

        this.nodeDao = Objects.requireNonNull(nodeDao, "nodeDao must not be null");
        this.sessionUtils = Objects.requireNonNull(sessionUtils, "sessionUtils must not be null");
        this.entityScopeProvider = Objects.requireNonNull(entityScopeProvider, "entityScopeProvider must not be null");
        setConfig(new MetaTagConfiguration(Maps.fromProperties(System.getProperties())));
    }

    public void setConfig(final MetaTagConfiguration config) {
        this.config = config;
    }

    public Set<Tag> load(final CollectionResource resource) {
        return sessionUtils.withReadOnlyTransaction(() -> {

            final Set<Tag> tags = new HashSet<>();
            List<Scope> scopes = new ArrayList<>();

            // node related scopes
            String nodeCriteria = getNodeCriteriaFromResource(resource);
            Optional<OnmsNode> nodeOptional = getNode(nodeCriteria);
            if(nodeOptional.isPresent()) {
                OnmsNode node = nodeOptional.get();
                scopes.add(this.entityScopeProvider.getScopeForNode(node.getId()));
                if (resource.getResourceTypeName().equals(CollectionResource.RESOURCE_TYPE_IF)) {
                    // We expect #getInstance to return the ifIndex for interface-level resources
                    try {
                        int ifIndex = Integer.parseInt(resource.getInstance());
                        scopes.add(this.entityScopeProvider.getScopeForInterfaceByIfIndex(node.getId(), ifIndex));
                    } catch(NumberFormatException nfe) {
                        // pass
                    }
                }
                // We cannot retrieve service meta-data - resource time resources contain the IP address and service name, but not the node
            }

            // create tags for scopes
            Scope scope = new FallbackScope(scopes);
            Map<String, String> configuredMetaTags = this.config.getConfiguredMetaTags();
            for(Map.Entry<String, String> entry: configuredMetaTags.entrySet()) {
                final String value = Interpolator.interpolate(entry.getValue(), scope);
                // Ignore tags with empty values
                if (Strings.isNullOrEmpty(value)) {
                    continue;
                }
                tags.add(new ImmutableTag(entry.getKey(), value));
            }

            // create tags for categories
            nodeOptional.ifPresent(onmsNode -> mapCategories(tags, onmsNode));
            return tags;
        });
    }

    private void mapCategories(final Set<Tag> tags, final OnmsNode node) {
        Objects.requireNonNull(node);
        if(config.isCategoriesEnabled()) {
            node.getCategories().stream()
                    .map(OnmsCategory::getName)
                    .forEach(catName -> tags.add(new ImmutableTag("cat_" + catName, catName)));
        }
    }

    private Optional<OnmsNode> getNode(String nodeCriteria) {
        if (nodeCriteria == null || nodeCriteria.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(nodeDao.get(nodeCriteria));
        } catch (Exception e) {
            LOG.error("Error while trying to load node for criteria: {}. No node will be returned.", nodeCriteria, e);
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
}
