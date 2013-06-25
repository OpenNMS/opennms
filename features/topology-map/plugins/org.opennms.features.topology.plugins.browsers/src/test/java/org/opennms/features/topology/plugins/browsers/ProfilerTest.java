package org.opennms.features.topology.plugins.browsers;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Date;

public class ProfilerTest {

    @Test
    public void testProfiler() throws InterruptedException {
        Profiler profiler = new Profiler();
        Assert.assertTrue(profiler.timerMap.isEmpty());
        profiler.start("test");

        Assert.assertNotNull(profiler.timerMap.get("test"));
        Assert.assertTrue(profiler.timerMap.get("test").isStarted());
        Assert.assertEquals(1, profiler.timerMap.get("test").getCount());

        profiler.start("test");
        Assert.assertNotNull(profiler.timerMap.get("test"));
        Assert.assertTrue(profiler.timerMap.get("test").isStarted());
        Assert.assertEquals(2, profiler.timerMap.get("test").getCount());

        Thread.sleep(1000);

        profiler.stop("test");
        Assert.assertNotNull(profiler.timerMap.get("test"));
        Assert.assertFalse(profiler.timerMap.get("test").isStarted());
        Assert.assertEquals(2, profiler.timerMap.get("test").getCount());


        System.out.println(profiler.toString());
    }
}