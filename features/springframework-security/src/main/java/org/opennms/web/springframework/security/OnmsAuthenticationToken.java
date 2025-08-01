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

import org.springframework.security.authentication.AbstractAuthenticationToken;

final class OnmsAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = -5896244818836123481L;
    private final SpringSecurityUser m_user;

    OnmsAuthenticationToken(final SpringSecurityUser user) {
        super(user.getAuthorities());
        m_user = user;
        setAuthenticated(true);
    }

    /**
     * This should always be a UserDetails. Java-Spec allows this,
     * spring can handle it and it's easier for us this way.
     */
    @Override
    public Object getPrincipal() {
        return m_user;
    }

    @Override
    public Object getCredentials() {
        return m_user.getPassword();
    }
}