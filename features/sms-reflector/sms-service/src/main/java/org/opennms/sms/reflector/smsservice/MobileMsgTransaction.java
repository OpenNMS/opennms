/**
 * 
 */
package org.opennms.sms.reflector.smsservice;

public class MobileMsgTransaction {
	private String m_label;
	private RequestFactory m_requestFactory;
	
	private interface RequestFactory {
		MobileMsgRequest createRequest();
	}
	
	private class SmsRequestFactory implements RequestFactory {
		
		String m_recipient;
		String m_text;
		
		SmsRequestFactory(String recipient, String text) {
			m_recipient = recipient;
			m_text = text;
		}

		public MobileMsgRequest createRequest() {
			return null;
		}
		
	}
	public MobileMsgTransaction(String label) {
		m_label = label;
	}

	public void setSmsRequest(String recipient, String text) {
		m_requestFactory = new SmsRequestFactory(recipient, text);
	}

}