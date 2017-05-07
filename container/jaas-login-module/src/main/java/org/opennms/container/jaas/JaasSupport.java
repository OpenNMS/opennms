/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.container.jaas;

import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.api.UserConfig;
import org.opennms.web.springframework.security.SpringSecurityUserDao;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaasSupport {
	private static final transient Logger LOG = LoggerFactory.getLogger(OpenNMSLoginModule.class);

	private static transient volatile BundleContext m_context;
	private static transient volatile UserConfig m_userConfig;
	private static transient volatile GroupDao m_groupDao;
	private static transient volatile SpringSecurityUserDao m_userDao;

	public static synchronized void setContext(final BundleContext context) {
		m_userConfig = null;
		m_groupDao = null;
		m_userDao = null;
		m_context = context;
	}

	public static synchronized BundleContext getContext() {
		if (m_context == null) {
			setContext(FrameworkUtil.getBundle(JaasSupport.class).getBundleContext());
		}
		return m_context;
	}

	public static UserConfig getUserConfig() {
		if (m_userConfig == null) {
			m_userConfig = getFromRegistry(UserConfig.class);
		}
		return m_userConfig;
	}

	public static SpringSecurityUserDao getSpringSecurityUserDao() {
		if (m_userDao == null) {
			m_userDao = getFromRegistry(SpringSecurityUserDao.class);
		}
		return m_userDao;
	}

	public static GroupDao getGroupDao() {
		if (m_groupDao == null) {
			m_groupDao = getFromRegistry(GroupDao.class);
		}
		return m_groupDao;
	}

	private static <T> T getFromRegistry(final Class<T> clazz) {
		if (m_context == null) {
			LOG.warn("No bundle context.  Unable to get class {} from the registry.", clazz);
			return null;
		}
		final ServiceReference<T> ref = m_context.getServiceReference(clazz);
		return m_context.getService(ref);
	}
}
