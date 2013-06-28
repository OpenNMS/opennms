package org.opennms.core.logging.log4j;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class MDCFilter extends Filter {

	private String m_key;
	private String m_value;
	private int m_onMatch = ACCEPT;
	private int m_onMisMatch = DENY;


	public String getKeyToMatch() {
		return m_key;
	}

	public String getValueToMatch() {
		return m_value;
	}

	public void setKeyToMatch(String key) {
		m_key = key;
	}

	public void setValueToMatch(String value) {
		m_value = value;
	}

	public void setOnMatch(String value) {
		m_onMatch = parsePolicy(value, ACCEPT);
	}

	public void setOnMisMatch(String value) {
		m_onMisMatch = parsePolicy(value, NEUTRAL);
	}

	private int parsePolicy(String value, int deflt) {
		if ("ACCEPT".equalsIgnoreCase(value)) {
			return ACCEPT;
		} else if ("DENY".equals(value)) {
			return DENY;
		} else if("NEUTRAL".equalsIgnoreCase(value)) {
			return NEUTRAL;
		} else {
			return deflt;
		}
	}

	@Override
	public int decide(LoggingEvent event) {
		if (m_key != null && m_value != null && m_value.equals(event.getMDC(m_key))) {
			return m_onMatch;
		}
		else {
			return m_onMisMatch;
		}
	}
}
