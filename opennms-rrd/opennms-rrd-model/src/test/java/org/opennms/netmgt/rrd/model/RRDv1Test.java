/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.rrd.model.v1.RRDv1;
import org.opennms.netmgt.rrd.model.v1.CFType;
import org.opennms.netmgt.rrd.model.v1.DSType;

/**
 * The Class JRB Parsing Test.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class RRDv1Test {

    /**
     * Parses a simple JRB.
     *
     * @throws Exception the exception
     */
    @Test
    public void parseJrbSimple() throws Exception {
        RRDv1 rrd = JaxbUtils.unmarshal(RRDv1.class, new File("src/test/resources/jrb-dump.xml"));
        Assert.assertNotNull(rrd);
        Assert.assertEquals(new Long(300), rrd.getStep());
        Assert.assertEquals(new Long(1381503600), rrd.getLastUpdate());
        Assert.assertEquals("temp", rrd.getDataSources().get(0).getName());
        Assert.assertEquals(DSType.GAUGE, rrd.getDataSources().get(0).getType());
        Assert.assertEquals(new Long(0), rrd.getDataSources().get(0).getUnknownSec());

        Assert.assertEquals(CFType.AVERAGE, rrd.getRras().get(0).getConsolidationFunction());
        Assert.assertEquals(new Long(1), rrd.getRras().get(0).getPdpPerRow());

        Assert.assertEquals(new Long(1), rrd.getRras().get(0).getPdpPerRow());
        Assert.assertEquals(new Long(1381488900), rrd.getStartTimestamp(rrd.getRras().get(0)));
    }

}
