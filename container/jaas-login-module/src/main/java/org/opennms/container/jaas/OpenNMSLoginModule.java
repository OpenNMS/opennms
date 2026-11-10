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

import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.modules.AbstractKarafLoginModule;
import org.opennms.netmgt.config.api.UserConfig;
import org.opennms.web.springframework.security.LoginModuleUtils;
import org.opennms.web.springframework.security.OpenNMSLoginHandler;
import org.opennms.web.springframework.security.SpringSecurityUserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

public class OpenNMSLoginModule extends AbstractKarafLoginModule implements OpenNMSLoginHandler {
    private static final transient Logger LOG = LoggerFactory.getLogger(OpenNMSLoginModule.class);
    private Map<String, ?> m_sharedState;

    @Override
    public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
        LOG.info("OpenNMS Login Module initializing: subject={}, callbackHandler={}, sharedState={}, options={}", subject, callbackHandler, sharedState, options);
        m_sharedState = sharedState;
        super.initialize(subject, callbackHandler, options);
    }

    @Override
    public boolean login() throws LoginException {
        succeeded = LoginModuleUtils.doLogin(this, subject, m_sharedState, options);
        return succeeded;
    }

    @Override
    public boolean abort() throws LoginException {
        return super.abort();
    }

    @Override
    public boolean logout() throws LoginException {
        return super.logout();
    }

    public CallbackHandler callbackHandler() {
        return this.callbackHandler;
    }

    @Override
    public UserConfig userConfig() {
        return JaasSupport.getUserConfig();
    }

    @Override
    public SpringSecurityUserDao springSecurityUserDao() {
        return JaasSupport.getSpringSecurityUserDao();
    }

    @Override
    public String user() {
        return this.user;
    }

    @Override
    public void setUser(final String user) {
        this.user = user;
    }

    @Override
    public Set<Principal> createPrincipals(final GrantedAuthority authority) {
        final String role = authority.getAuthority().replaceFirst("^[Rr][Oo][Ll][Ee]_", "");
        final Set<Principal> principals = new HashSet<>();
        principals.add(new RolePrincipal(role));
        principals.add(new RolePrincipal(role.toLowerCase()));
        principals.add(new RolePrincipal(authority.getAuthority()));
        LOG.debug("created principals from authority {}: {}", authority, principals);
        return principals;
    }

    @Override
    public Set<Principal> principals() {
        return this.principals;
    }

    @Override
    public void setPrincipals(final Set<Principal> principals) {
        this.principals = principals;
    }

    @Override
    public boolean requiresAdminRole() {
        // this LoginHandler is used for Karaf access, allow admin login only
        return true;
    }
}
