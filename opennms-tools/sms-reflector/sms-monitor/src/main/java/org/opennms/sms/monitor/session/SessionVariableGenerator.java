package org.opennms.sms.monitor.session;

import java.util.Map;

public interface SessionVariableGenerator {
	public SessionVariableGenerator getInstance(Map<String, String> parameters);
	
	public String checkOut();
	public void checkIn(String variable);
}
