#!/usr/bin/env groovy

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

