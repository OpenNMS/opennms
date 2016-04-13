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
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;

/**
 * The Class EvaluateStatsIT.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluateStatsIT {

    /**
     * Test statistics.
     * 
     * @throws Exception the exception
     */
    @Test
    public void testStats() throws Exception {
        MetricRegistry registry = new MetricRegistry();
        EvaluateStats stats = new EvaluateStats(registry);

        // Check collection set
        for (int i = 0; i < 10; i++) {
            stats.checkCollectionSet();
        }
        Assert.assertEquals(10, registry.getMeters().get("evaluate.meter.collections").getCount());

        // Check resources
        for (int j=0; j<10; j++) {
            for (int i = 0; i < 10; i++) {
                stats.checkResource("resource" + i);
            }
        }
        Assert.assertEquals(10, registry.getGauges().get("evaluate.resources").getValue());
        Assert.assertEquals(100, registry.getMeters().get("evaluate.meter.resources").getCount());
    }

}
