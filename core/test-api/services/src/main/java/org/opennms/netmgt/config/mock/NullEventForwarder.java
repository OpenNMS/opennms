package org.opennms.netmgt.config.mock;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public class NullEventForwarder implements EventForwarder {
    @Override
    public void sendNow(Event event) {
    }

    @Override
    public void sendNow(Log eventLog) {
    }

    @Override
    public void sendNowSync(Event event) {
    }

    @Override
    public void sendNowSync(Log eventLog) {
    }
}
