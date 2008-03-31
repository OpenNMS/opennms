/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created January 31, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.correlation;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class TestEngine extends AbstractCorrelationEngine {
    
    Integer m_timerId = null;

	public void correlate(Event e) {
		if ("testDown".equals(e.getUei())) {
            EventBuilder bldr = new EventBuilder("testDownReceived", "TestEngine");
            sendEvent(bldr.getEvent());
		}
		else if ("testUp".equals(e.getUei())) {
            EventBuilder bldr = new EventBuilder("testUpReceived", "TestEngine");
            sendEvent(bldr.getEvent());
		}
        else if ("timed".equals(e.getUei())) {
            m_timerId = setTimer(1000);
        }
        else if ("cancelTimer".equals(e.getUei())) {
            cancelTimer(m_timerId);
        }
		else {
			throw new IllegalArgumentException("Unexpected event with uei = "+e.getUei());
		}
		
	}
    
	public List<String> getInterestingEvents() {
		List<String> ueis = new ArrayList<String>();
		ueis.add("testDown");
		ueis.add("testUp");
        ueis.add("timed");
        ueis.add("cancelTimer");
		return ueis;
	}

    @Override
    protected void timerExpired(Integer timerId) {
        EventBuilder bldr = new EventBuilder("timerExpired", "TestEngine");
        sendEvent(bldr.getEvent());
    }

    public String getName() {
        return "TestEngine";
    }

}
