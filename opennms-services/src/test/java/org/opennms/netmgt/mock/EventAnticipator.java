//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Aug 24: Add the ability to reset either of the anticipated
//              and unanticipated lists individually. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.xml.event.Event;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class EventAnticipator implements EventListener {

    List m_anticipatedEvents = new ArrayList();

    List m_unanticipatedEvents = new ArrayList();

    /**
     */
    public EventAnticipator() {
    }

    /**
     * @param event
     * 
     */
    public void anticipateEvent(Event event) {
        m_anticipatedEvents.add(new EventWrapper(event));
    }

    /**
     * @param event
     */
    public synchronized void eventReceived(Event event) {
        EventWrapper w = new EventWrapper(event);
        if (m_anticipatedEvents.contains(w)) {
            m_anticipatedEvents.remove(w);
            notifyAll();
        } else {
            m_unanticipatedEvents.add(event);
        }
    }

    public synchronized Collection getAnticipatedEvents() {
        List events = new ArrayList(m_anticipatedEvents.size());
        Iterator it = m_anticipatedEvents.iterator();
        while (it.hasNext()) {
            EventWrapper w = (EventWrapper) it.next();
            events.add(w.getEvent());
        }
        return events;
    }

    public void reset() {
        resetAnticipated();
        resetUnanticipated();
    }

    public void resetUnanticipated() {
        m_unanticipatedEvents = new ArrayList();
    }

    public void resetAnticipated() {
        m_anticipatedEvents = new ArrayList();
    }

    /**
     * @return
     */
    public Collection unanticipatedEvents() {
        return Collections.unmodifiableCollection(m_unanticipatedEvents);
    }

    /**
     * @param i
     * @return
     */
    public synchronized Collection waitForAnticipated(long millis) {
        long waitTime = millis;
        long start = System.currentTimeMillis();
        long now = start;
        while (waitTime > 0) {
            if (m_anticipatedEvents.isEmpty())
                return Collections.EMPTY_LIST;
            try {
                wait(waitTime);
            } catch (InterruptedException e) {
            }
            now = System.currentTimeMillis();
            waitTime -= (now - start);
        }
        return getAnticipatedEvents();
    }

    /**
     * @param event
     */
    public void eventProcessed(Event event) {
    }

	public void verifyAnticipated(long wait,
			long sleepMiddle,
			long sleepAfter,
			int anticipatedSize,
			int unanticipatedSize) {
		
		StringBuffer problems = new StringBuffer();

		Collection missingEvents = waitForAnticipated(wait);
		
		if (sleepMiddle > 0) {
			try {
				Thread.sleep(sleepMiddle);
			} catch (InterruptedException e) {
			}
		}

		if (missingEvents.size() != anticipatedSize) {
			problems.append(missingEvents.size() +
					" expected events still outstanding (expected " +
					anticipatedSize + "):\n");
			problems.append(listEvents("\t", missingEvents));
		}
		if (unanticipatedEvents().size() != unanticipatedSize) {
			problems.append(unanticipatedEvents().size() +
					" unanticipated events received (expected " +
					unanticipatedSize + "):\n");
			problems.append(listEvents("\t", unanticipatedEvents()));
		}
		
		if (problems.length() > 0) {
			problems.deleteCharAt(problems.length() - 1);
			Assert.fail(problems.toString());
		}
	}

	private static String listEvents(String prefix,
			Collection events) {
		StringBuffer b = new StringBuffer();
		
		for (Iterator it = events.iterator(); it.hasNext();) {
			Event event = (Event) it.next();
			b.append(prefix);
			b.append(event.getUei() + "/" + event.getNodeid() + "/" + event.getInterface() + "/" + event.getService());
			b.append("\n");
		}

		return b.toString();
	}

    public String getName() {
        return "eventAnticipator";
    }

    public void onEvent(Event e) {
        eventReceived(e);
    }

}
