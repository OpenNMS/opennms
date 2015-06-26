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
import java.util.List;

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
public class RRDv3Test {

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
        Assert.assertEquals("ifInDiscards", rrd.getDataSources().get(0).getName());
        Assert.assertEquals(DSType.COUNTER, rrd.getDataSources().get(0).getType());
        Assert.assertEquals(new Long(0), rrd.getDataSources().get(0).getUnknownSec());

        Assert.assertEquals(CFType.AVERAGE, rrd.getRras().get(0).getConsolidationFunction());
        Assert.assertEquals(new Long(1), rrd.getRras().get(0).getPdpPerRow());

        Assert.assertEquals(new Long(1), rrd.getRras().get(0).getPdpPerRow());
        Assert.assertEquals(new Long(1233321900), rrd.getStartTimestamp(rrd.getRras().get(0)));
        Assert.assertEquals(new Long(12), rrd.getRras().get(1).getPdpPerRow());
        Assert.assertEquals(new Long(1228572000), rrd.getStartTimestamp(rrd.getRras().get(1)));
        Assert.assertEquals(new Long(288), rrd.getRras().get(4).getPdpPerRow());
        Assert.assertEquals(new Long(1202342400), rrd.getStartTimestamp(rrd.getRras().get(4)));
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

}
