package org.opennms.netmgt.syslogd.sink;

import org.opennms.core.ipc.sink.api.MessageProducer;
import org.opennms.core.ipc.sink.api.MessageProducerFactory;
import org.opennms.netmgt.syslogd.UDPMessageLogDTO;
import org.opennms.netmgt.syslogd.UDPMessageLogDTOHandler;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

public class SyslogProducer implements UDPMessageLogDTOHandler {

    private static final MetricRegistry METRICS = new MetricRegistry();

    private final Timer processTimer = METRICS.timer(MetricRegistry.name(getClass(), "send"));

    private final MessageProducer<UDPMessageLogDTO> delegate;

    public SyslogProducer(MessageProducerFactory messageProducerFactory) {
        delegate = messageProducerFactory.getProducer(new SyslogModule());

        final JmxReporter reporter = JmxReporter.forRegistry(METRICS).inDomain("test").build();
        reporter.start();
    }

    @Override
    public void handleUDPMessageLog(UDPMessageLogDTO messageLog) {
        try (Context ctx = processTimer.time()) {
            delegate.send(messageLog);
        }
    }

}
