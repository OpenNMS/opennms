/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;

public class TestEngine extends AbstractCorrelationEngine implements InitializingBean {
    
    Integer m_timerId = null;
    CorrelationEngineRegistrar m_correlator;

    @Override
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
    
    @Override
	public List<String> getInterestingEvents() {
		List<String> ueis = new ArrayList<>();
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

    @Override
    public String getName() {
        return "TestEngine";
    }

    @Override
    public void tearDown() {
        // pass
    }

    @Override
    public void reloadConfig() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        m_correlator.addCorrelationEngine(this);
    }

    public void setCorrelator(CorrelationEngineRegistrar correlator) {
        m_correlator = correlator;
    }

}
