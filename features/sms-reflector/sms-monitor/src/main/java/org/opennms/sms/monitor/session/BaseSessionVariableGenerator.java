package org.opennms.sms.monitor.session;

import java.util.Map;

/**
 * <p>BaseSessionVariableGenerator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class BaseSessionVariableGenerator implements SessionVariableGenerator {
	private Map<String, String> m_parameters;
	
	/**
	 * <p>Constructor for BaseSessionVariableGenerator.</p>
	 */
	public BaseSessionVariableGenerator() {
		
	}

	/**
	 * <p>Constructor for BaseSessionVariableGenerator.</p>
	 *
	 * @param parameters a {@link java.util.Map} object.
	 */
	public BaseSessionVariableGenerator(Map<String, String> parameters) {
		setParameters(parameters);
	}

	/** {@inheritDoc} */
	public void checkIn(String variable) {
		throw new UnsupportedOperationException("You must implement checkIn() in your class!");
	}

	/**
	 * <p>checkOut</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String checkOut() {
		throw new UnsupportedOperationException("You must implement checkOut() in your class!");
	}

	/**
	 * <p>getParameters</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	protected Map<String, String> getParameters() {
		return m_parameters;
	}
	
	/** {@inheritDoc} */
	public void setParameters(Map<String, String> parameters) {
		m_parameters = parameters;
	}
}
