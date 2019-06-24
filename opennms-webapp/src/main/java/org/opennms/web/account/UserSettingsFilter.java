/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.web.account;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.opennms.core.time.CentralizedDateTimeFormat;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.User;
import org.opennms.web.tags.DateTimeTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSettingsFilter implements Filter {

    private Logger LOG = LoggerFactory.getLogger(UserSettingsFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession userSession = httpRequest.getSession(false);
        String userId = httpRequest.getRemoteUser();
        if(userSession != null
                && userId !=null
                && userSession.getAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID) == null){
            setUserTimeZoneIdIfPossible(httpRequest, userId);
        }
        filterChain.doFilter(request, response);
    }

    private void setUserTimeZoneIdIfPossible(HttpServletRequest httpRequest, String userId) {

        Optional<User> user = getUser(userId);
        ZoneId timeZoneId;
        if(user.isPresent()){
            timeZoneId = user.get().getTimeZoneId().orElse(ZoneId.systemDefault());
        } else {
            // set default zone so that the next http request won't run through the same (expensive) logic again
            timeZoneId = ZoneId.systemDefault();
        }
        httpRequest.getSession().setAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID, timeZoneId);
    }

    private Optional<User> getUser(String userId){
        if(userId == null || userId.length()==0){
            return Optional.empty();
        }
        try {
            UserFactory.init();
            UserManager userFactory = UserFactory.getInstance();
            return Optional.ofNullable(userFactory.getUser(userId));
        } catch (IOException e) {
            LOG.warn("Can not retrieve user - will use system default time zone settings", e);
            return Optional.empty();
        }
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // no initialization needed
    }
}
