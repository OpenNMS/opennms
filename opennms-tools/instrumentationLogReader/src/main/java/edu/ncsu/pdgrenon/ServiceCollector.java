package edu.ncsu.pdgrenon;

import java.util.concurrent.TimeUnit;

import com.brozowski.instrumentation.Duration;


public class ServiceCollector {
	
	private String m_serviceID;
	private int m_collectionCount = 0;
    private int m_errorCount = 0;
    private int m_betweenCount = 0;
	private long m_totalTime = 0;
    private long m_errorTime = 0;
    private long m_totalBetweenTime = 0;
	private long m_lastBegin = 0;
	private long m_lastErrorBegin = 0;
	private long m_lastEnd = 0;

	public ServiceCollector(String serviceID) {
		m_serviceID = serviceID;
	}
	
	public String getServiceID() {
		return m_serviceID;
	}


	public void addMessage(LogMessage msg) {
		if (msg.isCollectorBeginMessage()) {
			m_lastBegin = msg.getDate().getTime();
			// measure the time between collections
			if (m_lastEnd > 0) {
			    m_totalBetweenTime += msg.getDate().getTime() - m_lastEnd;
			    m_betweenCount++;
			}
			m_lastEnd = 0;
		}
		if (msg.isErrorMessage()) {
		    m_lastErrorBegin = m_lastBegin;
		}
		if (msg.isCollectorEndMessage()) {
			long end = msg.getDate().getTime();
			m_lastEnd = msg.getDate().getTime();
			if (m_lastBegin > 0) {
				m_totalTime += end - m_lastBegin;
				m_collectionCount++;
			}
			m_lastBegin = 0;
			if (m_lastErrorBegin > 0) {
			    m_errorTime += end - m_lastErrorBegin;
			    m_errorCount++;
			}
			m_lastErrorBegin = 0;
		}
	}

	public int getCollectionCount() {
		return m_collectionCount;
	}
	
	int getErrorCollectionCount() {
	    return m_errorCount;
	}

	public long getTotalCollectionTime() {
		return m_totalTime;
	}

	public Duration getTotalCollectionDuration() {
		return new Duration(getTotalCollectionTime());
	}
	
	long getErrorCollectionTime() {
	    return m_errorTime;
	}
	
	long getSuccessfulCollectionTime() {
	    return m_totalTime - m_errorTime;
	}
	
	int getSuccessfulCollectionCount() {
	    return m_collectionCount - m_errorCount;
	}
	
	double successPercentage() {
        return getSuccessfulCollectionCount()*100.0/getCollectionCount();
	}
	
	double errorPercentage() {
        return getErrorCollectionCount()*100.0/getCollectionCount();
	}

	long getAverageCollectionTime() {
		int count = getCollectionCount();
		if (count == 0) return 0;
		return totalCollectionTime()/count;
	}
	
	public Duration getAverageCollectionDuration() {
		return new Duration(getAverageCollectionTime());
	}

	long averageErrorCollectionTime() {
	    int count = getErrorCollectionCount();
	    if (count == 0) return 0;
	    return getErrorCollectionTime()/count;
	}
	
	long averageSuccessfulCollectionTime() {
	    int count = getSuccessfulCollectionCount();
	    if (count == 0) return 0;
	    return getSuccessfulCollectionTime()/count;
	}
	
	long averageTimeBetweenCollections() {
	    if (m_betweenCount == 0) return 0;
	    return m_totalBetweenTime/m_betweenCount;
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
