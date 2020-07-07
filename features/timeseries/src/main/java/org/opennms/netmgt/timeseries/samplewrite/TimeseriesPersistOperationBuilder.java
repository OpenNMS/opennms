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

import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.toResourceId;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableMetric;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTag;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.PersistOperationBuilder;
import org.opennms.netmgt.collection.api.ResourceIdentifier;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.collection.support.DefaultTimeKeeper;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.timeseries.util.TimeseriesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Used to collect attribute values and meta-data for a given resource
 * and persist these via the {@link TimeseriesWriter} on {@link #commit()}.
 *
 * @author jwhite
 */
public class TimeseriesPersistOperationBuilder implements PersistOperationBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesPersistOperationBuilder.class);

    private final TimeseriesWriter writer;
    private final RrdRepository rrepository;
    private final String name;
    private final ResourceIdentifier resource;

    private final Map<CollectionAttributeType, Number> declarations = Maps.newLinkedHashMap();
    private final Map<String, String> metaData = Maps.newLinkedHashMap();
    private final Map<ResourcePath, Map<String, String>> stringAttributesByPath = Maps.newLinkedHashMap();
    private final Timer commitTimer;

    private TimeKeeper timeKeeper = new DefaultTimeKeeper();

    public TimeseriesPersistOperationBuilder(TimeseriesWriter writer, RrdRepository repository,
                                             ResourceIdentifier resource, String name, Map<String, String> metaTags,
                                             MetricRegistry metricRegistry) {
        this.writer = writer;
        rrepository = repository;
        this.resource = resource;
        this.name = name;
        metaData.putAll(metaTags);
        this.commitTimer = metricRegistry.timer("samples.write.integration");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setAttributeValue(CollectionAttributeType attributeType, Number value) {
        declarations.put(attributeType, value);
    }

    public void persistStringAttribute(ResourcePath path, String key, String value) {
        Map<String, String> stringAttributesForPath = stringAttributesByPath.computeIfAbsent(path, k -> Maps.newLinkedHashMap());
        stringAttributesForPath.put(key, value);
    }

    @Override
    public void setAttributeMetadata(String metricIdentifier, String name) {
        if (metricIdentifier == null) {
            if (name == null) {
                LOG.warn("Cannot set attribute metadata with null key and null value");
            } else {
                LOG.warn("Cannot set attribute metadata with null key and value of: {}", name);
            }
        } else {
            metaData.put(metricIdentifier, name);
        }
    }

    @Override
    public void commit() {
        try(final Timer.Context context = commitTimer.time()) {
            writer.insert(getSamplesToInsert());
            writer.index(getSamplesToIndex());
        }
    }

    public List<Sample> getSamplesToInsert() {
        final List<Sample> samples = Lists.newLinkedList();
        ResourcePath path = ResourceTypeUtils.getResourcePathWithRepository(rrepository, ResourcePath.get(resource.getPath(), name));

        // Add extra attributes that can be used to walk the resource tree.
        TimeseriesUtils.addIndicesToAttributes(path, metaData);
        String resourceId = TimeseriesUtils.toResourceId(path);

        // Convert numeric attributes to samples
        final Instant time = Instant.ofEpochMilli(timeKeeper.getCurrentTime());
        for (Entry<CollectionAttributeType, Number> entry : declarations.entrySet()) {
            CollectionAttributeType attrType = entry.getKey();

            Tag type = typeToTag(attrType.getType());
            if (type == null) {
                // Skip attributes with no type
                continue;
            }

            Number value = entry.getValue();
            if (value == null) {
                // Skip attributes with no value (see NMS-8103)
                continue;
            }

            ImmutableMetric.MetricBuilder builder = ImmutableMetric.builder()
                    .intrinsicTag(IntrinsicTagNames.resourceId, resourceId)
                    .intrinsicTag(IntrinsicTagNames.name, attrType.getName())
                    .metaTag(type);
                metaData.forEach(builder::metaTag);

            final ImmutableMetric metric = builder.build();
            final Double sampleValue = value.doubleValue();
            samples.add(ImmutableSample.builder().metric(metric).time(time).value(sampleValue).build());
        }
        return samples;
    }


    public List<Sample> getSamplesToIndex() {
        final List<Sample> samples = Lists.newLinkedList();

        // Convert string attributes to samples
        for (Entry<ResourcePath, Map<String, String>> entry : stringAttributesByPath.entrySet()) {
            samples.add(TimeseriesUtils.createSampleForIndexingStrings(toResourceId(entry.getKey()), entry.getValue()));
        }
        return samples;
    }

    private Tag typeToTag (final AttributeType type) {

        ImmutableMetric.Mtype mtype;

        if(type == AttributeType.COUNTER) {
            mtype = ImmutableMetric.Mtype.count;
        } else if (type == AttributeType.GAUGE) {
            mtype = ImmutableMetric.Mtype.gauge;
        } else if(type == AttributeType.STRING) {
            return null;
        } else {
            mtype = ImmutableMetric.Mtype.gauge;
        }
        return new ImmutableTag(IntrinsicTagNames.mtype, mtype.name());
    }

    /**
     * <p>setTimeKeeper</p>
     *
     * @param timeKeeper a {@link org.opennms.netmgt.collection.api.TimeKeeper} object.
     */
    public void setTimeKeeper(TimeKeeper timeKeeper) {
        this.timeKeeper = timeKeeper;
    }
}
