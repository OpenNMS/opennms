package org.opennms.sms.reflector.smsservice.internal;

import org.opennms.sms.reflector.smsservice.SmsService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class SmsServiceFactoryBean implements FactoryBean<SmsService>, InitializingBean {
	private SmsService[] m_serviceList;

	public void setSmsServiceList(SmsService[] serviceList) {
		m_serviceList = serviceList;
	}
	
	public SmsService getObject() throws Exception {
		return m_serviceList[0];
	}

	public Class<? extends SmsService> getObjectType() {
		return SmsService.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(m_serviceList, "there must be at least one service in the SMS service list");
	}
}