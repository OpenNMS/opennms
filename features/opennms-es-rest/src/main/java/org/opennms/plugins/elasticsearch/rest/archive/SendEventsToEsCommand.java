/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
 * <p>Command example: {@code opennms:send-events-to-elasticsearch 100 0 admin admin http://localhost:8980 false}</p>
 * 
 * <p>This retrieves 110 alarms from the local machine using the local node cache for node label.</p>
 * 
 * <p>Command example: {@code opennms:send-events-to-elasticsearch 100 0 demo demo http://demo.opennms.org true}</p>
 * 
 * <p>This retrieves 110 alarms from the remote machine using the node label.</p>
 * 
 * @author Craig Gallen <cgallen@opennms.org>
 */
@Command(scope = "opennms", name = "send-events-to-elasticsearch", description="Sends events in selected OpenNMS to Elasticsearch")
@Service
public class SendEventsToEsCommand implements Action {

	private static final Logger LOG = LoggerFactory.getLogger(SendEventsToEsCommand.class);

	@Reference
	private EventForwarder eventForwarder;

	@Option(name = "--limit", description = "Limit number of events to send. Use 0 to send ALL events.")
	int limit = 1000;

	@Option(name = "--offset", description = "Offset for starting events")
	int offset = 0;

	@Option(name = "--username", description = "Username for OpenNMS ReST interface")
	String username = null;

	@Option(name = "--password", description = "Password for OpenNMS ReST interface")
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
