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

import java.net.URL;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.events.api.EventForwarder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Command example: {@code elasticsearch:send-historic-events 100 0 admin admin http://localhost:8980 false}</p>
 * 
 * <p>This retrieves 110 alarms from the local machine using the local node cache for node label.</p>
 * 
 * <p>Command example: {@code elasticsearch:send-historic-events 100 0 demo demo http://demo.opennms.org true}</p>
 * 
 * <p>This retrieves 110 alarms from the remote machine using the node label.</p>
 * 
 * @author Craig Gallen <cgallen@opennms.org>
 */
@Command(scope = "elasticsearch", name = "send-historic-events", description="Sends events in selected OpenNMS to Elasticsearch")
@Service
public class SendEventsToEsCommand implements Action {

	private static final Logger LOG = LoggerFactory.getLogger(SendEventsToEsCommand.class);

	@Reference
	private EventForwarder eventForwarder;

	@Option(name = "--limit", description = "Limit number of events to send. Use 0 to send ALL events.")
	int limit = 1000;

	@Option(name = "--offset", description = "Offset for starting events")
	int offset = 0;

	@Option(name = "--username", description = "Password for OpenNMS ReST interface")
	String username = null;

	@Option(name = "--password", description = "Username for OpenNMS ReST interface")
	String password = null;

	@Option(name = "--url", description = "URL of OpenNMS ReST interface to retrieve events to send")
	String url = null;

	@Option(name = "--use-node-label", description = "If false local node cache will get nodelabel for nodeid. If true will use remote nodelabel")
	boolean useNodelabel = false;

	@Option(name ="--log-size", description = "The size of the number of events to dispatch at once to elastic.")
	int logSize = 200;

	@Override
	public Object execute() {
		try {
			final OnmsHistoricEventsToEs onmsHistoryEventsToEs = new OnmsHistoricEventsToEs();
			onmsHistoryEventsToEs.setEventForwarder(eventForwarder);
			onmsHistoryEventsToEs.setLimit(limit);
			if (limit > 0) {
				onmsHistoryEventsToEs.setOffset(offset);
			}
			if (this.password != null) {
				onmsHistoryEventsToEs.setOnmsPassWord(password);
			}
			if (this.username != null) {
				onmsHistoryEventsToEs.setOnmsUserName(username);
			}
			if (this.url != null){
				new URL(this.url); // check url is formatted ok
				onmsHistoryEventsToEs.setOnmsUrl(this.url);
			}
			if (this.useNodelabel) {
				onmsHistoryEventsToEs.setUseNodeLabel(useNodelabel);
			}
			if (logSize > 0) {
				onmsHistoryEventsToEs.setLogSize(logSize);
			}
			final String msg = "Sending events to Elasticsearch. "
					+ "\n Limit: "+onmsHistoryEventsToEs.getLimit()
					+ "\n Offset: "+onmsHistoryEventsToEs.getOffset()
					+ "\n Retrieving events from OpenNMS URL: "+onmsHistoryEventsToEs.getOnmsUrl()
					+ "\n OpenNMS Username: "+onmsHistoryEventsToEs.getOnmsUserName()
					+ "\n OpenNMS Password: "+onmsHistoryEventsToEs.getOnmsPassWord()
			        + "\n Use Node Label: "+onmsHistoryEventsToEs.getUseNodeLabel()
					+ "\n Log Size: " + logSize;
			LOG.info(msg);
			System.out.println(msg);
			
			final String response = onmsHistoryEventsToEs.sendEventsToEs();
			LOG.info(response);
			System.out.println(response);
		} catch (Exception e) {
			System.err.println("Error Sending Historical Events to ES (see karaf.log) "+ExceptionUtils.getStackTrace(e));
			LOG.error("Error Sending Historical Events to ES ",e);
		}
		return null;
	}
}
