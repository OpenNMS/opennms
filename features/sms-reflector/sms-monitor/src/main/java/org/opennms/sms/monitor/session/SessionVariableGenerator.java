package org.opennms.sms.monitor.session;

import java.util.Map;


/**
 * <p>SessionVariableGenerator interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface SessionVariableGenerator {
	/**
	 * <p>setParameters</p>
	 *
	 * @param parameters a {@link java.util.Map} object.
	 */
	public void setParameters(Map<String,String> parameters);
	/**
	 * <p>checkOut</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String checkOut();
	/**
	 * <p>checkIn</p>
	 *
	 * @param variable a {@link java.lang.String} object.
	 */
	public void checkIn(String variable);
}
