package org.opennms.netmgt.queued;

import java.util.Set;

import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class Queued extends AbstractServiceDaemon implements EventListener {
    
    private EventIpcManager m_eventMgr; 
    private RrdStrategy m_rrdStrategy;

    public Queued() {
        super("Queued");
    }
    
    public void setEventIpcManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }
    
    public void setRrdStrategy(RrdStrategy rrdStrategy) {
        m_rrdStrategy = rrdStrategy;
    }
    
    
    @Override
    protected void onInit() {
        Assert.state(m_eventMgr != null, "setEventIpcManager must be set");
        Assert.state(m_rrdStrategy != null, "rrdStrategy must be set");
        
        m_eventMgr.addEventListener(this, "uei.opennms.org/internal/promoteQueueData");
    }

    public void onEvent(Event e) {
        String fileList = EventUtils.getParm(e, "filesToPromote");
        Set<String> files = StringUtils.commaDelimitedListToSet(fileList);

        logFilePromotion(files);
        
        m_rrdStrategy.promoteEnqueuedFiles(files);
    }
    
    private void logFilePromotion(Set<String> files) {
        if (!log().isDebugEnabled()) {
            return;
        }
        
        for(String file : files) {
            debugf("Promoting file: %s", file);
        }
    }

    private void debugf(String format, Object... args) {
        if (log().isDebugEnabled()) {
            log().debug(String.format(format, args));
        }
    }

}
