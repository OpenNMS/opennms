package org.opennms.sms.reflector.smsservice;


public interface SmsService {
	void start() throws SmsServiceException;
	void stop() throws SmsServiceException;
	void addModem(String id, String port, int baudRate, String manufacturer, String model) throws SmsServiceException;
	void removeModem(String id) throws SmsServiceException;
}
