package org.opennms.netmgt.eventd.processor;

import org.opennms.core.tsid.TsidFactory;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TsidAssigner implements EventProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TsidAssigner.class);

    private final TsidFactory tsidFactory;

    public TsidAssigner(TsidFactory tsidFactory) {
        this.tsidFactory = tsidFactory;
    }

    @Override
    public void process(Log eventLog) throws EventProcessorException {
        process(eventLog, false);
    }

    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        if (eventLog.getEvents() == null) {
            return;
        }
        for (Event event : eventLog.getEvents().getEvent()) {
            if (event.getDbid() == null || event.getDbid() == 0) {
                long tsid = tsidFactory.create();
                event.setDbid(tsid);
                LOG.debug("Assigned TSID {} to event {}", tsid, event.getUei());
            }
        }
    }
}
