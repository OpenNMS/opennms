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
// Modifications:
//
// 2007 Aug 24: Eliminate warnings, use Java 5 generics and loops, and use
//              DaoTestConfigBean instead of calling
//              System.setProperty("opennms.home", ...). - dj@opennms.org
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
 */
package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.test.DaoTestConfigBean;

/**
 * @author brozow
 * 
 */
public class EventconfFactoryTest extends OpenNMSTestCase {

    private static final String knownUEI1="uei.opennms.org/internal/capsd/snmpConflictsWithDb";
    private static final String knownLabel1="OpenNMS-defined capsd event: snmpConflictsWithDb";
    private static final String knownSubfileUEI1="uei.opennms.org/IETF/Bridge/traps/newRoot";
    private static final String knownSubfileLabel1="BRIDGE-MIB defined trap event: newRoot";
    private static final String knownSubSubfileUEI1="uei.opennms.org/IETF/Bridge/traps/topologyChange";
    private static final String knownSubSubfileLabel1="BRIDGE-MIB defined trap event: topologyChange";
    
    public EventconfFactoryTest() {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/resources");
        daoTestConfig.afterPropertiesSet();
    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        EventconfFactory.init();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetEventsByLabel() {
        List<Event> events = getEventsByLabel();

        ArrayList<String> beforeSort = new ArrayList<String>(events.size());
        for (Event e : events) {
            String label = e.getEventLabel();
            beforeSort.add(label);
        }

        ArrayList<String> afterSort = new ArrayList<String>(beforeSort);
        Collections.sort(afterSort, String.CASE_INSENSITIVE_ORDER);

        assertEquals(beforeSort.size(), afterSort.size());
        for (int i = 0; i < beforeSort.size(); i++) {
            assertEquals("Lists unequals at index " + i, beforeSort.get(i), afterSort.get(i));
        }

    }
    
    @SuppressWarnings("unchecked")
    private List<Event> getEventsByLabel() {
        return EventconfFactory.getInstance().getEventsByLabel();
    }
    
    public void testGetEventByUEI() {
        EventconfFactory factory=EventconfFactory.getInstance();
        List result=factory.getEvents(knownUEI1);
        assertEquals("Should only be one result", 1, result.size());
        Event firstEvent=(Event)result.get(0);
        assertEquals("UEI should be "+knownUEI1, knownUEI1, firstEvent.getUei());
        
        result=factory.getEvents("uei.opennms.org/internal/capsd/nonexistent");
        assertNull("Should be null list for non-existent URI", result);
        
        //Find an event that's in a sub-file
        result=factory.getEvents(knownSubfileUEI1);
        assertEquals("Should only be one result", 1, result.size());
        firstEvent=(Event)result.get(0);
        assertEquals("UEI should be "+knownSubfileUEI1,knownSubfileUEI1, firstEvent.getUei());
 
        //Find an event that's in a nested-sub-file
        result=factory.getEvents(knownSubSubfileUEI1);
        assertNotNull(result);
        assertEquals("Should only be one result", 1, result.size());
        firstEvent=(Event)result.get(0);
        assertEquals("UEI should be "+knownSubSubfileUEI1,knownSubSubfileUEI1, firstEvent.getUei());

    }
    
    public void testGetEventUEIS() {
        List ueis=EventconfFactory.getInstance().getEventUEIs();
        //This test assumes the test eventconf files only have X events in them.  Adjust as you modify eventconf.xml and sub files
        assertEquals("Count must be correct", 3, ueis.size());
        assertTrue("Must contain known UEI", ueis.contains(knownUEI1));
        assertTrue("Must contain known UEI", ueis.contains(knownSubfileUEI1));
        assertTrue("Must contain known UEI", ueis.contains(knownSubSubfileUEI1));
    }
    
    public void testGetLabels() {
        Map labels=EventconfFactory.getInstance().getEventLabels();
        //This test assumes the test eventconf files only have X events in them.  Adjust as you modify eventconf.xml and sub files
        assertEquals("Count must be correct", 3, labels.size());
        assertTrue("Must contain known UEI", labels.containsKey(knownUEI1));
        assertEquals("Must have known Label", labels.get(knownUEI1), knownLabel1);
        assertTrue("Must contain known UEI", labels.containsKey(knownSubfileUEI1));
        assertEquals("Must have known Label", labels.get(knownSubfileUEI1), knownSubfileLabel1);
        assertTrue("Must contain known UEI", labels.containsKey(knownSubSubfileUEI1));
        assertEquals("Must have known Label", labels.get(knownSubSubfileUEI1), knownSubSubfileLabel1);
     }
    public void testGetLabel() {
        EventconfFactory factory = EventconfFactory.getInstance();
        assertEquals("Must have correct label"+knownLabel1, knownLabel1, factory.getEventLabel(knownUEI1));
        assertEquals("Must have correct label"+knownSubfileLabel1, knownSubfileLabel1, factory.getEventLabel(knownSubfileUEI1));
        assertEquals("Must have correct label"+knownSubSubfileLabel1, knownSubSubfileLabel1, factory.getEventLabel(knownSubSubfileUEI1));
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
    
    //Ensure reload does indeed reload fresh data
    public void testReload() {
        String newUEI="uei.opennms.org/custom/newTestUEI";
        EventconfFactory factory=EventconfFactory.getInstance();
        
        List events=factory.getEvents(knownUEI1);
        Event event=(Event)events.get(0);
        event.setUei(newUEI);
        
        //Check that the new UEI is there
        List events2=factory.getEvents(newUEI);
        Event event2=((Event)events2.get(0));
        assertNotNull("Must have some events", event2);
        assertEquals("Must be exactly 1 event", 1, events2.size());
        assertEquals("uei must be the new one", newUEI, event2.getUei());
        

        //Now reload without saving - should not find the new one, but should find the old one
        try {
            EventconfFactory.reload();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should not have had exception while reloading factory "+e.getMessage());
        }
        List events3=factory.getEvents(knownUEI1);
        assertNotNull("Must have some events", events3);
        assertEquals("Must be exactly 1 event", 1, events3.size());
        Event event3=(Event)events3.get(0);
        assertEquals("uei must be the new one", knownUEI1, event3.getUei());       
        
        //Check that the new UEI is *not* there this time
        List events4=factory.getEvents(newUEI);
        assertNull("Must be no events by that name", events4);
    }
    

}
