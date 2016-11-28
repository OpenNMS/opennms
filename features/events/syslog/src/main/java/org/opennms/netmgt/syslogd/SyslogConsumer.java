package org.opennms.netmgt.syslogd;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.syslogd.sink.SyslogModule;
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

    private SyslogdConfig syslogdConfig;
    
    
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
            	UDPProcessor processor = new UDPProcessor(syslogdConfig);
                eventLog = processor.toEventLog(messageLog);
            }

            if (eventLog != null) {
                try (Context broadCastCtx = broadcastTimer.time()) {
                	m_eventForwarder.sendNowSync(eventLog);
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Automatically register the consumer on initialization
        messageConsumerManager.registerConsumer(this);
    }
    
    public SyslogdConfig getSyslogdConfig() {
		return syslogdConfig;
	}

	public void setSyslogdConfig(SyslogdConfig syslogdConfig) {
		this.syslogdConfig = syslogdConfig;
	}
    
    /**
     * The event IPC manager to which we send events created from traps.
     */
    private EventForwarder m_eventForwarder;

    /**
     * @return the eventMgr
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    /**
     * @param eventForwarder the eventMgr to set
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

}
