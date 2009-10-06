package org.opennms.sms.monitor.session;

import java.util.Map;

public class TmobileBalance extends BaseSessionVariableGenerator {
	public TmobileBalance() {
	}
	
	public TmobileBalance(Map<String,String> parameters) {
		super(parameters);
	}

	@Override
	public void checkIn(String variable) {
	}

	@Override
	public String checkOut() {
		return "#225#";
	}
}
