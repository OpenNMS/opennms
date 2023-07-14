/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

import java.util.concurrent.atomic.AtomicReference;

import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.api.UserConfig;
import org.opennms.web.springframework.security.SpringSecurityUserDao;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JaasSupport {
	private static final Logger LOG = LoggerFactory.getLogger(JaasSupport.class);

	private static AtomicReference<BundleContext> m_context = new AtomicReference<>();
	private static AtomicReference<UserConfig> m_userConfig = new AtomicReference<>();
	private static AtomicReference<GroupDao> m_groupDao = new AtomicReference<>();
	private static AtomicReference<SpringSecurityUserDao> m_userDao = new AtomicReference<>();

	private JaasSupport() {}

	public static synchronized void setContext(final BundleContext context) {
	    m_context.set(context);
		m_userConfig.set(null);
		m_groupDao.set(null);
		m_userDao.set(null);
	}

	public static synchronized BundleContext getContext() {
	    final var context = m_context.get();
	    if (context != null) {
	        return context;
	    }
		setContext(FrameworkUtil.getBundle(JaasSupport.class).getBundleContext());
		return m_context.get();
	}

	public static UserConfig getUserConfig() {
	    final var userConfig = m_userConfig.get();
	    if (userConfig != null) {
	        return userConfig;
	    }
		m_userConfig.set(getFromRegistry(UserConfig.class));
		return m_userConfig.get();
	}

	public static SpringSecurityUserDao getSpringSecurityUserDao() {
	    final var userDao = m_userDao.get();
	    if (userDao != null) {
	        return userDao;
	    }
		m_userDao.set(getFromRegistry(SpringSecurityUserDao.class));
		return m_userDao.get();
	}

	public static GroupDao getGroupDao() {
	    final var groupDao = m_groupDao.get();
	    if (groupDao != null) {
	        return groupDao;
	    }
		m_groupDao.set(getFromRegistry(GroupDao.class));
		return m_groupDao.get();
	}

	private static <T> T getFromRegistry(final Class<T> clazz) {
	    final var context = m_context.get();
		if (context == null) {
			LOG.warn("No bundle context.  Unable to get class {} from the registry.", clazz);
			return null;
		}
		final ServiceReference<T> ref = context.getServiceReference(clazz);
		return context.getService(ref);
	}
}
