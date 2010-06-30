package org.opennms.sms.reflector.smsservice.internal;

import org.opennms.sms.reflector.smsservice.SmsService;

/**
 * <p>SmsServiceRegistrar interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface SmsServiceRegistrar {
	
	/**
	 * <p>registerSmsService</p>
	 *
	 * @param service a {@link org.opennms.sms.reflector.smsservice.SmsService} object.
	 */
	public void registerSmsService(SmsService service);

    /**
     * <p>unregisterSmsService</p>
     *
     * @param smsServiceImpl a {@link org.opennms.sms.reflector.smsservice.SmsService} object.
     */
    public void unregisterSmsService(SmsService smsServiceImpl);
}
