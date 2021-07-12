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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.MetaTagNames;
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
import com.google.common.collect.Sets;

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
    private final String groupName;
    private final ResourceIdentifier resource;

    private final Map<CollectionAttributeType, Number> declarations = Maps.newLinkedHashMap();
    private final Set<Tag> configuredAdditionalMetaTags;
    private final Map<ResourcePath, Map<String, String>> stringAttributesByPath = Maps.newLinkedHashMap();
    private final Map<Set<Tag>, Map<String, String>> stringAttributesByResourceIdAndName = Maps.newLinkedHashMap();
    private final Timer commitTimer;

    private TimeKeeper timeKeeper = new DefaultTimeKeeper();

    public TimeseriesPersistOperationBuilder(TimeseriesWriter writer, RrdRepository repository,
                                             ResourceIdentifier resource, String groupName, Set<Tag> configuredAdditionalMetaTags,
                                             MetricRegistry metricRegistry) {
        this.writer = writer;
        rrepository = repository;
        this.resource = resource;
        this.groupName = groupName;
        this.configuredAdditionalMetaTags = configuredAdditionalMetaTags;
        this.commitTimer = metricRegistry.timer("samples.write.integration");
    }

    @Override
    public String getName() {
        return groupName;
    }

    @Override
    public void setAttributeValue(CollectionAttributeType attributeType, Number value) {
        declarations.put(attributeType, value);
    }

    /**
     * Persists a String attribute that is associated to a ResourcePath (resourceId)
     */
    public void persistStringAttribute(ResourcePath path, String key, String value) {
        Map<String, String> stringAttributesForPath = stringAttributesByPath.computeIfAbsent(path, k -> Maps.newLinkedHashMap());
        stringAttributesForPath.put(key, value);
    }

    /**
     * Persists a String attribute that is associated to a Metric (resourceId & name)
     */
    public void persistStringAttributeForMetricLevel(ResourcePath path, String metricName, String key, String value) {
        Set<Tag> intrinsicTags = Sets.newHashSet(new ImmutableTag(IntrinsicTagNames.resourceId, TimeseriesUtils.toResourceId(path)), new ImmutableTag(IntrinsicTagNames.name, metricName));
        Map<String, String> stringAttributesForPath = this.stringAttributesByResourceIdAndName.computeIfAbsent(intrinsicTags, k -> Maps.newLinkedHashMap());
        stringAttributesForPath.put(key, value);
    }

    @Override
    public void setAttributeMetadata(String metricIdentifier, String name) {
        // Ugly hack here:
        // This method is normally called by AbstractPersister.persistNumericAttribute(CollectionAttribute attribute)
        // but we are overriding that method in TimeseriesPersister => this method should never be called.
        throw new UnsupportedOperationException("Should never be called. We made a mistake!");
    }

    @Override
    public void commit() {
        try(final Timer.Context context = commitTimer.time()) {
            writer.insert(getSamplesToInsert());
        }
    }

    public List<Sample> getSamplesToInsert() {
        final Set<Tag> resourceIdLevelExternalData = Sets.newHashSet();
        final List<Sample> samples = Lists.newLinkedList();
        ResourcePath path = ResourceTypeUtils.getResourcePathWithRepository(rrepository, ResourcePath.get(resource.getPath(), groupName));

        // Collect resource and group level attributes
        Map<String, String> stringAttributes = new HashMap<>();
        ResourcePath p = path;
        while (p.hasParent()) {
            p = p.getParent();
            Map<String, String> attributes = stringAttributesByPath.get(p);
            if (attributes != null) {
                stringAttributes.putAll(attributes);
            }
        }
        for (Entry<String, String> entry : stringAttributes.entrySet()) {
            resourceIdLevelExternalData.add(new ImmutableTag(entry.getKey(), entry.getValue()));
        }

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
                    .externalTag(type);

            // add resource level string attributes
            this.configuredAdditionalMetaTags.forEach(builder::metaTag);
            resourceIdLevelExternalData.forEach(builder::externalTag);

            // add metric level string attributes
            Map<String, String> metricLevelAttributes = stringAttributesByResourceIdAndName.get(builder.build().getIntrinsicTags());
            if (metricLevelAttributes != null) {
                for (Entry<String, String> entry2 : stringAttributesByResourceIdAndName.get(builder.build().getIntrinsicTags()).entrySet()) {
                    builder.externalTag(entry2.getKey(), entry2.getValue());
                }
            }

            final ImmutableMetric metric = builder.build();
            final Double sampleValue = value.doubleValue();
            samples.add(ImmutableSample.builder().metric(metric).time(time).value(sampleValue).build());
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
        return new ImmutableTag(MetaTagNames.mtype, mtype.name());
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
