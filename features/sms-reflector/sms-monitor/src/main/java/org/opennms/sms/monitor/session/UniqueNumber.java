package org.opennms.sms.monitor.session;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>UniqueNumber class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class UniqueNumber extends BaseSessionVariableGenerator {
	private int min = 0;
	private int max = 1000;
	private static Set<Integer> m_used = new HashSet<Integer>();

	/**
	 * <p>Constructor for UniqueNumber.</p>
	 */
	public UniqueNumber() {
		super();
	}
	
	/**
	 * <p>Constructor for UniqueNumber.</p>
	 *
	 * @param parameters a {@link java.util.Map} object.
	 */
	public UniqueNumber(Map<String,String> parameters) {
		super(parameters);

		if (parameters.containsKey("min")) {
			min = Integer.valueOf(parameters.get("min"));
		}
		if (parameters.containsKey("max")) {
			max = Integer.valueOf(parameters.get("max"));
		}
}
	
	/** {@inheritDoc} */
	public void checkIn(String variable) {
		m_used.remove(Integer.valueOf(variable));
	}

	/**
	 * <p>checkOut</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String checkOut() {
		for (int i = min; i < max; i++) {
			if (!m_used.contains(i)) {
				m_used.add(i);
				return String.valueOf(i);
			}
		}
		return null;
	}
}
