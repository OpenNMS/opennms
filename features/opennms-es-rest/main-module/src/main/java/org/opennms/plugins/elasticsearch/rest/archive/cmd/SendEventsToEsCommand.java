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
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.plugins.elasticsearch.rest.archive.OpenNMSHistoricEventsToEs;
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
 * <p>This command implements the Apache Karaf 3 and Apache Karaf 4 shell APIs.
 * Once the Karaf 4 commands work, the deprecated Karaf 3 annotations should 
 * be removed:</p>
 * <ul>
 * <li>{@link org.apache.karaf.shell.commands.Command}</li>
 * <li>{@link org.apache.karaf.shell.console.OsgiCommandSupport}</li>
 * </ul>
 * 
 * @author Craig Gallen <cgallen@opennms.org>
 */
@Command(scope = "elasticsearch", name = "send-historic-events", description="Sends events in selected OpenNMS to Elasticsearch")
@org.apache.karaf.shell.commands.Command(scope = "elasticsearch", name = "send-historic-events", description="Sends events in selected OpenNMS to Elasticsearch")
@Service
public class SendEventsToEsCommand extends OsgiCommandSupport implements Action {

	private static final Logger LOG = LoggerFactory.getLogger(SendEventsToEsCommand.class);

	@Reference
	private OpenNMSHistoricEventsToEs openNMSHistoricEventsToEs;

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
	public Object execute() throws Exception {
		try{
			
			// use defaults if arguments not set
			if (this.offset!=null)openNMSHistoricEventsToEs.setOffset(Integer.valueOf(offset));
			if (this.limit!=null)openNMSHistoricEventsToEs.setLimit(Integer.valueOf(limit));
			if (this.onmsPassWord!=null) openNMSHistoricEventsToEs.setOnmsPassWord(onmsPassWord);
			if (this.onmsUserName!=null) openNMSHistoricEventsToEs.setOnmsUserName(onmsUserName);
			if (this.onmsUrl!=null){
				URL url = new URL(onmsUrl); // check url is formatted ok
				openNMSHistoricEventsToEs.setOnmsUrl(onmsUrl);
			}
			if (this.useNodelabel!=null) openNMSHistoricEventsToEs.setUseNodeLabel(Boolean.valueOf(useNodelabel));

			
			String msg= "Sending events to Elasticsearch. "
					+ "\n Limit ="+openNMSHistoricEventsToEs.getLimit()
					+ "\n Offset ="+openNMSHistoricEventsToEs.getOffset()
					+ "\n Retreiving events from OpenNMS URL="+openNMSHistoricEventsToEs.getOnmsUrl()
					+ "\n OpenNMS Username="+openNMSHistoricEventsToEs.getOnmsUserName()
					+ "\n OpenNMS Password="+openNMSHistoricEventsToEs.getOnmsPassWord()
			        + "\n Use Node Label="+openNMSHistoricEventsToEs.getUseNodeLabel();

			LOG.info(msg);
			System.out.println(msg);
			
			msg = openNMSHistoricEventsToEs.sendEventsToEs();

			LOG.info(msg);
			System.out.println(msg);
		} catch (Exception e) {
			System.err.println("Error Sending Historical Events to ES (see karaf log) "+ExceptionUtils.getStackTrace(e));
			LOG.error("Error Sending Historical Events to ES ",e);
		}
		return null;
	}

	@Override
	@Deprecated
	protected Object doExecute() throws Exception {
		return execute();
	}

	@Deprecated
	public void setOpenNMSHistoricEventsToEs(OpenNMSHistoricEventsToEs openNMSHistoricEventsToEs) {
		this.openNMSHistoricEventsToEs = openNMSHistoricEventsToEs;
	}

}
