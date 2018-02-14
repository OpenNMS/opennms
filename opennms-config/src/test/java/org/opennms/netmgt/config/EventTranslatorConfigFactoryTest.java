/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
       
        List<UpdateField> updateFields = new ArrayList<>();
       
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
