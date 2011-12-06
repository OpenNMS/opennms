package org.opennms.netmgt.alarmd;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.alarmd.api.Alarm;
import org.opennms.netmgt.alarmd.api.support.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsAlarm;

public class AlarmQueueTest {

    public int alarmNumber = 0;
    
    private Alarm createAlarm() {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(++alarmNumber);
        alarm.setUei("uei.opennms.org/test/httpNorthBounder");
        
        Alarm a = new NorthboundAlarm(alarm);
        return a;
    }
    
    @Test
    public void testRegularForwarding() throws InterruptedException {
        AlarmQueue queue = new AlarmQueue();
        queue.setMaxBatchSize(3);
        queue.init();
    
        queue.accept(createAlarm());
        queue.preserve(createAlarm());
        queue.accept(createAlarm());
        
        List<Alarm> alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(3, alarms.size());
        
        queue.forwardSuccessful(alarms);
        
        queue.preserve(createAlarm());
        queue.accept(createAlarm());
        queue.preserve(createAlarm());
        queue.accept(createAlarm());
        
        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(3, alarms.size());
        
        queue.forwardSuccessful(alarms);
        
        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(1, alarms.size());
        
        queue.forwardSuccessful(alarms);

        
    }

    public void testFailure() throws InterruptedException {
        AlarmQueue queue = new AlarmQueue();
        queue.setMaxBatchSize(3);
        queue.init();
    
        queue.accept(createAlarm());  // 1
        queue.preserve(createAlarm()); // 2
        queue.accept(createAlarm());  // 3
        
        List<Alarm> alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(3, alarms.size());
        
        queue.forwardSuccessful(alarms);
        
        queue.preserve(createAlarm()); // 4
        queue.accept(createAlarm());  // 5
        queue.preserve(createAlarm()); // 6
        queue.accept(createAlarm());  // 7
        
        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(3, alarms.size());
        
        queue.forwardFailed(alarms);
        
        queue.accept(createAlarm()); // 8
        
        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(2, alarms.size());
        assertPreservedAlarm(alarms, 0, 4);
        assertPreservedAlarm(alarms, 1, 6);
        
        queue.forwardFailed(alarms);
        
        queue.preserve(createAlarm()); // 9
        queue.accept(createAlarm()); // 10
        queue.preserve(createAlarm()); // 11
        queue.accept(createAlarm()); // 12
        
        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(3, alarms.size());
        assertPreservedAlarm(alarms, 0, 4);
        assertPreservedAlarm(alarms, 1, 6);
        assertPreservedAlarm(alarms, 2, 9);
        
        queue.forwardSuccessful(alarms);

        queue.preserve(createAlarm()); // 13
        queue.accept(createAlarm()); // 14

        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(1, alarms.size());
        assertPreservedAlarm(alarms, 0, 11);

        queue.forwardSuccessful(alarms);

        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(2, alarms.size());
        assertPreservedAlarm(alarms, 0, 13);
        assertEquals(14, alarms.get(1).getId().intValue());

        queue.forwardSuccessful(alarms);
        
        
    }

    private void assertPreservedAlarm(List<Alarm> alarms, int index, int id) {
        assertTrue(alarms.get(index).isPreserved());
        assertEquals(id, alarms.get(index).getId().intValue());
    }

}
