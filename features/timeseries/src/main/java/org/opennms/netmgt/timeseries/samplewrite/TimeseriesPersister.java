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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.opennms.core.cache.Cache;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.netmgt.collection.api.AbstractPersister;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.PersistException;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TimeseriesPersister persistence strategy.
 *
 * Both string and numeric attributes are persisted via {@link TimeseriesPersistOperationBuilder}.
 *
 * String attributes:
 * We collect all attributes within a resource and commit them when we finished the collection.
 * We cannot commit earlier since we need to collect the resource level string attributes.
 * They can be part of any group within the resource but need to be applied to all metrics in the resource.
 * Therefore, we need to collect all attributes under the resource
 * Structure:
 * - resource
 *   + group
 *     + resource level string attribute
 *     + numeric attributes
 */
public class TimeseriesPersister extends AbstractPersister {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesPersister.class);

    private final RrdRepository repository;
    private final TimeseriesWriter writer;
    private final MetaTagDataLoader metaDataLoader;
    private final Cache<ResourcePath, Set<Tag>> configuredAdditionalMetaTagCache;
    private TimeseriesPersistOperationBuilder currentBuilder; // builds a group of attributes
    private List<TimeseriesPersistOperationBuilder> allBuilders; // we need to keep track for commit
    private Map<ResourcePath, Map<String, String>> resourceLevelStringAttributes;
    private final MetricRegistry metricRegistry;

    protected TimeseriesPersister(ServiceParameters params, RrdRepository repository, TimeseriesWriter timeseriesWriter,
                                  MetaTagDataLoader metaDataLoader, Cache<ResourcePath, Set<Tag>> configuredAdditionalMetaTagCache,
                                  MetricRegistry metricRegistry) {
        super(params, repository);
        this.repository = repository;
        writer = timeseriesWriter;
        this.metaDataLoader = metaDataLoader;
        this.configuredAdditionalMetaTagCache = configuredAdditionalMetaTagCache;
        this.metricRegistry = Objects.requireNonNull(metricRegistry, "metricRegistry can not be null");
    }

    @Override
    public void visitResource(CollectionResource resource) {
        super.visitResource(resource);
        // compute user defined meta data for this resource
        this.configuredAdditionalMetaTagCache.put(resource.getPath(), metaDataLoader.load(resource));
        this.resourceLevelStringAttributes = Maps.newLinkedHashMap();
        this.allBuilders = new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public void visitGroup(AttributeGroup group) {
        pushShouldPersist(group);
        if (shouldPersist()) {
            // Set the builder before any calls to persistNumericAttribute are made
            CollectionResource resource = group.getResource();
            Set<Tag> metaTags = getUserDefinedMetaTags(resource);
            currentBuilder = new TimeseriesPersistOperationBuilder(writer, repository, resource, group.getName(), metaTags,
                    resourceLevelStringAttributes, this.metricRegistry);
            if (resource.getTimeKeeper() != null) {
                currentBuilder.setTimeKeeper(resource.getTimeKeeper());
            }
            setBuilder(currentBuilder);
            this.allBuilders.add(currentBuilder);
        }
    }

    private Set<Tag> getUserDefinedMetaTags(final CollectionResource resource) {
        try {
            return configuredAdditionalMetaTagCache.get(resource.getPath());
        } catch (ExecutionException e) {
            LOG.warn("An exception occurred while trying to retrieve meta tags for {}", resource.getPath(), e);
        }
        return Collections.emptySet();
    }

    /**
     * Persists a resource level string attribute.
     */
    @Override
    protected void persistStringAttribute(ResourcePath path, String key, String value) throws PersistException {
        currentBuilder.persistStringAttribute(path, key, value);
    }

    @Override
    public void completeResource(CollectionResource resource) {
        if (shouldPersist()) {
            this.allBuilders.forEach(this::commitBuilder);
        }
        popShouldPersist();
    }

    public void commitBuilder(TimeseriesPersistOperationBuilder builder) {
        if (isPersistDisabled()) {
            LOG.debug("Persist disabled for {}", builder.getName());
            return;
        }
        try {
            builder.commit();
        } catch (PersistException e) {
            LOG.error("Unable to persist data for {}", builder.getName(), e);
        }
    }

    /**
     * Persists a metric level string attribute.
     */
    @Override // Override to implement our own string attribute handling
    public void persistNumericAttribute(CollectionAttribute attribute) {
        boolean shouldIgnorePersist = isIgnorePersist() && AttributeType.COUNTER.equals(attribute.getType());
        LOG.trace("Persisting {} {}", attribute, (shouldIgnorePersist ? ". Ignoring value because of sysUpTime changed." : ""));
        Number value = shouldIgnorePersist ? Double.NaN : attribute.getNumericValue();
        currentBuilder.setAttributeValue(attribute.getAttributeType(), value);
        if(attribute.getMetricIdentifier() != null) {
            ResourcePath path = ResourceTypeUtils.getResourcePathWithRepository(repository, ResourcePath.get(attribute.getResource().getPath(), attribute.getAttributeType().getGroupType().getName()));
            this.currentBuilder.persistStringAttributeForMetricLevel(path,  attribute.getName(), attribute.getMetricIdentifier(), attribute.getName());
        }
    }
}
