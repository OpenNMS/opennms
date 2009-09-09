package org.opennms.sms.reflector.smsservice;

import org.opennms.sms.reflector.smsservice.MobileMsgTransaction.SmsTransaction;

public class MobileMsgSequenceBuilder {
	public abstract class MobileMsgTransactionBuilder {
		private MobileMsgResponseMatcher m_matcher;
		private String m_label;

		public MobileMsgTransactionBuilder(String label) {
			m_label = label;
		}

		public abstract MobileMsgTransaction getTransaction();

		public MobileMsgTransactionBuilder expects(MobileMsgResponseMatcher matcher) {
			m_matcher = matcher;
			return this;
		}
		
		public String getLabel() {
			return m_label;			
		}
		
		public MobileMsgResponseMatcher getMatcher() {
			return m_matcher;
		}
	}

	public class SmsTransactionBuilder extends MobileMsgTransactionBuilder {
		private String m_recipient;
		private String m_text;

		public SmsTransactionBuilder(String label, String recipient, String text) {
			super(label);
			m_recipient = recipient;
			m_text = text;
		}

		@Override
		public MobileMsgTransaction getTransaction() {
			return new SmsTransaction(getLabel(), m_recipient, m_text, getMatcher());
		}
	}

	public class UssdTransactionBuilder extends MobileMsgTransactionBuilder {
		private String m_gatewayId;
		private String m_text;
		
		public UssdTransactionBuilder(String label, String gatewayId, String text) {
			super(label);
			m_gatewayId = gatewayId;
			m_text = text;
		}

		public UssdTransactionBuilder setGatewayId(String gatewayId) {
			m_gatewayId = gatewayId;
			return this;
		}
		
		@Override
		public MobileMsgTransaction getTransaction() {
			return null;
		}
		
	}
	
	private MobileMsgSequence m_sequence = new MobileMsgSequence();
	private MobileMsgTransaction m_currentTransaction;
	private String m_gatewayId;
	private MobileMsgTransactionBuilder m_currentBuilder;
	
	public MobileMsgTransactionBuilder sendSms(String label, String recipient, String text) {
		addCurrentBuilderToSequence();
		m_currentBuilder = new SmsTransactionBuilder(label, recipient, text);
		return m_currentBuilder;
	}

	public MobileMsgTransactionBuilder sendUssd(String label, String text) {
		addCurrentBuilderToSequence();
		m_currentBuilder = new UssdTransactionBuilder(label, m_gatewayId, text);
		return m_currentBuilder;
	}


	public MobileMsgSequence getSequence() {
		addCurrentBuilderToSequence();
		return m_sequence;
	}

	private void addCurrentBuilderToSequence() {
		if (m_currentBuilder == null) {
			return;
		}

		m_sequence.addTransaction(m_currentBuilder.getTransaction());
		m_currentBuilder = null;
	}

}
