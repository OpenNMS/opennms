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
package org.opennms.web.springframework.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

public class OpenNMSUserDetailsService implements UserDetailsService, InitializingBean {
	private SpringSecurityUserDao m_userDao;
	private boolean m_trimRealm = false;
	
    public OpenNMSUserDetailsService() {
    }

    public OpenNMSUserDetailsService(final SpringSecurityUserDao userDao) {
        m_userDao = userDao;
    }

	@Override
	public void afterPropertiesSet() throws Exception {
	    Assert.notNull(m_userDao);
	}

	/** {@inheritDoc} */
        @Override
	public UserDetails loadUserByUsername(final String rawUsername) throws UsernameNotFoundException, DataAccessException {
            final String username;
            if (m_trimRealm && rawUsername.contains("@")) {
                username = rawUsername.substring(0, rawUsername.indexOf("@"));
            } else {
                username = rawUsername;
            }
	    final UserDetails userDetails = m_userDao.getByUsername(username);
		
		if (userDetails == null) {
			throw new UsernameNotFoundException("Unable to locate " + username + " in the userDao");
		}
		
		return userDetails;
	}

	public void setUserDao(final SpringSecurityUserDao userDao) {
		m_userDao = userDao;
		
	}

	public SpringSecurityUserDao getUserDao() {
		return m_userDao;
	}

	/**
	 * 
	 * @param trimRealm Defaults to false. If set to true, trim the realm
	 * portion (e.g. @EXAMPLE.ORG) from the authenticated user principal
	 * name (e.g. user@EXAMPLE.ORG). Useful when authenticating against a
	 * Kerberos realm or possibly other realm- / domain-aware technologies
	 * such as OAUTH.
	 */
	public void setTrimRealm(boolean trimRealm) {
	    m_trimRealm = trimRealm;
	}

	public boolean getTrimRealm() {
	    return m_trimRealm;
	}
}
