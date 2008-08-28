package org.opennms.web.rest;

import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public class MockEventProxy implements EventProxy {

    public void send(Event event) throws EventProxyException {
    }

    public void send(Log eventLog) throws EventProxyException {
    }

}
