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

import junit.framework.TestCase;

import org.opennms.netmgt.xml.eventconf.Event;

/**
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EventconfFactoryTest extends TestCase {

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		EventconfFactory.init();
        EventconfFactory.getInstance();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGetEventsByLabel() {
		List events = EventconfFactory.getInstance().getEventsByLabel();
        
        ArrayList beforeSort = new ArrayList(events.size());
        Iterator it = events.iterator();
        while(it.hasNext()) {
        	    Event e = (Event)it.next();
        	    String label = e.getEventLabel();
        	    beforeSort.add(label);
        }
        
        ArrayList afterSort = new ArrayList(beforeSort);
        Collections.sort(afterSort, String.CASE_INSENSITIVE_ORDER);
        
        assertEquals(beforeSort.size(), afterSort.size());
        for(int i = 0; i < beforeSort.size(); i++) {
        	    assertEquals("Lists unequals at index "+i, beforeSort.get(i), afterSort.get(i));
        }
        
	}

}
