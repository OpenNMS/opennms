/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
        @Override
	public SmsService getObject() throws Exception {
		return m_serviceList[0];
	}

	/**
	 * <p>getObjectType</p>
	 *
	 * @return a {@link java.lang.Class} object.
	 */
        @Override
	public Class<? extends SmsService> getObjectType() {
		return SmsService.class;
	}

	/**
	 * <p>isSingleton</p>
	 *
	 * @return a boolean.
	 */
        @Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * <p>afterPropertiesSet</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(m_serviceList, "there must be at least one service in the SMS service list");
	}
}
