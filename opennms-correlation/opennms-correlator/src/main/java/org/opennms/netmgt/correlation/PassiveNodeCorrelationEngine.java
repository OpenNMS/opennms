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
	package org.opennms.netmgt.correlation;

import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class PassiveNodeCorrelationEngine implements CorrelationEngine, InitializingBean {
	
	public class InitialState implements State {

		public State processEvent(Event e) {
			throw new UnsupportedOperationException("InitialState.processEvent not yet implemented.");
		}

		public boolean isEndState() {
			throw new UnsupportedOperationException("InitialState.isEndState not yet implemented.");
		}

		public State processTimerExpired(int timerId) {
			throw new UnsupportedOperationException("InitialState.processTimerExpired not yet implemented.");
		}

	}

	CorrelationServices m_correlationServices;
	StateKeeper m_stateKeeper;


	public void setCorrelationServices(CorrelationServices correlationServices) {
		m_correlationServices = correlationServices;
	}
	
	public void setStateKeeper(StateKeeper stateKeeper) {
		m_stateKeeper = stateKeeper;
	}

	public void correlate(Event e) {
		Assert.notNull(e, "Event e may not be null");
		Assert.isTrue(getInterestingEvents().contains(e.getUei()), "Uninteresting event received: "+e.getUei());
		
		int nodeId = (int)e.getNodeid();

		State currentState = getCurrentState(nodeId);
		
		State newState = currentState.processEvent(e);
		
		saveNewState(nodeId, newState);

	}
	
	private void saveNewState(int nodeId, State newState) {
		if (newState.isEndState()) {
			m_stateKeeper.removeStateForNode(nodeId);
		} else {
			m_stateKeeper.setStateForNode(nodeId, newState);
		}
	}

	private State getCurrentState(int nodeId) {
		State currentState = m_stateKeeper.getStateForNode(nodeId);
		if (currentState == null) {
			currentState = new InitialState();
		}
		return currentState;
	}

	public List<String> getInterestingEvents() {
		String[] ueis = new String[] {
			EventConstants.NODE_DOWN_EVENT_UEI	
		};
		return Arrays.asList(ueis);
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(m_correlationServices, "the correlationServices property must be set");
	}

}

	