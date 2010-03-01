package org.opennms.poller.remote;

public class DefaultSimpleBackEnd implements SimpleBackEnd {
	private int m_count = 0;

	public int getCount() {
		return m_count++;
	}

}
