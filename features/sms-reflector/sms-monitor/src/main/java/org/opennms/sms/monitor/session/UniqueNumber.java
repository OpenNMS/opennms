package org.opennms.sms.monitor.session;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UniqueNumber extends BaseSessionVariableGenerator {
	private int min = 0;
	private int max = 1000;
	private static Set<Integer> m_used = new HashSet<Integer>();

	public UniqueNumber() {
		super();
	}
	
	public UniqueNumber(Map<String,String> parameters) {
		super(parameters);

		if (parameters.containsKey("min")) {
			min = Integer.valueOf(parameters.get("min"));
		}
		if (parameters.containsKey("max")) {
			max = Integer.valueOf(parameters.get("max"));
		}
}
	
	public void checkIn(String variable) {
		m_used.remove(Integer.valueOf(variable));
	}

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
