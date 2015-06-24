#!/usr/bin/env groovy

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

import org.opennms.netmgt.snmp.*;
import org.opennms.netmgt.config.*;

class MyTracker extends ColumnTracker {
	Closure processor;
	public MyTracker(SnmpObjId base, Closure c) {
		super(base)
		processor = c;
	}
	
    protected void storeResult(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
    	processor.call(base, inst, val);
    }
    
}

//System.setProperty("org.opennms.snmp.strategyClass","org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy");

SnmpPeerFactory.init()

def config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(args[0]));
def oid = ( args.length < 2 ? ".1.3.6.1.2.1.1" : args[1] );
SnmpObjId system = SnmpObjId.get(oid)

ColumnTracker tracker = new MyTracker(system) { base, inst, val -> println "[$base].[$inst] = $val" }

def walker = SnmpUtils.createWalker(config, "system", tracker)

walker.start();
walker.waitFor();

