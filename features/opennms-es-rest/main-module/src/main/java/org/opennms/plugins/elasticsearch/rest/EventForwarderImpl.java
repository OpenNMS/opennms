/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventForwarderImpl implements EventForwarder {


	private static final Logger LOG = LoggerFactory.getLogger(EventForwarderImpl.class);

	private EventToIndex eventToIndex=null;
	
	public EventToIndex getEventToIndex() {
		return eventToIndex;
	}

	public void setEventToIndex(EventToIndex eventToIndex) {
		this.eventToIndex = eventToIndex;
	}
	
	@Override
	public void sendNow(Event event) {		
		LOG.debug("Event to send received: " + event.toString());
		if (eventToIndex!=null) eventToIndex.forwardEvent(event);
	}

	@Override
	public void sendNow(Log arg0) {
		// TODO Auto-generated method stub

	}

    @Override
    public void sendNowSync(Event event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendNowSync(Log eventLog) {
        throw new UnsupportedOperationException();
    }

}
