package org.opennms.netmgt.model;


public class OnmsPackage {
	
	private String m_name;

	/** Describes the interfaces that 'match' this package */
	private IpInterfaceSelector m_ipSelector;
	
	/** Describes the interval to poll/collect/check threshholds etc in millis */
	private long m_interval;
	
	/** The respository used to store data collected for this package */
	private OnmsRepository m_repository;
	
	public long getInterval() {
		return m_interval;
	}

	public void setInterval(long interval) {
		m_interval = interval;
	}

	public IpInterfaceSelector getIpSelector() {
		return m_ipSelector;
	}

	public void setIpSelector(IpInterfaceSelector ipSelector) {
		m_ipSelector = ipSelector;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public OnmsRepository getRepository() {
		return m_repository;
	}

	public void setRepository(OnmsRepository repository) {
		m_repository = repository;
	}


}
