/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.nrtg.api.model;

import org.junit.Assert;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 23.06.12
 * Time: 18:48
 * To change this template use File | Settings | File Templates.
 */
public class LightweightMeasurementSetTest {

    @org.junit.Test
    public void testGetMeasurements() throws Exception {

        final int numberOfMetrics = 10;
        // metricId, metricValue
        HashMap<String, String> metrics = new HashMap<String, String>();

        for (int i=0;i<numberOfMetrics;i++) {
            metrics.put("metric"+i, "value"+i);
        }

        Date date = new Date();

        LightweightMeasurementSet lightweightMeasurementSet = new LightweightMeasurementSet(1, "service", "interface", date);

        for (String metricId : metrics.keySet()) {
            lightweightMeasurementSet.addMeasurement(metricId, "int32", metrics.get(metricId), "DummyName");
        }

        List<Measurement> measurementList = lightweightMeasurementSet.getMeasurements();

        Assert.assertEquals(measurementList.size(), metrics.size());

        for(Measurement measurement : measurementList) {
            Assert.assertEquals(measurement.getNodeId(), 1);
            Assert.assertEquals(measurement.getService(), "service");
            Assert.assertEquals(measurement.getNetInterface(), "interface");
            Assert.assertEquals(measurement.getTimestamp(), date);

            Assert.assertTrue(metrics.containsKey(measurement.getMetricId()));
            Assert.assertEquals(measurement.getValue(), metrics.get(measurement.getMetricId()));

            metrics.remove(measurement.getMetricId());
        }

        Assert.assertEquals(metrics.size(), 0);
    }
}
