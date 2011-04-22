package org.opennms.netmgt.scriptd.helper;

import java.io.UnsupportedEncodingException;

import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.xml.event.Event;

public class EventForwarderDefaultImpl extends AbstractEventForwarder implements
		EventForwarder {

	public void flushEvent(Event event) {
		super.filter(event);
	}

	public void flushSyncEvent(Event event) {
		super.filter(event);
	}
		

	public static void main(String[] args) throws UnsupportedEncodingException {
		UeiEventMatch match = new UeiEventMatch("~^pippo.*$");
		EventBuilder builder = new EventBuilder("antonio.opennms.org/provaEventMatch", "eventMatch");
		System.out.println(match.match(builder.getEvent()));
		EventPolicyRule policy = new EventPolicyRuleDefaultImpl();
		policy.addDropRule(match);
		SnmpTrapHelper snmptraphelper = new SnmpTrapHelper();
		System.out.println("localEngineID: " +snmptraphelper.getLocalEngineID());		
		//EventForwarder forward1 = new SnmpV1TrapEventForwarder("192.168.0.25", "192.168.0.25", 162, "public", snmptraphelper);
		//forward1.setEventPolicyRule(policy);
		//forward1.flushEvent(builder.getEvent());
		
		//EventForwarder forward2 = new SnmpV2TrapEventForwarder("192.168.0.25", 162, "public", snmptraphelper);
		//forward2.setEventPolicyRule(policy);
		//forward2.flushEvent(builder.getEvent());
		EventForwarder forward3 = new SnmpV3TrapEventForwarder("192.168.0.25", 162, SnmpAgentConfig.AUTH_PRIV, "traptest", "mypassword", "SHA", "mypassword2", "AES",snmptraphelper);
		forward3.setEventPolicyRule(policy);
		forward3.flushEvent(builder.getEvent());
		snmptraphelper.stop();
	} 
	
}
