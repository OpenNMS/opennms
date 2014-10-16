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
import org.opennms.netmgt.rrd.model.RrdXport;

/**
 * The Class RRD Export Test.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class RrdXportTest {

    /**
     * Parses the Xport.
     *
     * @throws Exception the exception
     */
    @Test
    public void parseXport() throws Exception {
        RrdXport xport = JaxbUtils.unmarshal(RrdXport.class, new File("src/test/resources/rrd-xport.xml"));
        Assert.assertNotNull(xport);
        Assert.assertEquals(new Long(300), xport.getMeta().getStep());
        Assert.assertEquals(new Long(1206312900), xport.getMeta().getStart());
        Assert.assertEquals(new Long(1206316500), xport.getMeta().getEnd());
        Assert.assertEquals("load average 5min", xport.getMeta().getLegends().get(0));
        Assert.assertEquals(new Long(1206312900), xport.getRows().get(0).getTimestamp());
        Assert.assertEquals(new Double(19.86), xport.getRows().get(0).getValues().get(0));
    }
}
