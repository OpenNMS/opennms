package org.opennms.netmgt.syslogd.sink;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.syslogd.UDPMessageLogDTO;
import org.opennms.netmgt.syslogd.UDPProcessor;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

public class SyslogConsumer implements MessageConsumer<UDPMessageLogDTO>, InitializingBean {

    private static final SyslogModule syslogModule = new SyslogModule();

    private final Timer handleTimer;
    private final Timer toEventTimer;
    private final Timer broadcastTimer;

    @Autowired
    private MessageConsumerManager messageConsumerManager;

    @Autowired
    private UDPProcessor udpProcessor;
    
    @Autowired
    private EventForwarder eventForwarder;

    public SyslogConsumer(MetricRegistry registry) {
        handleTimer = registry.timer("handle");
        toEventTimer = registry.timer("handle.toevent");
        broadcastTimer = registry.timer("handle.broadcast");
    }

    @Override
    public SyslogModule getModule() {
        return syslogModule;
    }

    @Override
    public void handleMessage(UDPMessageLogDTO messageLog) {
        try (Context handleCtx = handleTimer.time()) {
            Log eventLog = null;
            try (Context toEventCtx = toEventTimer.time()) {
                eventLog = udpProcessor.toEventLog(messageLog);
            }

            if (eventLog != null) {
                try (Context broadCastCtx = broadcastTimer.time()) {
                    eventForwarder.sendNowSync(eventLog);
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Automatically register the consumer on initialization
        messageConsumerManager.registerConsumer(this);
    }

}
