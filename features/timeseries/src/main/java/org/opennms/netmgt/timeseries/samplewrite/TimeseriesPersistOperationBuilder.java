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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opennms.integration.api.v1.timeseries.DataPoint;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.MetaTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableMetric;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTag;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.PersistException;
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
 * Used to collect attribute values and meta-data for a given resource group
 * and persist these via the {@link RingBufferTimeseriesWriter} on {@link #commit()}.
 */
public class TimeseriesPersistOperationBuilder implements PersistOperationBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesPersistOperationBuilder.class);

    private final TimeseriesWriter writer;
    private final RrdRepository rrepository;
    private final String groupName;
    private final ResourceIdentifier resource;

    private final Map<CollectionAttributeType, Number> declarations = Maps.newLinkedHashMap();
    private final Set<Tag> configuredAdditionalMetaTags;
    private final Map<ResourcePath, Map<String, String>> stringAttributesByPath;
    private final Map<Set<Tag>, Map<String, String>> stringAttributesByResourceIdAndName = Maps.newLinkedHashMap();
    private final Timer commitTimer;

    private TimeKeeper timeKeeper = new DefaultTimeKeeper();

    public TimeseriesPersistOperationBuilder(TimeseriesWriter writer, RrdRepository repository,
                                             ResourceIdentifier resource, String groupName,
                                             Set<Tag> configuredAdditionalMetaTags,
                                             final Map<ResourcePath, Map<String, String>> stringAttributesByPath,
                                             MetricRegistry metricRegistry) {
        this.writer = writer;
        rrepository = repository;
        this.resource = resource;
        this.groupName = groupName;
        this.configuredAdditionalMetaTags = configuredAdditionalMetaTags;
        this.stringAttributesByPath = stringAttributesByPath;
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
     * => Resource level attributes.
     */
    public void persistStringAttribute(ResourcePath path, String key, String value) {
        Map<String, String> stringAttributesForPath = stringAttributesByPath.computeIfAbsent(path, k -> Maps.newLinkedHashMap());
        stringAttributesForPath.put(key, value);
    }

    /**
     * Persists a String attribute that is associated to a Metric (resourceId & name)
     * => Group level attributes
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
    public void commit() throws PersistException {
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
                    .metaTag(type);

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

    /**
     * @see org.opennms.netmgt.timeseries.sampleread.aggregation.NewtsConverterUtils#toNewtsValue
     * @param type
     * @return
     */
    private Tag typeToTag(final AttributeType type) {
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
        // types handling is in NewtsConverterUtils.toNewtsValue
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
