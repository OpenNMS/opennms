package org.opennms.netmgt.dao;

import java.util.Date;

import org.junit.Test;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.Acknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;

public class AcknowledgmentDaoTest extends AbstractTransactionalDaoTestCase {

    @Test
    public void testSaveUnspecified() {
        Acknowledgment ack = new Acknowledgment();
        ack.setAckTime(new Date());
        ack.setAckType(AckType.Unspecified);
        ack.setAckUser("not-admin");
        getAcknowledgmentDao().save(ack);
        getAcknowledgmentDao().flush();
        Integer id = new Integer(ack.getId());
        ack = null;
        
        Acknowledgment ack2 = getAcknowledgmentDao().get(id);
        assertNotNull(ack2);
        assertEquals(id, ack2.getId());
        assertFalse("admin".equals(ack2.getAckUser()));
        assertEquals("not-admin", ack2.getAckUser());
        
    }

    @Test
    public void testSaveWithAlarm() {
        OnmsEvent event = new OnmsEvent();
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventCreateTime(new Date());
        event.setDistPoller(getDistPollerDao().load("localhost"));
        event.setEventTime(new Date());
        event.setEventSeverity(new Integer(7));
        event.setEventUei("uei://org/opennms/test/EventDaoTest");
        event.setEventSource("test");
        getEventDao().save(event);
        
        OnmsNode node = getNodeDao().findAll().iterator().next();

        OnmsAlarm alarm = new OnmsAlarm();
        
        alarm.setNode(node);
        alarm.setUei(event.getEventUei());
        alarm.setSeverityId(event.getEventSeverity());
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setCounter(new Integer(1));
        alarm.setDistPoller(getDistPollerDao().load("localhost"));
        alarm.setAlarmAckTime(new Date());
        alarm.setAlarmAckUser("not-admin");
        
        getAlarmDao().save(alarm);
        //getAlarmDao().flush();
        
        Acknowledgment ack = new Acknowledgment(alarm);
        getAcknowledgmentDao().save(ack);
        Integer ackId = new Integer(ack.getId());
        ack = null;
        
        Acknowledgment ack2 = getAcknowledgmentDao().get(ackId);
        OnmsAlarm alarm2 = getAlarmDao().get(ack2.getRefId());
        
        assertEquals(ack2.getAckUser(), alarm2.getAlarmAckUser());
        assertEquals(ack2.getAckTime(), alarm2.getAlarmAckTime());
        
    }

    /*
    @Test
    public void testAcknowledgmentDaoHibernate() {
        fail("Not yet implemented");
    }

    @Test
    public void testFindAcknowledgables() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpdateAckable() {
        fail("Not yet implemented");
    }
    */

}
