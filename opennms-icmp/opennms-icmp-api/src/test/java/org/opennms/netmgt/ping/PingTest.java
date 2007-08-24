package org.opennms.netmgt.ping;

import java.net.InetAddress;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.core.utils.CollectionMath;

public class PingTest extends TestCase {
    private InetAddress m_goodHost = null;
    private InetAddress m_badHost = null;

    /**
     * Don't run this test unless the runPingTests property
     * is set to "true".
     */
    @Override
    protected void runTest() throws Throwable {
        if (!isRunTest()) {
            System.err.println("Skipping test '" + getName() + "' because system property '" + getRunTestProperty() + "' is not set to 'true'");
            return;
        }
            
        try {
            System.err.println("------------------- begin "+getName()+" ---------------------");
            super.runTest();
        } finally {
            System.err.println("------------------- end "+getName()+" -----------------------");
        }
    }

    private boolean isRunTest() {
        return Boolean.getBoolean(getRunTestProperty());
    }

    private String getRunTestProperty() {
        return "runPingTests";
    }

    @Override
    protected void setUp() throws Exception {
        if (!isRunTest()) {
            return;
        }

        super.setUp();
        m_goodHost = InetAddress.getByName("www.google.com");
        m_badHost  = InetAddress.getByName("1.1.1.1");
    }

    public void testSinglePing() throws Exception {
        assertTrue(Pinger.ping(m_goodHost) > 0);
    }

    public void testSinglePingFailure() throws Exception {
        assertNull(Pinger.ping(m_badHost));
    }

    public void testParallelPing() throws Exception {
        List<Number> items = Pinger.parallelPing(m_goodHost, 20, Pinger.DEFAULT_TIMEOUT, 50);
        Thread.sleep(1000);
        printResponse(items);
        assertTrue(CollectionMath.countNotNull(items) > 0);
    }

    public void testParallelPingFailure() throws Exception {
        List<Number> items = Pinger.parallelPing(m_badHost, 20, Pinger.DEFAULT_TIMEOUT, 50);
        Thread.sleep(1000);
        printResponse(items);
        assertTrue(CollectionMath.countNotNull(items) == 0);
    }
    
    private void printResponse(List<Number> items) {
        Long passed = CollectionMath.countNotNull(items);
        Long failed = CollectionMath.countNull(items);
        Number passedPercent = CollectionMath.percentNotNull(items);
        Number failedPercent = CollectionMath.percentNull(items);
        Number average = CollectionMath.average(items);
        Number median = CollectionMath.median(items);
        
        if (passedPercent == null) passedPercent = new Long(0);
        if (failedPercent == null) failedPercent = new Long(100);
        if (median        == null) median        = new Double(0);

        if (average == null) {
            average = new Double(0);
        } else {
            average = new Double(average.doubleValue() / 1000.0);
        }

        StringBuffer sb = new StringBuffer();
        sb.append("response times = ").append(items);
        sb.append("\n");
        
        sb.append("pings = ").append(items.size());
        sb.append(", passed = ").append(passed).append(" (").append(passedPercent).append("%)");
        sb.append(", failed = ").append(failed).append(" (").append(failedPercent).append("%)");
        sb.append(", median = ").append(median);
        sb.append(", average = ").append(average).append("ms");
        sb.append("\n");
        System.out.print(sb);
    }
}
