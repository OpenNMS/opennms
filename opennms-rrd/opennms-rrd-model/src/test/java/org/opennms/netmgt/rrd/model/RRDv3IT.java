/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd.model;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.rrd.model.AbstractRRD;
import org.opennms.netmgt.rrd.model.Row;
import org.opennms.netmgt.rrd.model.v3.CFType;
import org.opennms.netmgt.rrd.model.v3.DSType;
import org.opennms.netmgt.rrd.model.v3.RRA;
import org.opennms.netmgt.rrd.model.v3.RRDv3;

/**
 * The Class RRD Parsing Test.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class RRDv3IT {

    /**
     * Parses a simple RRD.
     *
     * @throws Exception the exception
     */
    @Test
    public void parseRrdSimple() throws Exception {
        RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, new File("src/test/resources/rrd-dump.xml"));
        Assert.assertNotNull(rrd);
        Assert.assertEquals(new Long(300), rrd.getStep());
        Assert.assertEquals(new Long(1233926670), rrd.getLastUpdate());

        // Test Data Source
        Assert.assertEquals("ifInDiscards", rrd.getDataSources().get(0).getName());
        Assert.assertEquals(DSType.COUNTER, rrd.getDataSources().get(0).getType());
        Assert.assertEquals(new Long(0), rrd.getDataSources().get(0).getUnknownSec());

        // Test RRA
        Assert.assertEquals(CFType.AVERAGE, rrd.getRras().get(0).getConsolidationFunction());
        Assert.assertEquals(new Long(1), rrd.getRras().get(0).getPdpPerRow());
        Assert.assertEquals(new Long(12), rrd.getRras().get(1).getPdpPerRow());
        Assert.assertEquals(new Long(288), rrd.getRras().get(4).getPdpPerRow());

        // Test time related functions : getEndTimestamp
        Assert.assertEquals(new Long(1233926400), rrd.getEndTimestamp(rrd.getRras().get(0)));
        Assert.assertEquals(new Long(1233925200), rrd.getEndTimestamp(rrd.getRras().get(1)));
        Assert.assertEquals(new Long(1233878400), rrd.getEndTimestamp(rrd.getRras().get(4)));

        // Test time related functions : getStartTimestamp
        Assert.assertEquals(new Long(1233321900), rrd.getStartTimestamp(rrd.getRras().get(0)));
        Assert.assertEquals(new Long(1228572000), rrd.getStartTimestamp(rrd.getRras().get(1)));
        Assert.assertEquals(new Long(1202342400), rrd.getStartTimestamp(rrd.getRras().get(4)));

        // Test time related functions : findRowByTimestamp
        AbstractRRA rra = rrd.getRras().get(0);
        Assert.assertEquals(rra.getRows().get(0), rrd.findRowByTimestamp(rra, new Long(1233321900)));
        Assert.assertEquals(rra.getRows().get(5), rrd.findRowByTimestamp(rra, new Long(1233323400)));

        // Test time related functions : findTimestampByRow
        Assert.assertEquals(new Long(1233321900), rrd.findTimestampByRow(rra, rra.getRows().get(0)));
        Assert.assertEquals(new Long(1233323400), rrd.findTimestampByRow(rra, rra.getRows().get(5)));
    }

    /**
     * Parses the RRD with computed DS.
     *
     * @throws Exception the exception
     */
    @Test
    public void parseRrdWithComputedDs() throws Exception {
        RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, new File("src/test/resources/rrd-dump-compute-ds.xml"));
        Assert.assertNotNull(rrd);
    }

    /**
     * Parses the RRD with aberrant behavior detection.
     *
     * @throws Exception the exception
     */
    @Test
    public void parseRrdWithAberrantBehaviorDetection() throws Exception {
        RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, new File("src/test/resources/rrd-dump-aberrant-behavior-detection.xml"));
        Assert.assertNotNull(rrd);
    }

    /**
     * Test split and merge
     *
     * @throws Exception the exception
     */
    @Test
    public void testSplit() throws Exception {
        RRDv3 masterRrd = JaxbUtils.unmarshal(RRDv3.class, new File("src/test/resources/rrd-dump.xml"));
        Assert.assertNotNull(masterRrd);
        List<AbstractRRD> rrds = masterRrd.split();
        Assert.assertEquals(masterRrd.getDataSources().size(), rrds.size());
        RRA masterRRA = masterRrd.getRras().get(0);
        for (int i=0; i<rrds.size(); i++) {
            RRDv3 singleRRD = (RRDv3) rrds.get(i);
            Assert.assertEquals(1, singleRRD.getDataSources().size());
            Assert.assertEquals(masterRrd.getDataSource(i).getName(), singleRRD.getDataSource(0).getName());
            RRA singleRRA = singleRRD.getRras().get(0);
            Assert.assertEquals(1, singleRRA.getDataSources().size());
            Assert.assertEquals(masterRRA.getPdpPerRow(), singleRRA.getPdpPerRow());
            Assert.assertEquals(masterRRA.getRows().size(), singleRRA.getRows().size());
            Assert.assertEquals(masterRRA.getConsolidationFunction().name(), singleRRA.getConsolidationFunction().name());
            for (int j=0; j < masterRRA.getRows().size(); j++) {
                Row masterRow = masterRRA.getRows().get(j);
                Row row = singleRRA.getRows().get(j);
                Assert.assertEquals(1, row.getValues().size());
                Assert.assertEquals(masterRow.getValues().get(i), row.getValues().get(0));
                masterRow.getValues().set(i, Double.NaN);
            }
        }
        int dsIndex = 3;
        masterRrd.merge(rrds);
        for (int j=0; j < masterRRA.getRows().size(); j++) {
            Row masterRow = masterRRA.getRows().get(j);
            Row row = rrds.get(dsIndex).getRras().get(0).getRows().get(j);
            Assert.assertEquals(1, row.getValues().size());
            Assert.assertEquals(masterRow.getValues().get(dsIndex), row.getValues().get(0));
        }
    }

    /**
     * Test merge.
     *
     * @throws Exception the exception
     */
    @Test
    public void testMerge() throws Exception {
        File sourceFile = new File("src/test/resources/rrd-temp-multids-rrd.xml");
        File targetFile = new File("target/multimetric.xml");
        RRDv3 multimetric = JaxbUtils.unmarshal(RRDv3.class, sourceFile);
        Assert.assertNotNull(multimetric);
        Assert.assertEquals("tempA", multimetric.getDataSource(0).getName());
        Assert.assertEquals("tempB", multimetric.getDataSource(1).getName());
        multimetric.reset();
        List<RRDv3> singleMetricArray = new ArrayList<>();
        RRDv3 tempA = JaxbUtils.unmarshal(RRDv3.class, new File("src/test/resources/rrd-tempA-rrd.xml"));
        Assert.assertNotNull(tempA);
        Assert.assertEquals("tempA", tempA.getDataSource(0).getName());
        singleMetricArray.add(tempA);
        RRDv3 tempB = JaxbUtils.unmarshal(RRDv3.class, new File("src/test/resources/rrd-tempB-rrd.xml"));
        Assert.assertNotNull(tempB);
        Assert.assertEquals("tempB", tempB.getDataSource(0).getName());
        singleMetricArray.add(tempB);
        multimetric.merge(singleMetricArray);
        JaxbUtils.marshal(multimetric, new FileWriter(targetFile));
        Assert.assertTrue(FileUtils.contentEquals(sourceFile, targetFile));
        targetFile.delete();
    }

    /**
     * Test samples for a single RRA
     *
     * @throws Exception the exception
     */
    @Test
    public void testSamplesSingleRRA() throws Exception {
        File source = new File("src/test/resources/sample-counter.xml");
        RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, source);
        Assert.assertNotNull(rrd);
        NavigableMap<Long, List<Double>> samples = rrd.generateSamples(rrd.getRras().get(0));
        Assert.assertFalse(samples.isEmpty());
        long ts = 1441748400L;
        Double v1 = 600.0;
        Double v2 = 2.0;
        Assert.assertEquals(rrd.getRras().get(0).getRows().size(), samples.size());
        for (Map.Entry<Long, List<Double>> s : samples.entrySet()) {
            System.out.println(s);
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
        File source = new File("src/test/resources/sample-counter-rras.xml");
        RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, source);
        Assert.assertNotNull(rrd);
        NavigableMap<Long, List<Double>> samples = rrd.generateSamples(rrd.getRras().get(1));
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
        File source = new File("src/test/resources/sample-counter-rras.xml");
        RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, source);
        Assert.assertNotNull(rrd);
        SortedMap<Long, List<Double>> samples = rrd.generateSamples();
        Assert.assertFalse(samples.isEmpty());
        int size = rrd.getRras().stream().mapToInt(r -> r.getRows().size()).sum();
        Assert.assertEquals(size - 3 - 1, samples.size()); // There are 3 timestamps that exist in both RRAs and the last one is incomplete
    }

}
