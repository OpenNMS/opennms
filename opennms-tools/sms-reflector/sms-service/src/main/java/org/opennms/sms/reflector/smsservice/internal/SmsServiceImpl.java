package org.opennms.sms.reflector.smsservice.internal;

import java.io.IOException;

import org.opennms.sms.reflector.smsservice.SmsService;
import org.smslib.GatewayException;
import org.smslib.SMSLibException;
import org.smslib.Service;
import org.smslib.TimeoutException;

public class SmsServiceImpl implements SmsService {
	
	private Service m_service;
	
	public void addModem(String id, String port, int baudRate,
			String manufacturer, String model) {
		// TODO Auto-generated method stub

	}

	public void removeModem(String id) {
		// TODO Auto-generated method stub

	}

	public void start() {
		m_service.startService();

	}

	public void stop() {
		try {
			m_service.stopService();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

}
