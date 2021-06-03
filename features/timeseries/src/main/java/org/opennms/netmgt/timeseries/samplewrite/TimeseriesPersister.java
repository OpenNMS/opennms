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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.opennms.core.cache.Cache;
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

/**
 * TimeseriesPersister persistence strategy.
 *
 * Both string and numeric attributes are persisted via {@link TimeseriesPersistOperationBuilder}.
 *
 */
public class TimeseriesPersister extends AbstractPersister {

    private final RrdRepository repository;
    private final TimeseriesWriter writer;
    private final MetaTagDataLoader metaDataLoader;
    private final Cache<ResourcePath, Map<String, String>> metaTagsByResourceCache;
    private TimeseriesPersistOperationBuilder builder;
    private final MetricRegistry metricRegistry;

    protected TimeseriesPersister(ServiceParameters params, RrdRepository repository, TimeseriesWriter timeseriesWriter,
                                  MetaTagDataLoader metaDataLoader, Cache<ResourcePath, Map<String, String>> metaTagsByResourceCache,
                                  MetricRegistry metricRegistry) {
        super(params, repository);
        this.repository = repository;
        writer = timeseriesWriter;
        this.metaDataLoader = metaDataLoader;
        this.metaTagsByResourceCache = metaTagsByResourceCache;
        this.metricRegistry = Objects.requireNonNull(metricRegistry, "metricRegistry can not be null");
    }

    @Override
    public void visitResource(CollectionResource resource) {
        super.visitResource(resource);
        // compute meta data for this resource
        this.metaTagsByResourceCache.put(resource.getPath(), metaDataLoader.load(resource));
    }

    /** {@inheritDoc} */
    @Override
    public void visitGroup(AttributeGroup group) {
        pushShouldPersist(group);
        if (shouldPersist()) {
            // Set the builder before any calls to persistNumericAttribute are made
            CollectionResource resource = group.getResource();
            Map<String, String> metaTags = getMetaTags(resource);
            builder = new TimeseriesPersistOperationBuilder(writer, repository, resource, group.getName(), metaTags, this.metricRegistry);
            if (resource.getTimeKeeper() != null) {
                builder.setTimeKeeper(resource.getTimeKeeper());
            }
            setBuilder(builder);
        }
    }

    private Map<String, String> getMetaTags(final CollectionResource resource) {
        try {
            return metaTagsByResourceCache.get(resource.getPath());
        } catch (ExecutionException e) {
            LOG.warn("An exception occurred while trying to retrieve meta tags for {}", resource.getPath(), e);
        }
        return Collections.emptyMap();
    }

    @Override
    protected void persistStringAttribute(ResourcePath path, String key, String value) throws PersistException {
        builder.persistStringAttribute(path, key, value);
    }

    /** {@inheritDoc} */
    @Override
    public void completeGroup(AttributeGroup group) {
        if (shouldPersist()) {
            commitBuilder();
        }
        popShouldPersist();
    }

    @Override // Override to implement our own string attribute handling
    public void persistNumericAttribute(CollectionAttribute attribute) {
        boolean shouldIgnorePersist = isIgnorePersist() && AttributeType.COUNTER.equals(attribute.getType());
        LOG.debug("Persisting {} {}", attribute, (shouldIgnorePersist ? ". Ignoring value because of sysUpTime changed." : ""));
        Number value = shouldIgnorePersist ? Double.NaN : attribute.getNumericValue();
        builder.setAttributeValue(attribute.getAttributeType(), value);
        if(attribute.getMetricIdentifier() != null) {
            ResourcePath path = ResourceTypeUtils.getResourcePathWithRepository(repository, ResourcePath.get(attribute.getResource().getPath(), attribute.getAttributeType().getGroupType().getName()));
            this.builder.persistStringAttributeForMetricLevel(path,  attribute.getName(), attribute.getMetricIdentifier(), attribute.getName());
        }
    }
}
