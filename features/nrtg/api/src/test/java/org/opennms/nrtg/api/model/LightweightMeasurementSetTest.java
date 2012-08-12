package org.opennms.nrtg.api.model;

import junit.framework.Assert;

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

        HashMap<String, String> metrics = new HashMap<String, String>();

        for (int i=0;i<numberOfMetrics;i++) {
            metrics.put("metric"+i, "value"+i);
        }

        Date date = new Date();

        LightweightMeasurementSet lightweightMeasurementSet = new LightweightMeasurementSet(1, "service", "interface", date);

        for (String metricId : metrics.keySet()) {
            lightweightMeasurementSet.addMeasurement(metricId, metrics.get(metricId));
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
