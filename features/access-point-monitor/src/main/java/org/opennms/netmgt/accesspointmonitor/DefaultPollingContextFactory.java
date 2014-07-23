package org.opennms.netmgt.accesspointmonitor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class DefaultPollingContextFactory implements PollingContextFactory, BeanFactoryAware, InitializingBean {
	private BeanFactory m_beanFactory;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.state(m_beanFactory != null);
	}

	@Override
	public PollingContext getInstance() {
		// Fetch a new instance of the PollingContext prototype bean
		return m_beanFactory.getBean(PollingContext.class);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		m_beanFactory = beanFactory;
	}
}
