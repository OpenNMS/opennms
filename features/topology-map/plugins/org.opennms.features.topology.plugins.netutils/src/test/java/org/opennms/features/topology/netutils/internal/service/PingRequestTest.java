package org.opennms.features.topology.netutils.internal.service;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class PingRequestTest {

    @Test
    public void testWithTimeout() {
        PingRequest request = new PingRequest().withTimeout(1, TimeUnit.SECONDS);
        Assert.assertEquals(1000L, request.getTimeout());
    }

}