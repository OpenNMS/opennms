package org.opennms.web.svclayer;

public class SurveillanceViewError {
	
	private String m_shortDescr;
	private String m_longDescr;

	public String getShortDescr() {
		return m_shortDescr;
	}

	public void setShortDescr(String shortDescr) {
		m_shortDescr = shortDescr;
	}
	
	public String getLongDescr() {
		return m_longDescr;
	}

	public void setLongDescr(String longDescr) {
		m_longDescr = longDescr;
	}
	
}
