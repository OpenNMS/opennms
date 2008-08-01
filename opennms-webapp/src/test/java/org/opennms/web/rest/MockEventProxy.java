package org.opennms.web.rest;

import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public class MockEventProxy implements EventProxy {

    public void send(Event event) throws EventProxyException {
    }

    public void send(Log eventLog) throws EventProxyException {
    }

}
