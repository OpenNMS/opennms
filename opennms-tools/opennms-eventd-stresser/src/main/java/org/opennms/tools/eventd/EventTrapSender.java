package org.opennms.tools.eventd;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.snmp.SnmpTrapBuilder;

public class EventTrapSender implements Runnable {
	
	private static AtomicInteger m_trapCounter = new AtomicInteger(0);
	
	private SnmpTrapBuilder m_builder;
	private String m_host;
	private Integer m_port;
	private String m_community;
	
	private Integer m_trapLimit;

	private long m_beginMillis;
	
	public EventTrapSender(SnmpTrapBuilder builder, String host, Integer port, String community, Integer limit) {
		m_builder = builder;
		m_host = host;
		m_port = port;
		m_community = community;
		m_trapLimit = limit;
		
		m_beginMillis = Calendar.getInstance().getTimeInMillis();
	}

	
	@Override
	public void run() {
		try {
			getBuilder().send(m_host, m_port, m_community);
			int totalTrapsSent = m_trapCounter.getAndIncrement();
			if (totalTrapsSent >= m_trapLimit) {
				throw new IllegalStateException("Reached traps to send limit.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SnmpTrapBuilder getBuilder() {
		return m_builder;
	}
	
	public String getHost() {
		return m_host;
	}
	
	public String getCommunity() {
		return m_community;
	}
	
	public Integer getTrapsSent() {
		return m_trapCounter.get();
	}
	
	public long getBeginMillis() {
		return m_beginMillis;
	}

}
