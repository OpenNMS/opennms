package org.opennms.netmgt.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.UpdateField;

import static org.junit.Assert.*;

public class EventTranslatorConfigFactoryTest {

    private static String SEVERITY = "severity";
    private static String LOG_MSG = "logmsg";
    
    private Event event;
    
    @Before
    public void setUpTestCloneEvent() {
        event = new Event();
        AlarmData alarmData = new AlarmData();
       
        List<UpdateField> updateFields = new ArrayList<UpdateField>();
       
        UpdateField field1 = new UpdateField();
        field1.setFieldName(SEVERITY);
        
        UpdateField field2 = new UpdateField();
        field2.setFieldName(LOG_MSG);
        
        updateFields.add(field1);
        updateFields.add(field2);
        
        alarmData.setUpdateFieldCollection(updateFields);
        
        event.setAlarmData(alarmData);
    }

    @Test
    public void testCloneEvent() {
        assertTrue(event instanceof Serializable);
        
        Event copy = EventTranslatorConfigFactory.cloneEvent(event);
        
        assertNotNull(copy);
                
    }

}
