/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import com.codahale.metrics.MetricRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.integration.api.v1.timeseries.MetaTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.netmgt.collectd.NumericAttributeType;
import org.opennms.netmgt.collectd.ResourceType;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.ResourceIdentifier;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeseriesPersistOperationBuilderTest {

    private TimeseriesPersistOperationBuilder getBuilder() {
        Map<ResourcePath, Map<String, String>> stringAttributesByPath = new HashMap<>();
        RrdRepository rrdRepository = new RrdRepository();

        rrdRepository.setRrdBaseDir(new File("/tmp"));
        MetricRegistry metricRegistry = new MetricRegistry();
        ResourceIdentifier resourceIdentifier = mock(ResourceIdentifier.class);
        when(resourceIdentifier.getPath()).thenReturn(new ResourcePath("foo"));
        Set<Tag> tags = new HashSet<>();

        var builder = new TimeseriesPersistOperationBuilder(null, rrdRepository, resourceIdentifier,
                "groupName", tags, stringAttributesByPath, metricRegistry);


        return builder;
    }

    @Test
    public void testCounter() {
        MibObject mibObject = mock(MibObject.class);
        when(mibObject.getType()).thenReturn("counter");
        when(mibObject.getAlias()).thenReturn("counterAlias");
        NumericAttributeType numericAttributeType = new NumericAttributeType(mock(ResourceType.class), "some-collection", mibObject, new AttributeGroupType("mibGroup", AttributeGroupType.IF_TYPE_IGNORE));

        testDataType(numericAttributeType, Metric.Mtype.count, 1);
    }

    @Test
    public void testGauge() {
        MibObject mibObject = mock(MibObject.class);
        when(mibObject.getType()).thenReturn("guage");
        when(mibObject.getAlias()).thenReturn("guageAlias");
        NumericAttributeType numericAttributeType = new NumericAttributeType(mock(ResourceType.class), "some-collection", mibObject, new AttributeGroupType("mibGroup", AttributeGroupType.IF_TYPE_IGNORE));

        testDataType(numericAttributeType, Metric.Mtype.gauge, 1);
    }

    private void testDataType(CollectionAttributeType attributeType, Metric.Mtype mtype, Number value) {
        TimeseriesPersistOperationBuilder builder = this.getBuilder();
        builder.setAttributeValue(attributeType, value);

        List<Sample> samples = builder.getSamplesToInsert();
        System.out.println(samples);
        Assert.assertEquals(1, samples.size());
        Assert.assertEquals(Double.valueOf(value.doubleValue()), samples.get(0).getValue());

        Metric metric = samples.get(0).getMetric();
        Assert.assertEquals(1, metric.getMetaTags().size());
        Tag tag = metric.getMetaTags().stream().findFirst().get();
        Assert.assertEquals(MetaTagNames.mtype, tag.getKey());
        Assert.assertEquals(mtype.toString(), tag.getValue().toLowerCase());
        Assert.assertEquals(2, metric.getIntrinsicTags().size());
        tag = metric.getIntrinsicTags().stream().filter(t -> t.getKey().equals("name")).findFirst().get();
        Assert.assertEquals(attributeType.getName(), tag.getValue());
    }
}

