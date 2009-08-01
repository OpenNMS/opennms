package org.opennms.sms.reflector.smsservice.internal;

import org.opennms.sms.reflector.smsservice.SmsService;
import org.opennms.sms.reflector.smsservice.SmsServiceException;
import org.smslib.AGateway;
import org.smslib.Service;
import org.smslib.modem.SerialModemGateway;

public class SmsServiceImpl implements SmsService {
	
	private Service m_service;
	
	public void addModem(String id, String port, int baudRate, String manufacturer, String model) throws SmsServiceException {
        try {
            m_service.addGateway(new SerialModemGateway(id, port, baudRate, manufacturer, model));
        } catch (Exception e) {
            throw new SmsServiceException(e);
        }
	}

	public void removeModem(String id) throws SmsServiceException {
        try {
            AGateway gateway = m_service.findGateway(id);
                m_service.removeGateway(gateway);
        } catch (Exception e) {
            throw new SmsServiceException(e);
        }
	}

	public void start() throws SmsServiceException {
		try {
            m_service.startService();
        } catch (Exception e) {
            throw new SmsServiceException(e);
        }

	}

	public void stop() throws SmsServiceException {
		try {
			m_service.stopService();
		} catch (Exception e) {
		    throw new SmsServiceException(e);
		} 

	}

}
