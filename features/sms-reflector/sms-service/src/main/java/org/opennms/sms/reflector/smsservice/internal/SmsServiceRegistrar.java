package org.opennms.sms.reflector.smsservice.internal;

import org.opennms.sms.reflector.smsservice.SmsService;

public interface SmsServiceRegistrar {
	
	public void registerSmsService(SmsService service);

    public void unregisterSmsService(SmsService smsServiceImpl);
}
