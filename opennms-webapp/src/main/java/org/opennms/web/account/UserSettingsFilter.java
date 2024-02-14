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
