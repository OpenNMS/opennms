/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 9, 2010
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.tools.spectrum;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.FileSystemResource;

/**
 * @author jeffg
 *
 */
public class EventDispositionReaderTest {
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }
    
    @Test
    public void oneArgConstructor() throws IOException {
        @SuppressWarnings("unused")
        EventDispositionReader reader = new EventDispositionReader(new FileSystemResource("src/test/resources/sonus-traps/EventDisp"));
    }
    
    @Test
    public void readSonusEventDisposition() throws IOException {
        EventDispositionReader reader = new EventDispositionReader(new FileSystemResource("src/test/resources/sonus-traps/EventDisp"));
        List<EventDisposition> dispositions = reader.getEventDispositions();
        
        int alarmFreeDispositions = 0;
        int alarmCreateDispositions = 0;
        int alarmClearDispositions = 0;
        
        Assert.assertEquals("There should exist 757 event-dispositions in this EventDisp file", 757, dispositions.size());
        
        for (EventDisposition disposition : dispositions) {
            if (disposition.isCreateAlarm()) {
                alarmCreateDispositions++;
            } else if (disposition.isClearAlarm()) {
                alarmClearDispositions++;
            } else {
                alarmFreeDispositions++;
            }
        }

        Assert.assertEquals("321 event-dispositions should neither create nor clear an alarm", 321, alarmFreeDispositions);
        Assert.assertEquals("379 event-dispositions should create an alarm", 379, alarmCreateDispositions);
        Assert.assertEquals("57 event-dispositions should clear an alarm", 57, alarmClearDispositions);
        
        Assert.assertEquals("First event-disposition is for event-code 0xfff00000", "0xfff00000", dispositions.get(0).getEventCode());
        Assert.assertEquals("First event-disposition specifies event logging", true, dispositions.get(0).isLogEvent());
        Assert.assertEquals("First event-disposition specifies creating an alarm", true, dispositions.get(0).isCreateAlarm());
        Assert.assertEquals("First event-disposition specifies an alarm-severity of 3", 3, dispositions.get(0).getAlarmSeverity());
        Assert.assertEquals("First event-disposition specifies an alarm-cause of 0xfff00000", "0xfff00000", dispositions.get(0).getAlarmCause());
        
        Assert.assertEquals("Second event-disposition is for event-code 0xfff00000", "0xfff00000", dispositions.get(1).getEventCode());
        Assert.assertEquals("Second event-disposition specifies event logging", true, dispositions.get(1).isLogEvent());
        Assert.assertEquals("Second event-disposition specifies clearing an alarm", true, dispositions.get(1).isClearAlarm());
        Assert.assertEquals("Second event-disposition specifies a clear-alarm-cause of 0xfff00001", "0xfff00001", dispositions.get(1).getClearAlarmCause());
        
        Assert.assertEquals("Fifth event-disposition is for event-code 0xfff00002", "0xfff00002", dispositions.get(4).getEventCode());
        Assert.assertEquals("Fifth event-disposition specifies event logging", true, dispositions.get(4).isLogEvent());
        Assert.assertEquals("Fifth event-disposition specifies clearing an alarm", true, dispositions.get(4).isClearAlarm());
        Assert.assertEquals("Fifth event-disposition specifies a clear-alarm-cause of 0xfff00000", "0xfff00000", dispositions.get(4).getClearAlarmCause());

    }
}
