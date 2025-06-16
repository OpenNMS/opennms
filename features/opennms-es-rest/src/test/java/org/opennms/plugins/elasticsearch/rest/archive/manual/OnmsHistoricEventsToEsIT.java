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
package org.opennms.plugins.elasticsearch.rest.archive.manual;

import org.junit.Test;
import org.junit.Ignore;
import org.opennms.plugins.elasticsearch.rest.EventForwarderImpl;
import org.opennms.plugins.elasticsearch.rest.archive.OnmsHistoricEventsToEs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore("manual test meant to be run against a real elasticsearch")
public class OnmsHistoricEventsToEsIT {
	private static final Logger LOG = LoggerFactory.getLogger(OnmsHistoricEventsToEsIT.class);
	
	private String onmsUrl="http://localhost:8980";

	private String onmsUserName="admin";

	private String onmsPassWord="admin";
	
	private Integer limit = 100;
	
	private Integer offset = 0;
	
	@Test
	public void test() {
		LOG.debug("start of test OnmsHistoricEventsToEsTest");
		
		OnmsHistoricEventsToEs eventsToES= new OnmsHistoricEventsToEs();
		
		eventsToES.setOnmsUserName(onmsUserName);
		
		eventsToES.setOnmsPassWord(onmsPassWord);
		
		eventsToES.setOnmsUrl(onmsUrl);
		
		eventsToES.setLimit(limit);
		
		eventsToES.setOffset(offset);
		
		EventForwarderImpl eventForwarder = new EventForwarderImpl();
		
		eventsToES.setEventForwarder(eventForwarder);
		
		String msg = eventsToES.sendEventsToEs();
		
		LOG.debug("message from forwarder: "+msg);

		LOG.debug("end of test OnmsHistoricEventsToEsTest");
	}

}
