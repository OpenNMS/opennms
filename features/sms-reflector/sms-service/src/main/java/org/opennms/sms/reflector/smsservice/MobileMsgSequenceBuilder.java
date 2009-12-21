package org.opennms.sms.reflector.smsservice;

import org.opennms.sms.reflector.smsservice.MobileMsgTransaction.SmsTransaction;
import org.opennms.sms.reflector.smsservice.MobileMsgTransaction.UssdTransaction;

public class MobileMsgSequenceBuilder {
	public static final int DEFAULT_RETRIES = 0;
	public static final long DEFAULT_TIMEOUT = 10000L;

	public static abstract class MobileMsgTransactionBuilder {
		private MobileMsgResponseMatcher m_matcher;
		private String m_label;
		private String m_gatewayId;
		private long m_timeout;
		private int m_retries;

		public MobileMsgTransactionBuilder(String label, String gatewayId, long timeout, int retries) {
			m_label = label;
			m_gatewayId = gatewayId;
			m_timeout = timeout;
			m_retries = retries;
		}

		public abstract MobileMsgTransaction getTransaction();

		public MobileMsgTransactionBuilder expects(MobileMsgResponseMatcher matcher) {
			m_matcher = matcher;
			return this;
		}
		
		public String getLabel() {
			return m_label;			
		}
		
		public String getGatewayId() {
			return m_gatewayId;
		}

		public long getTimeout() {
			return m_timeout;
		}

		public MobileMsgTransactionBuilder setTimeout(long timeout) {
			m_timeout = timeout;
			return this;
		}

		public int getRetries() {
			return m_retries;
		}
		
		public MobileMsgTransactionBuilder setRetries(int retries) {
			m_retries = retries;
			return this;
		}
		
		public MobileMsgTransactionBuilder setGatewayId(String gatewayId) {
			m_gatewayId = gatewayId;
			return this;
		}
		
		public MobileMsgResponseMatcher getMatcher() {
			return m_matcher;
		}
	}

	public static class SmsTransactionBuilder extends MobileMsgTransactionBuilder {
		private MobileMsgSequence m_sequence;
		private String m_recipient;
		private String m_text;

		public SmsTransactionBuilder(MobileMsgSequence sequence, String label, String gatewayId, long timeout, int retries, String recipient, String text) {
			super(label, gatewayId, timeout, retries);
			m_sequence = sequence;
			m_recipient = recipient;
			m_text = text;
		}

		@Override
		public MobileMsgTransaction getTransaction() {
			return new SmsTransaction(
				m_sequence,
				getLabel(),
				getGatewayId(),
				getTimeout(),
				getRetries(),
				getRecipient(),
				getText(),
				getMatcher()
			);
		}

		private String getText() {
			return m_text;
		}

		private String getRecipient() {
			return m_recipient;
		}
	}

	public static class UssdTransactionBuilder extends MobileMsgTransactionBuilder {
		private MobileMsgSequence m_sequence;
		private String m_text;
		
		public UssdTransactionBuilder(MobileMsgSequence sequence, String label, String gatewayId, long timeout, int retries, String text) {
			super(label, gatewayId, timeout, retries);
			m_sequence = sequence;
			m_text = text;
		}

		@Override
		public MobileMsgTransaction getTransaction() {
			return new UssdTransaction(
				m_sequence,
				getLabel(),
				getGatewayId(),
				getTimeout(),
				getRetries(),
				m_text,
				getMatcher()
			);
		}
		
	}
	
	private MobileMsgSequence m_sequence = new MobileMsgSequence();
	private String m_gatewayId = "*";
	private MobileMsgTransactionBuilder m_currentBuilder;
	private long m_timeout = DEFAULT_TIMEOUT;
	private int m_retries = DEFAULT_RETRIES;
	
	public MobileMsgTransactionBuilder sendSms(String label, String gatewayId, String recipient, String text) {
		addCurrentBuilderToSequence();
		m_currentBuilder = new SmsTransactionBuilder(m_sequence, label, gatewayId == null? m_gatewayId : gatewayId, m_timeout, m_retries, recipient, text);
		return m_currentBuilder;
	}

	public MobileMsgTransactionBuilder sendUssd(String label, String gatewayId, String text) {
		addCurrentBuilderToSequence();
		m_currentBuilder = new UssdTransactionBuilder(m_sequence, label, gatewayId == null? m_gatewayId : gatewayId, m_timeout, m_retries, text);
		return m_currentBuilder;
	}

	public MobileMsgSequenceBuilder setDefaultGatewayId(String gatewayId) {
		m_gatewayId = gatewayId;
		return this;
	}

	public long getDefaultTimeout() {
		return m_timeout;
	}

	public MobileMsgSequenceBuilder setDefaultTimeout(long timeout) {
		m_timeout = timeout;
		return this;
	}
	
	public int getDefaultRetries() {
		return m_retries;
	}

	public MobileMsgSequenceBuilder setDefaultRetries(int retries) {
		m_retries = retries;
		return this;
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
