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

package org.opennms.netmgt.collection.persistence.timeseries;

import static org.opennms.netmgt.timeseries.integration.support.TimeseriesUtils.toResourceId;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.opennms.netmgt.timeseries.integration.TimescaleWriter;
import org.opennms.netmgt.timeseries.integration.support.TimeseriesUtils;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Used to collect attribute values and meta-data for a given resource
 * and persist these via the {@link TimescaleWriter} on {@link #commit()}.
 *
 * @author jwhite
 */
public class TimeseriesPersistOperationBuilder implements PersistOperationBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesPersistOperationBuilder.class);

    private final TimescaleWriter writer;
    private final RrdRepository rrepository;
    private final Context context;
    private final String name;
    private final ResourceIdentifier resource;

    private final Map<CollectionAttributeType, Number> declarations = Maps.newLinkedHashMap();
    private final Map<String, String> metaData = Maps.newLinkedHashMap();
    private final Map<ResourcePath, Map<String, String>> stringAttributesByPath = Maps.newLinkedHashMap();

    private TimeKeeper timeKeeper = new DefaultTimeKeeper();

    public TimeseriesPersistOperationBuilder(TimescaleWriter newtsWriter, Context context, RrdRepository repository, ResourceIdentifier resource, String name) {
        writer = newtsWriter;
        this.context = context;
        rrepository = repository;
        this.resource = resource;
        this.name = name;
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
        Map<String, String> stringAttributesForPath = stringAttributesByPath.get(path);
        if (stringAttributesForPath == null) {
            stringAttributesForPath = Maps.newLinkedHashMap();
            stringAttributesByPath.put(path, stringAttributesForPath);
        }
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
    public void commit() throws PersistException {
        writer.insert(getSamplesToInsert());
        writer.index(getSamplesToIndex());
    }

    public List<Sample> getSamplesToInsert() {
        final List<Sample> samples = Lists.newLinkedList();
        ResourcePath path = ResourceTypeUtils.getResourcePathWithRepository(rrepository, ResourcePath.get(resource.getPath(), name));

        // Add extra attributes that can be used to walk the resource tree.
        TimeseriesUtils.addIndicesToAttributes(path, metaData);
        Resource resource = new Resource(TimeseriesUtils.toResourceId(path), Optional.of(metaData));

        // Convert numeric attributes to samples
        Timestamp timestamp = Timestamp.fromEpochMillis(timeKeeper.getCurrentTime());
        for (Entry<CollectionAttributeType, Number> entry : declarations.entrySet()) {
            CollectionAttributeType attrType = entry.getKey();
            MetricType type = mapType(attrType.getType());
            if (type == null) {
                // Skip attributes with no type
                continue;
            }

            Number value = entry.getValue();
            if (value == null) {
                // Skip attributes with no value (see NMS-8103)
                continue;
            }

            samples.add(
                new Sample(
                    timestamp,
                        context,
                    resource,
                    attrType.getName(),
                    type,
                    ValueType.compose(entry.getValue(), type)
                )
            );
        }
        return samples;
    }

    public List<Sample> getSamplesToIndex() {
        final List<Sample> samples = Lists.newLinkedList();

        // Convert string attributes to samples
        for (Entry<ResourcePath, Map<String, String>> entry : stringAttributesByPath.entrySet()) {
            Resource resource = new Resource(toResourceId(entry.getKey()),
                    Optional.of(entry.getValue()));
            samples.add(TimeseriesUtils.createSampleForIndexingStrings(context, resource));
        }
        return samples;
    }

    public static MetricType mapType(AttributeType type) {
        switch(type) {
            case COUNTER:
                return MetricType.COUNTER;
            case GAUGE:
                return MetricType.GAUGE;
            case STRING:
                return null;
            default:
                return MetricType.GAUGE;
        }
    }

    /**
     * <p>getTimeKeeper</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.TimeKeeper} object.
     */
    public TimeKeeper getTimeKeeper() {
        return timeKeeper;
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
