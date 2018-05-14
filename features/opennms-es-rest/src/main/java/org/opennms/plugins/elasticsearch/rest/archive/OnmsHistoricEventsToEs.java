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

package org.opennms.plugins.elasticsearch.rest.archive;

import java.util.Date;
import java.util.List;

import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Header;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * takes events from OpenNMS historic events through the OpennMS
 * rest interface and sends them to Elasticsearch using the same queue
 * as live incoming events use.
 * @author admin
 *
 */
public class OnmsHistoricEventsToEs {
	private static final Logger LOG = LoggerFactory.getLogger(OnmsHistoricEventsToEs.class);

	private String onmsUrl="http://localhost:8980";

	private String onmsUserName="admin";

	private String onmsPassWord="admin";

	private Integer limit=10;

	private Integer offset=0;

	private EventForwarder eventForwarder=null;
	
	private boolean useNodeLabel=true;

	private int logSize = 100;

	public String getOnmsUrl() {
		return onmsUrl;
	}

	public void setOnmsUrl(String onmsUrl) {
		this.onmsUrl = onmsUrl;
	}

	public String getOnmsUserName() {
		return onmsUserName;
	}

	public void setOnmsUserName(String onmsUserName) {
		this.onmsUserName = onmsUserName;
	}

	public String getOnmsPassWord() {
		return onmsPassWord;
	}

	public void setOnmsPassWord(String onmsPassWord) {
		this.onmsPassWord = onmsPassWord;
	}

	public EventForwarder getEventForwarder() {
		return eventForwarder;
	}

	public void setEventForwarder(EventForwarder eventForwarder) {
		this.eventForwarder = eventForwarder;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	
	public boolean getUseNodeLabel() {
		return useNodeLabel;
	}

	public void setUseNodeLabel(boolean useCache) {
		this.useNodeLabel = useCache;
	}

	public void setLogSize(int logSize) {
		this.logSize = logSize;
	}

	/**
	 * sends events to Elasticsearch returns true if successful
	 * @return
	 */
	public String sendEventsToEs(){
		final OnmsRestEventsClient onmsRestEventsClient = new OnmsRestEventsClient(onmsUrl, onmsPassWord, onmsUserName);
		Event firstEvent = null;
		Event lastEvent = null;

		boolean endofEvents = false;
		int eventsSent = 0;

		int eventOffset=offset;

		while (!endofEvents && eventsSent <= limit){
			final List<Event> events = onmsRestEventsClient.getEvents(logSize, eventOffset);
			endofEvents = events.isEmpty();
			if (!endofEvents) {
				if (firstEvent == null) {
					firstEvent = events.get(0);
				}

				// remove node label param if included in event
				if (!useNodeLabel) {
					events.forEach(event -> {
						final Parm parm = event.getParm(OnmsRestEventsClient.NODE_LABEL);
						if (parm != null) {
							event.getParmCollection().remove(parm);
						}
					});
				}

				if (LOG.isDebugEnabled()) {
					events.forEach(event -> {
						LOG.debug("sending event to es: eventid={}", event.getDbid());
					});
				}
				lastEvent = events.get(events.size() - 1);

				final Log log = createLog(events);
				getEventForwarder().sendNow(log);
				eventsSent += log.getEvents().getEventCount();
				eventOffset = eventOffset + events.size();
			}
		}

		return "Dispatched "+eventsSent
				+ " events to forward to Elasticsearch. First event "
				+ "id="+((firstEvent!=null) ? firstEvent.getDbid() : "firstEvent null")
				+ " last event id="+((lastEvent!=null) ? lastEvent.getDbid() : "lastEvent null");
	}

	private Log createLog(List<Event> eventList) {
		final Header header = new Header();
		header.setCreated(StringUtils.toStringEfficiently(new Date()));

		final Events events = new Events();
		events.setEvent(eventList);

		final Log log = new Log();
		log.setEvents(events);
		log.setHeader(header);
		return log;
	}

}
