//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/*
 * Created on Nov 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Event;

/**
 * @author brozow
 * 
 */
public class EventconfFactoryTest extends OpenNMSTestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testDoNothing() {
        // FIXME: This is because the below test is commented out
    }

    public void xtestGetEventsByLabel() {
        List events = EventconfFactory.getInstance().getEventsByLabel();

        ArrayList beforeSort = new ArrayList(events.size());
        Iterator it = events.iterator();
        while (it.hasNext()) {
            Event e = (Event) it.next();
            String label = e.getEventLabel();
            beforeSort.add(label);
        }

        ArrayList afterSort = new ArrayList(beforeSort);
        Collections.sort(afterSort, String.CASE_INSENSITIVE_ORDER);

        assertEquals(beforeSort.size(), afterSort.size());
        for (int i = 0; i < beforeSort.size(); i++) {
            assertEquals("Lists unequals at index " + i, beforeSort.get(i), afterSort.get(i));
        }

    }
    
    public void testGetAlarmType() {
        Event event = new Event();
        AlarmData data = new AlarmData();
        data.setAlarmType(2);
        data.setClearUei("uei.opennms.org.testUei");
        data.setReductionKey("reduceme");
        event.setAlarmData(data);
        
        int i = event.getAlarmData().getAlarmType();
        assertEquals(2, i);
        assertTrue("uei.opennms.org.testUei".equals(event.getAlarmData().getClearUei()));
        assertTrue("reduceme".equals(event.getAlarmData().getReductionKey()));
    }

}
