package edu.ncsu.pdgrenon;


public class ServiceCollector {
	
	private String m_serviceID;
	private int m_collectionCount = 0;
	private long m_totalTime = 0;
	private long m_lastBegin = 0;

	public ServiceCollector(String serviceID) {
		m_serviceID = serviceID;
	}
	
	public String getServiceID() {
		return m_serviceID;
	}


	public void addMessage(LogMessage msg) {
		if (msg.isCollectorBeginMessage()) {
			m_lastBegin = msg.getDate().getTime();
		}
		if (msg.isCollectorEndMessage()) {
			long end = msg.getDate().getTime();
			if (m_lastBegin > 0) {
				m_totalTime += end - m_lastBegin;
				m_collectionCount++;
			}
			m_lastBegin = 0;
		}
	}

	int getCollectionCount() {
		return m_collectionCount;
	}

	long totalCollectionTime() {
		return m_totalTime;
	}

	long averageCollectionTime() {
		int collectionsPerService = getCollectionCount();
		if (collectionsPerService == 0) return 0;
		return totalCollectionTime()/collectionsPerService;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ServiceCollector) {
			ServiceCollector c = (ServiceCollector)obj;
			return getServiceID().equals(c.getServiceID());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getServiceID().hashCode();
	}


}
