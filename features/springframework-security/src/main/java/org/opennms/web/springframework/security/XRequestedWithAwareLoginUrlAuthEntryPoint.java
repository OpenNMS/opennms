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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.Assert;

// See NMS-9783
public class XRequestedWithAwareLoginUrlAuthEntryPoint extends LoginUrlAuthenticationEntryPoint implements InitializingBean {
    private String realmName;

    public XRequestedWithAwareLoginUrlAuthEntryPoint(final String loginUrl) {
        super(loginUrl);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(realmName, "realmName must be specified");
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException, ServletException {
        final String requestedWithValue = request.getHeader("X-Requested-With");
        if (requestedWithValue != null && requestedWithValue.equals("XMLHttpRequest")) {
            response.addHeader("WWW-Authenticate", "Basic realm=\"" + realmName + "\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        } else {
            super.commence(request, response, authException);
        }
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }
}
