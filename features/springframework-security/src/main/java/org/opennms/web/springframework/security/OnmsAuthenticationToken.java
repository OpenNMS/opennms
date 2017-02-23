/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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