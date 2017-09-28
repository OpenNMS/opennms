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

package org.opennms.plugins.elasticsearch.rest.archive.cmd;

import java.net.URL;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.plugins.elasticsearch.rest.archive.OpenNMSHistoricEventsToEs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * command example: elastic-search:send-historic-events 100 0 admin admin http://localhost:8980 false
 * this retrieves 110 alarms from the local machine using the local node cache for node label
 * 
 * command example: elastic-search:send-historic-events 100 0 demo demo http://demo.opennms.org true
 * this retrieves 110 alarms from the remote machine using the node label
 * 
 * @author admin
 *
 */
@Command(scope = "elastic-search", name = "send-historic-events", description="Sends events in selected OpenNMS to Elastic Search")
public class SendEventsToEsCommand extends OsgiCommandSupport {
	private static final Logger LOG = LoggerFactory.getLogger(SendEventsToEsCommand.class);

	private OpenNMSHistoricEventsToEs openNMSHistoricEventsToEs;

	public OpenNMSHistoricEventsToEs getOpenNMSHistoricEventsToEs() {
		return openNMSHistoricEventsToEs;
	}

	public void setOpenNMSHistoricEventsToEs(OpenNMSHistoricEventsToEs openNMSHistoricEventsToEs) {
		this.openNMSHistoricEventsToEs = openNMSHistoricEventsToEs;
	}

	@Argument(index = 0, name = "limit", description = "Limit number of events to send", required = true, multiValued = false)
	String limit = null;
	
	@Argument(index = 1, name = "offset", description = "Offset for starting events", required = true, multiValued = false)
	String offset = null;

	@Argument(index = 2, name = "onms-username", description = "rest password for opennms", required = false, multiValued = false)
	String onmsUserName = null;
	
	@Argument(index = 3, name = "onms-password", description = "rest username for opennms", required = false, multiValued = false)
	String onmsPassWord = null;
	
	@Argument(index = 4, name = "onms-url", description = "URL of OpenNMS ReST interface to retrieve events to send", required = false, multiValued = false)
	String onmsUrl = null;
	
	@Argument(index = 5, name = "use-node-label", description = "If false local node cache will get nodelabel for nodeid. If true will use remote nodelabel", required = false, multiValued = false)
	String useNodelabel = "false";

	
	@Override
	protected Object doExecute() throws Exception {
		try{
			
			// use defaults if arguments not set
			if (this.offset!=null)getOpenNMSHistoricEventsToEs().setOffset(Integer.valueOf(offset));
			if (this.limit!=null)getOpenNMSHistoricEventsToEs().setLimit(Integer.valueOf(limit));
			if (this.onmsPassWord!=null) getOpenNMSHistoricEventsToEs().setOnmsPassWord(onmsPassWord);
			if (this.onmsUserName!=null) getOpenNMSHistoricEventsToEs().setOnmsUserName(onmsUserName);
			if (this.onmsUrl!=null){
				URL url = new URL(onmsUrl); // check url is formatted ok
				getOpenNMSHistoricEventsToEs().setOnmsUrl(onmsUrl);
			}
			if (this.useNodelabel!=null) getOpenNMSHistoricEventsToEs().setUseNodeLabel(Boolean.valueOf(useNodelabel));

			
			String msg= "Sending events to Elastic Search. "
					+ "\n Limit ="+getOpenNMSHistoricEventsToEs().getLimit()
					+ "\n Offset ="+getOpenNMSHistoricEventsToEs().getOffset()
					+ "\n Retreiving events from OpenNMS URL="+getOpenNMSHistoricEventsToEs().getOnmsUrl()
					+ "\n OpenNMS Username="+getOpenNMSHistoricEventsToEs().getOnmsUserName()
					+ "\n OpenNMS Password="+getOpenNMSHistoricEventsToEs().getOnmsPassWord()
			        + "\n Use Node Label="+getOpenNMSHistoricEventsToEs().getUseNodeLabel();

			LOG.info(msg);
			System.out.println(msg);
			
			msg = getOpenNMSHistoricEventsToEs().sendEventsToEs();

			LOG.info(msg);
			System.out.println(msg);
		} catch (Exception e) {
			System.err.println("Error Sending Historical Events to ES (see karaf log) "+ExceptionUtils.getStackTrace(e));
			LOG.error("Error Sending Historical Events to ES ",e);
		}
		return null;
	}

}