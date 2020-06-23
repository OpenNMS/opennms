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
