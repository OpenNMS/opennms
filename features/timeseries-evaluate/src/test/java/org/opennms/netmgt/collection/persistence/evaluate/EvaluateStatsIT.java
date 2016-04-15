/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.collection.persistence.evaluate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;

/**
 * The Class EvaluateStatsIT.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluateStatsIT {

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        System.setProperty("org.opennms.rrd.storeByGroup", "true");
    }

    /**
     * Test statistics.
     * 
     * @throws Exception the exception
     */
    @Test
    public void testStats() throws Exception {
        MetricRegistry registry = new MetricRegistry();
        EvaluateStats stats = new EvaluateStats(registry, 5);
        for (int i = 0; i < 10; i++) {
            stats.checkResource("resource" + i);
            for (int j = 0; j < 10; j++) {
                stats.checkGroup("resource" + i + "group" + j);
                for (int k = 0; k < 10; k++) {
                    stats.checkAttribute("resource" + i + "group" + j + "attribute" + k, true);
                    stats.getSamplesMeter().mark();
                }
            }
        }
        Assert.assertEquals(10, registry.getGauges().get("evaluate.resources").getValue());
        Assert.assertEquals(100, registry.getGauges().get("evaluate.groups").getValue());
        Assert.assertEquals(1000, registry.getGauges().get("evaluate.numeric-attributes").getValue());
        Assert.assertEquals(1000, registry.getMeters().get("evaluate.samples").getCount());
    }

}