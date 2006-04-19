package org.opennms.netmgt.dao;

import java.util.Date;

import org.opennms.netmgt.model.OnmsEvent;

public class EventDaoTest extends AbstractDaoTestCase {

    public void setUp() throws Exception {
        //setPopulate(false);
        super.setUp();
    }
    
    public void testSave() {
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(getDistPollerDao().load("localhost"));
        event.setEventCreateTime(new Date());
        event.setEventDescr("event dao test");
        event.setEventHost("localhost");
        event.setEventLog("event dao test log");
        event.setEventLogGroup("event dao test log group");
        event.setEventLogMsg("event dao test log msg");
        event.setEventSeverity(new Integer(7));
        event.setEventSource("EventDaoTest");
        event.setEventTime(new Date());
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        getEventDao().save(event);
        getOutageDao();
        //it works we're so smart! hehe
        event = getEventDao().load(event.getId());
        assertEquals("ICMP", event.getService().getServiceType().getName());
    }
}
