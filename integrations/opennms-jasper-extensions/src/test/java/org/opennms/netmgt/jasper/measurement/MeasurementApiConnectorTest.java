package org.opennms.netmgt.jasper.measurement;

import org.junit.Assert;
import org.junit.Test;

public class MeasurementApiConnectorTest {
    @Test
    public void testAuthenticationRequired() {
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired(null, null));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired(null, ""));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired("", null));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired("", ""));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired("dummy", null));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired("dummy", ""));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired(null, "dummy"));
        Assert.assertFalse(MeasurementApiConnector.isAuthenticationRequired("", "dummy"));
        Assert.assertTrue(MeasurementApiConnector.isAuthenticationRequired("dummy", "dummy"));
    }
}
