package org.opennms.netmgt.syslogd;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.camel.DispatcherWhiteboard;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.mock.MockDistPollerDao;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

public class PerfTest {
    
    public static class MyDispatcher extends DispatcherWhiteboard {
        
        private final Meter dispatches;

        public MyDispatcher(MetricRegistry metrics) {
            super("uri");
            dispatches = metrics.meter("dispatches");
        }

        @Override
        public void dispatch(final Object message) {
            if (message instanceof UDPMessageLogDTO) {
                UDPMessageLogDTO udpMessageLog = (UDPMessageLogDTO)message;
                dispatches.mark(udpMessageLog.getMessages().size());
            }
        }
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging(true, "WARN");
    }

    @Test
    public void doIt() throws InterruptedException {
        final MetricRegistry metrics = new MetricRegistry();
        // Setup the reporter
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        MyDispatcher dipsatcher = new MyDispatcher(metrics);

        SyslogConfigBean config = new SyslogConfigBean();
        config.setSyslogPort(1514);
        SyslogReceiverCamelNettyImpl receiver = new SyslogReceiverCamelNettyImpl(config);
        DistPollerDao distPollerDao = new MockDistPollerDao();
        receiver.setDistPollerDao(distPollerDao);
        receiver.setSyslogDispatcher(dipsatcher);

        reporter.start(1, TimeUnit.SECONDS);
        receiver.run();
        Thread.sleep(60 * 60 * 1000);
    }
}
