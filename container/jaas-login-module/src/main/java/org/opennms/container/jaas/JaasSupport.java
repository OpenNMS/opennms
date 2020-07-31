/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

public class JaasSupport {
	private static final transient Logger LOG = LoggerFactory.getLogger(OpenNMSLoginModule.class);

	private static AtomicReference<BundleContext> s_context;
	private static AtomicReference<UserConfig> s_userConfig;
	private static AtomicReference<GroupDao> s_groupDao;
	private static AtomicReference<SpringSecurityUserDao> s_userDao;

	public static synchronized void setContext(final BundleContext context) {
		s_context = new AtomicReference<>(context);
		s_userConfig = new AtomicReference<>();
		s_groupDao = new AtomicReference<>();
		s_userDao = new AtomicReference<>();
	}

	public static synchronized BundleContext getContext() {
		if (s_context.get() == null) {
			setContext(FrameworkUtil.getBundle(JaasSupport.class).getBundleContext());
		}
		return s_context.get();
	}

	public static UserConfig getUserConfig() {
		if (s_userConfig.get() == null) {
			s_userConfig.set(getFromRegistry(UserConfig.class));
		}
		return s_userConfig.get();
	}

	public static SpringSecurityUserDao getSpringSecurityUserDao() {
		if (s_userDao.get() == null) {
			s_userDao.set(getFromRegistry(SpringSecurityUserDao.class));
		}
		return s_userDao.get();
	}

	public static GroupDao getGroupDao() {
		if (s_groupDao.get() == null) {
			s_groupDao.set(getFromRegistry(GroupDao.class));
		}
		return s_groupDao.get();
	}

	private static <T> T getFromRegistry(final Class<T> clazz) {
		if (s_context.get() == null) {
			LOG.warn("No bundle context.  Unable to get class {} from the registry.", clazz);
			return null;
		}
		final BundleContext context = s_context.get();
		final ServiceReference<T> ref = context.getServiceReference(clazz);
		return context.getService(ref);
	}
}
