package org.opennms.sms.reflector.smsservice;

public class MobileMsgSequenceBuilder {
	private MobileMsgSequence m_sequence = new MobileMsgSequence();
	private MobileMsgTransaction m_currentTransaction;
	
	public MobileMsgSequenceBuilder addTransaction(String label) {
		m_currentTransaction = new MobileMsgTransaction(label);
		m_sequence.addTransaction(m_currentTransaction);
		return this;
	}

	public MobileMsgSequenceBuilder sendSms(String recipient, String text) {
		m_currentTransaction.setSmsRequest(recipient, text);
		return this;
	}


	public static MobileMsgResponse sms(String match) {
		// This doesn't seem right; I can't make a MobileMsgResponse yet because I have no response to base it on
		// And yet, I hate to make Yet Another Intermediate Object just to represent it  :P
		return null;
	}

	public MobileMsgSequenceBuilder expect(MobileMsgResponse sms) {
		return this;
	}

	public MobileMsgSequence getSequence() {
		return m_sequence;
	}


}
