package org.opennms.sms.monitor.session;

import java.util.Map;

public class BaseSessionVariableGenerator implements SessionVariableGenerator {
	private Map<String, String> m_parameters;
	
	public BaseSessionVariableGenerator() {
		
	}

	public BaseSessionVariableGenerator(Map<String, String> parameters) {
		setParameters(parameters);
	}

	public void checkIn(String variable) {
		throw new UnsupportedOperationException("You must implement checkIn() in your class!");
	}

	public String checkOut() {
		throw new UnsupportedOperationException("You must implement checkOut() in your class!");
	}

	protected Map<String, String> getParameters() {
		return m_parameters;
	}
	
	public void setParameters(Map<String, String> parameters) {
		m_parameters = parameters;
	}
}
