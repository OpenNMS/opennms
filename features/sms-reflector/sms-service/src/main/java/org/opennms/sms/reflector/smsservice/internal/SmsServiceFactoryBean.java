package org.opennms.sms.reflector.smsservice.internal;

import org.opennms.sms.reflector.smsservice.SmsService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>SmsServiceFactoryBean class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SmsServiceFactoryBean implements FactoryBean<SmsService>, InitializingBean {
	private SmsService[] m_serviceList;

	/**
	 * <p>setSmsServiceList</p>
	 *
	 * @param serviceList an array of {@link org.opennms.sms.reflector.smsservice.SmsService} objects.
	 */
	public void setSmsServiceList(SmsService[] serviceList) {
		m_serviceList = serviceList;
	}
	
	/**
	 * <p>getObject</p>
	 *
	 * @return a {@link org.opennms.sms.reflector.smsservice.SmsService} object.
	 * @throws java.lang.Exception if any.
	 */
	public SmsService getObject() throws Exception {
		return m_serviceList[0];
	}

	/**
	 * <p>getObjectType</p>
	 *
	 * @return a {@link java.lang.Class} object.
	 */
	public Class<? extends SmsService> getObjectType() {
		return SmsService.class;
	}

	/**
	 * <p>isSingleton</p>
	 *
	 * @return a boolean.
	 */
	public boolean isSingleton() {
		return true;
	}

	/**
	 * <p>afterPropertiesSet</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(m_serviceList, "there must be at least one service in the SMS service list");
	}
}
