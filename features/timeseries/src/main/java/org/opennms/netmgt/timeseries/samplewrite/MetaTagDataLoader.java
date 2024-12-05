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
package org.opennms.netmgt.timeseries.samplewrite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.FallbackScope;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTag;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.LatencyCollectionResource;
import org.opennms.netmgt.collection.support.builder.LatencyTypeResource;
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

import static org.opennms.netmgt.collection.api.CollectionResource.INTERFACE_INFO_IN_TAGS;


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
            if (nodeOptional.isPresent()) {
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
                if (resource.getResourceTypeName().equals(CollectionResource.RESOURCE_TYPE_LATENCY) &&
                        resource.getServiceParams().containsKey(INTERFACE_INFO_IN_TAGS) &&
                        Boolean.parseBoolean(resource.getServiceParams().get(INTERFACE_INFO_IN_TAGS))) {
                    if (resource instanceof LatencyCollectionResource) {
                        String ipAddress = ((LatencyCollectionResource) resource).getIpAddress();
                        scopes.add(this.entityScopeProvider.getScopeForInterface(node.getId(), ipAddress));
                        scopes.add(this.entityScopeProvider.getScopeForService(node.getId(), InetAddressUtils.addr(ipAddress),
                                ((LatencyCollectionResource) resource).getServiceName()));
                    } else if (resource instanceof LatencyTypeResource) {
                        String ipAddress = ((LatencyTypeResource) resource).getIpAddress();
                        scopes.add(this.entityScopeProvider.getScopeForInterface(node.getId(), ipAddress));
                        scopes.add(this.entityScopeProvider.getScopeForService(node.getId(), InetAddressUtils.addr(ipAddress),
                                ((LatencyTypeResource) resource).getServiceName()));
                    }
                }
                // We cannot retrieve service meta-data - resource time resources contain the IP address and service name, but not the node
            }

            // create tags for scopes
            Scope scope = new FallbackScope(scopes);
            Map<String, String> configuredMetaTags = this.config.getConfiguredMetaTags();
            for(Map.Entry<String, String> entry: configuredMetaTags.entrySet()) {
                final String value = Interpolator.interpolate(entry.getValue(), scope).output;
                // Ignore tags with empty values
                if (Strings.isNullOrEmpty(value)) {
                    continue;
                }
                tags.add(new ImmutableTag(entry.getKey(), value));
            }

            // create tags for categories
            nodeOptional.ifPresent(onmsNode -> mapCategories(tags, onmsNode));
            mapResourceTags(configuredMetaTags, tags, resource);
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

    private void mapResourceTags(final Map<String, String> configuredMetaTags, final Set<Tag> tags,
                                 final CollectionResource resource) {

        for (Map.Entry<String, String> entry : configuredMetaTags.entrySet()) {
            if (entry.getValue().contains("resource:label") && resource.getInterfaceLabel() != null) {
                tags.add(new ImmutableTag(entry.getKey(), resource.getInterfaceLabel()));
            }
            if (entry.getValue().contains("resource:node_label") && resource.getTags().get("node_label") != null) {
                tags.add(new ImmutableTag(entry.getKey(), resource.getTags().get("node_label")));
            }
            if (entry.getValue().contains("resource:location") && resource.getTags().get("location") != null) {
                tags.add(new ImmutableTag(entry.getKey(), resource.getTags().get("location")));
            }
            if (entry.getValue().contains("resource:node_id") && resource.getTags().get("node_id") != null) {
                tags.add(new ImmutableTag(entry.getKey(), resource.getTags().get("node_id")));
            }
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
            final String[] resourcePathArray = resource.getParent().elements();
            if (resourcePathArray != null && resourcePathArray.length > 0) {
                if (ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY.equals(resourcePathArray[0])
                        && resourcePathArray.length == 3) {
                    // parent denotes nodeCriteria, form fs:fid
                    nodeCriteria = resourcePathArray[1] + ":" + resourcePathArray[2];
                } else if (checkNumeric(resourcePathArray[0])) {
                    // parent denotes nodeId
                    nodeCriteria = resourcePathArray[0];
                }
            }
        }
        if (nodeCriteria == null && !resource.getTags().isEmpty()) {
            return resource.getTags().getOrDefault("node_id", null);
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
