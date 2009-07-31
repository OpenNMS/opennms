package org.opennms.sms.reflector.smsservice;

import java.io.IOException;

import org.smslib.GatewayException;
import org.smslib.SMSLibException;
import org.smslib.TimeoutException;

public interface SmsService {
	void start();
	void stop();
	void addModem(String id, String port, int baudRate, String manufacturer, String model);
//	void addGateway(AGateway gateway);
	void removeModem(String id);
//	void removeGateway(AGateway gateway);
}
