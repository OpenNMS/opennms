package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/* wrapper object to deal with old castor resources */
public class BasicSchedule {

	private String m_name;
	private String m_type;
	private List<Time> m_times = new ArrayList<Time>();

	public String getName() {
		return m_name;
	}
	
	public void setName(final String name) {
		m_name = name;
	}

	public String getType() {
		return m_type;
	}

	public void setType(final String type) {
		m_type = type;
	}

	public void setTimeCollection(final Collection<Time> times) {
		synchronized(m_times) {
			m_times.clear();
			m_times.addAll(times);
		}
	}

	public Collection<Time> getTimeCollection() {
		synchronized(m_times) {
			return m_times;
		}
	}
	
	public Enumeration<Time> enumerateTime() {
		synchronized(m_times) {
			return Collections.enumeration(m_times);
		}
	}

	public int getTimeCount() {
		synchronized(m_times) {
			return m_times.size();
		}
	}

	public Time getTime(final int index) {
		synchronized(m_times) {
			return m_times.get(index);
		}
	}
}
