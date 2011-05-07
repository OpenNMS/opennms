package org.opennms.netmgt.scriptd.helper;

import java.io.UnsupportedEncodingException;

import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;

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
		
		Event event = builder.getEvent();
		event.setDbid(100);
		event.setNodeid(0);
		
		Logmsg logmsg = new Logmsg();
		logmsg.setContent("Prova Event Match Logmsg");
		logmsg.setDest("logndisplay");
		event.setLogmsg(logmsg);
		
		event.setDescr("Prova Event Match Description");

		AlarmData alarmdata = new AlarmData();
		alarmdata.setAlarmType(1);
		alarmdata.setReductionKey("reductionkey");
		
		event.setAlarmData(alarmdata);
		
		
		System.out.println(match.match(event));
		
		EventPolicyRule policy = new EventPolicyRuleDefaultImpl();
		policy.addDropRule(match);
		SnmpTrapHelper snmptraphelper = new SnmpTrapHelper();
		System.out.println("localEngineID: " +snmptraphelper.getLocalEngineID());		
		//Event Trap
		//Version 1 event-trap
		EventForwarder forward1 = new SnmpV1TrapEventForwarder("192.168.0.25", "192.168.0.25", 162, "public", snmptraphelper);
		forward1.setEventPolicyRule(policy);
		forward1.flushEvent(event);
		forward1.flushSyncEvent(event);
		
		// Version 2 event-trap
		EventForwarder forward2 = new SnmpV2TrapEventForwarder("192.168.0.25", 162, "public", snmptraphelper);
		forward2.setEventPolicyRule(policy);
		forward2.flushEvent(event);
		forward2.flushSyncEvent(event);

		//Version 3 event-trap
		EventForwarder forward3 = new SnmpV3TrapEventForwarder("192.168.0.25", 162, SnmpAgentConfig.AUTH_PRIV, "traptest", "mypassword", "SHA", "mypassword2", "AES",snmptraphelper);
		forward3.setEventPolicyRule(policy);
		forward3.flushEvent(event);

		// version 2 event-inform
		EventForwarder forward4 = new SnmpV2InformEventForwarder("192.168.0.25", 162, "public", 3000, 3,snmptraphelper);
		forward4.setEventPolicyRule(policy);
		forward4.flushEvent(event);

		// version 3 event-inform
		EventForwarder forward5 = new SnmpV3InformEventForwarder("192.168.0.25", 162, 3000, 3, SnmpAgentConfig.AUTH_PRIV, "informtest", "mypassword", "SHA", "mypassword", "AES",snmptraphelper);
		forward5.setEventPolicyRule(policy);
		forward5.flushEvent(event);
		
		//Alarm Trap
		//Version 1 alarm-trap
		EventForwarder aforward1 = new SnmpV1TrapAlarmForwarder("192.168.0.25", "192.168.0.25", 162, "public", snmptraphelper);
		aforward1.setEventPolicyRule(policy);
		aforward1.flushEvent(event);
		aforward1.sendStartSync();
		aforward1.flushSyncEvent(event);
		aforward1.sendEndSync();

		// version 2 alarm-traps
		EventForwarder aforward2 = new SnmpV2TrapAlarmForwarder("192.168.0.25", 162, "public", snmptraphelper);
		aforward2.setEventPolicyRule(policy);
		aforward2.flushEvent(event);
		aforward2.sendStartSync();
		aforward2.flushSyncEvent(event);
		aforward2.sendEndSync();

		// version 3 alarm-traps
		EventForwarder aforward3 = new SnmpV3TrapAlarmForwarder("192.168.0.25", 162, SnmpAgentConfig.AUTH_PRIV, "traptest", "mypassword", "SHA", "mypassword2", "AES",snmptraphelper);
		aforward3.setEventPolicyRule(policy);
		aforward3.flushEvent(event);
		aforward3.sendStartSync();
		aforward3.flushSyncEvent(event);
		aforward3.sendEndSync();

		// version 2 alarm-informs
		EventForwarder aforward4 = new SnmpV2InformAlarmForwarder("192.168.0.25", 162, "public", 3000, 3,snmptraphelper);
		aforward4.setEventPolicyRule(policy);
		aforward4.flushEvent(event);
		aforward4.sendStartSync();
		aforward4.flushSyncEvent(event);
		aforward4.sendEndSync();

		// version 3 alarm-informs
		EventForwarder aforward5 = new SnmpV3InformAlarmForwarder("192.168.0.25", 162, 3000, 3, SnmpAgentConfig.AUTH_PRIV, "informtest", "mypassword", "SHA", "mypassword", "AES",snmptraphelper);
		aforward5.setEventPolicyRule(policy);
		aforward5.flushEvent(event);
		aforward5.sendStartSync();
		aforward5.flushSyncEvent(event);
		aforward5.sendEndSync();

		
		snmptraphelper.stop();

	}

	public void sendStartSync() {
	}

	public void sendEndSync() {
	} 
	
}
