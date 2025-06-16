/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
