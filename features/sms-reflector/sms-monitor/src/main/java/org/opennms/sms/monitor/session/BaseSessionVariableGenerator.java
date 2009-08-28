package org.opennms.sms.monitor.session;

import java.util.Map;

public class BaseSessionVariableGenerator implements SessionVariableGenerator {
	private final Map<String, String> m_parameters;
	private BaseSessionVariableGenerator m_instance;
	
	public BaseSessionVariableGenerator() {
		throw new UnsupportedOperationException("SessionVariableGenerators must be singletons!");
	}

	public BaseSessionVariableGenerator(Map<String, String> parameters) {
		m_parameters = parameters;
	}
	public BaseSessionVariableGenerator getInstance(Map<String,String> parameters) {
		if (m_instance == null) {
			m_instance = new BaseSessionVariableGenerator(parameters);
		}
		return m_instance;
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
}
