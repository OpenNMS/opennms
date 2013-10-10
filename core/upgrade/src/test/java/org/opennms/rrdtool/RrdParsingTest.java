/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.rrdtool;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;

/**
 * The Class XportTest.
 */
public class RrdParsingTest {
    
    @Test
    public void play()  {
        Double d1 = Double.NaN;
        Double d2 = Double.NaN;
        Assert.assertTrue(d1.equals(d2));
    }
    /**
     * Parses the RRD.
     *
     * @throws Exception the exception
     */
    @Test
    public void parseRrd() throws Exception {
        RRD rrd = JaxbUtils.unmarshal(RRD.class, new File("src/test/resources/rrd-dump.xml"));
        Assert.assertNotNull(rrd);
        Assert.assertEquals(new Long(300), rrd.getStep());
        Assert.assertEquals(new Long(1233926670), rrd.getLastUpdate());
        Assert.assertEquals("ifInDiscards", rrd.getDataSources().get(0).getName());
        Assert.assertEquals(new Integer(0), rrd.getDataSources().get(0).getUnknownSec());

        Assert.assertEquals(new Integer(1), rrd.getRras().get(0).getPdpPerRow());
        Assert.assertEquals(new Long(1233321900), rrd.getStartTimestamp(rrd.getRras().get(0)));
        Assert.assertEquals(new Integer(12), rrd.getRras().get(1).getPdpPerRow());
        Assert.assertEquals(new Long(1228572000), rrd.getStartTimestamp(rrd.getRras().get(1)));
        Assert.assertEquals(new Integer(288), rrd.getRras().get(4).getPdpPerRow());
        Assert.assertEquals(new Long(1202342400), rrd.getStartTimestamp(rrd.getRras().get(4)));
    }
}
