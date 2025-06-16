/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
