package org.opennms.nrtg.api.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

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
