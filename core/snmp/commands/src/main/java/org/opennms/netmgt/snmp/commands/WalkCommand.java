/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.snmp.commands;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.ColumnTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "snmp", name = "walk", description="Walk the agent on the specified host and print the results.")
public class WalkCommand extends OsgiCommandSupport {
	
	private static final transient Logger LOG = LoggerFactory.getLogger(WalkCommand.class);
	
	@Option(name="-v", aliases="--version", description="SNMP version either 1, 2c or 3", required=true, multiValued=false)
	String m_version;
	
	@Option(name="-c", aliases="--community", description="SNMP community string to use, defaults to 'public'", required=false, multiValued=false)
	String m_community="public";
	
	@Option(name="-p", aliases="--port", description="port to use to address the agent defaults to 161", required=false, multiValued=false)
	int m_port = 161;

	@Argument(index = 0, name = "host", description = "hostname/ipAddress of the system to walk", required = true, multiValued = false)
	String m_host;
	
	@Argument(index = 1, name = "oids", description = "list of objectIds to retrieve from the agent", required = true, multiValued = true)
	List<String> m_oids;
	
	private boolean validate() {
		return true;
	}
	

    @Override
    protected Object doExecute() throws Exception {
    	
    	LOG.debug("snmp:walk -v {} -c {} -p {} {} {}", m_version, m_community, m_port, m_host, m_oids);
    	
    	if (!validate()) {
    		return null;
    	}
    	
    	SnmpAgentConfig config = new SnmpAgentConfig(InetAddress.getByName(m_host));
    	config.setPort(m_port);
    	config.setVersionAsString("v"+m_version);
    	config.setReadCommunity(m_community);
    	
    	Collection<Collectable> trackers = new ArrayList<Collectable>();
    	
    	for(String oid : m_oids) {
    		SnmpObjId objId = SnmpObjId.get(oid);
    		ColumnTracker tracker = new ColumnTracker(objId);
    		trackers.add(tracker);
    	}
    	
    	CollectionTracker agg = new AggregateTracker(trackers) {

			@Override
			protected void storeResult(SnmpResult res) {
				System.out.printf("[%s].[%s] = %s%n", res.getBase(), res.getInstance(), res.getValue());
			}
    		
    	};
    	
    	SnmpWalker walker = SnmpUtils.createWalker(config, "snmp:walk", agg);
    	
    	walker.start();
    	
    	walker.waitFor();
    	
    	if (walker.timedOut()) {
    		System.err.println("Timed Out");
    	} else if (walker.failed()) {
    		System.err.println(walker.getErrorMessage());
    	}
    	
    	return null;
    	
    }
}
