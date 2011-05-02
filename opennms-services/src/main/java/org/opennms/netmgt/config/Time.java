package org.opennms.netmgt.config;

/* wrapper object to deal with old castor resources */
public class Time {

	private String m_id;
	private String m_day;
	private String m_begins;
	private String m_ends;

	public Time() {
	}
	
	public Time(final String id, final String day, final String begins, final String ends) {
		m_id = id;
		m_day = day;
		m_begins = begins;
		m_ends = ends;
	}

	public String getId() {
		return m_id;
	}
	
	public void setId(final String id) {
		m_id = id;
	}
	
	public String getDay() {
		return m_day;
	}

	public void setDay(final String day) {
		m_day = day;
	}

	public String getBegins() {
		return m_begins;
	}
	
	public void setBegins(final String begins) {
		m_begins = begins;
	}
	
	public String getEnds() {
		return m_ends;
	}
	
	public void setEnds(final String ends) {
		m_ends = ends;
	}
}
