package org.opennms.sms.monitor.session;

import java.util.Map;


public interface SessionVariableGenerator {
	public void setParameters(Map<String,String> parameters);
	public String checkOut();
	public void checkIn(String variable);
}
