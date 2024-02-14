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
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 23.06.12
 * Time: 19:05
 * To change this template use File | Settings | File Templates.
 */
public class DefaultMeasurementSetTest {
    @Test
    public void testGetMeasurements() throws Exception {

        final int numberOfMetrics = 10;
        Date dates[] = new Date[numberOfMetrics];

        DefaultMeasurementSet defaultMeasurementSet = new DefaultMeasurementSet();

        for (int i = 0; i < numberOfMetrics; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, i);
            dates[i] = calendar.getTime();

            DefaultMeasurement defaultMeasurement = new DefaultMeasurement();
            defaultMeasurement.setNodeId(i);
            defaultMeasurement.setService("service" + i);
            defaultMeasurement.setNetInterface("interface" + i);
            defaultMeasurement.setTimestamp(dates[i]);
            defaultMeasurement.setMetricId("metric" + i);
            defaultMeasurement.setValue("value" + i);

            defaultMeasurementSet.addMeasurement(defaultMeasurement);
        }

        List<Measurement> measurementList = defaultMeasurementSet.getMeasurements();

        Assert.assertEquals(measurementList.size(), numberOfMetrics);

        for (Measurement measurement : measurementList) {
            Assert.assertEquals(measurement.getService(), "service" + measurement.getNodeId());
            Assert.assertEquals(measurement.getNetInterface(), "interface" + measurement.getNodeId());
            Assert.assertEquals(measurement.getMetricId(), "metric" + measurement.getNodeId());
            Assert.assertEquals(measurement.getValue(), "value" + measurement.getNodeId());
            Assert.assertEquals(measurement.getTimestamp(), dates[measurement.getNodeId()]);
        }
    }
}
