package org.opennms.sms.reflector.smsservice;

import java.util.List;
import java.util.Map;


public class MobileMsgSequence {

	private List<MobileMsgTransaction> m_transactions;

	public void addTransaction(MobileMsgTransaction t) {
		m_transactions.add(t);
	}

	public Map<String, Long> execute() {
		return null;
	}

}
