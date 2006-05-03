package org.opennms.netmgt.dao;

import java.util.Date;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;

public class AlarmDaoTest extends AbstractDaoTestCase {

    public void setUp() throws Exception {
        //setPopulate(false);
        super.setUp();
    }
    
    public void testSave() {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setDistPoller(getDistPollerDao().load("localhost"));
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(getDistPollerDao().load("localhost"));
        event.setEventCreateTime(new Date());
        event.setEventSeverity(new Integer(7));
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        OnmsNode node = (OnmsNode) getNodeDao().findAll().iterator().next();
        alarm.setNode(node);
        alarm.setUei(event.getEventUei());
        alarm.setSeverity(event.getEventSeverity());
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        
        getAlarmDao().save(alarm);
        //it works we're so smart! hehe
        OnmsAlarm newAlarm = getAlarmDao().load(alarm.getId());
        assertEquals("uei://org/opennms/test/EventDaoTest", newAlarm.getUei());
        assertEquals(alarm.getLastEvent(), newAlarm.getLastEvent());
    }
}
