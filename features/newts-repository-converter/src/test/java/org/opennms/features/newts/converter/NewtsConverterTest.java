/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.newts.converter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.rrd.model.v3.DS;
import org.opennms.netmgt.rrd.model.v3.DSType;
import org.opennms.netmgt.rrd.model.v3.RRDv3;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Timestamp;

import com.google.common.base.Optional;
import com.google.common.primitives.UnsignedLong;

public class NewtsConverterTest {

    @Test(expected=IllegalArgumentException.class)
    public void cantConvertOutOfRangeCounterToSample() {
        Resource resource = new Resource("resource", Optional.absent());
        DS ds = new DS();
        ds.setType(DSType.COUNTER);
        Timestamp timestamp = Timestamp.fromEpochSeconds(0);
        NewtsConverter.toSample(ds, resource, timestamp, Double.MAX_VALUE);
    }

    public void canConvertCounterToSample() {
        Resource resource = new Resource("resource", Optional.absent());
        DS ds = new DS();
        ds.setType(DSType.COUNTER);
        Timestamp timestamp = Timestamp.fromEpochSeconds(0);
        NewtsConverter.toSample(ds, resource, timestamp, UnsignedLong.MAX_VALUE.doubleValue());
    }

    /**
     * Test samples for a single RRA
     *
     * @throws Exception the exception
     */
    @Test
    public void testSamplesSingleRRA() throws Exception {
        final File source = new File("src/test/resources/sample-counter.xml");
        final RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, source);
        Assert.assertNotNull(rrd);

        final NavigableMap<Long, List<Double>> samples = NewtsConverter.generateSamples(rrd, rrd.getRras().get(0));
        Assert.assertFalse(samples.isEmpty());
        Assert.assertEquals(12, samples.size());

        long ts = 1441748400L;
        Double v1 = 600.0;
        Double v2 = 2.0;
        for (final Map.Entry<Long, List<Double>> s : samples.entrySet()) {
            Assert.assertEquals(2, s.getValue().size());
            Assert.assertEquals(ts, (long) s.getKey());
            Assert.assertEquals(v1, s.getValue().get(0));
            Assert.assertEquals(v2, s.getValue().get(1));
            ts += 300L;
            v1 += 300.0 * v2;
            v2 += 1.0;
        }
    }

    /**
     * Test samples for multiple RRAs (1)
     *
     * @throws Exception the exception
     */
    @Test
    public void testSamplesMultipleRRAs1() throws Exception {
        final File source = new File("src/test/resources/sample-counter-rras.xml");
        final RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, source);
        Assert.assertNotNull(rrd);

        final NavigableMap<Long, List<Double>> samples = NewtsConverter.generateSamples(rrd, rrd.getRras().get(1));
        Assert.assertFalse(samples.isEmpty());
        Assert.assertEquals(rrd.getRras().get(1).getRows().size(), samples.size());
    }

    /**
     * Test samples for multiple RRAs (2)
     *
     * @throws Exception the exception
     */
    @Test
    public void testSamplesMultipleRRAs2() throws Exception {
        final File source = new File("src/test/resources/sample-counter-rras.xml");
        final RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, source);
        Assert.assertNotNull(rrd);

        final SortedMap<Long, List<Double>> samples = NewtsConverter.generateSamples(rrd);
        Assert.assertFalse(samples.isEmpty());

        final int size = rrd.getRras().stream().mapToInt(r -> r.getRows().size()).sum();
        Assert.assertEquals(size - 3 - 1, samples.size()); // There are 3 timestamps that exist in both RRAs and the last one is incomplete
    }

    @Test
    public void testSamplesMixedCF() throws Exception {
        final File source = new File("src/test/resources/sample-counter-mixed-cf.xml");
        final RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, source);
        Assert.assertNotNull(rrd);

        final SortedMap<Long, List<Double>> samples = NewtsConverter.generateSamples(rrd);
        Assert.assertFalse(samples.isEmpty());

        // Using 5 from the coarser RRA and 18 of 20 from the finer one as the last 2 are leaking into the upper RRA
        Assert.assertEquals(23, samples.size());
    }
}
