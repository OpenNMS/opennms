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
package org.opennms.bootstrap;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class OpenNMSProxyLoginModule implements LoginModule {
    private static volatile ClassLoader m_classLoader;
    private LoginModule m_delegate;

    @Override
    public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
        if (m_classLoader == null) {
            System.err.println("WARNING: OpenNMSProxyLoginModule is being initialized, but no classloader is set from bootstrap!");
        }
        final ClassLoader cl = m_classLoader == null? Thread.currentThread().getContextClassLoader() : m_classLoader;
        try {
            final Class<?> clazz = cl.loadClass("org.opennms.web.springframework.security.OpenNMSLoginModule");
            m_delegate = (LoginModule) clazz.newInstance();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (m_delegate == null) {
            throw new RuntimeException("OpenNMSLoginModule could not be loaded!");
        }
        m_delegate.initialize(subject, callbackHandler, sharedState, options);
    }

    @Override
    public boolean login() throws LoginException {
        return m_delegate == null? false : m_delegate.login();
    }

    @Override
    public boolean commit() throws LoginException {
        return m_delegate == null? false : m_delegate.commit();
    }

    @Override
    public boolean abort() throws LoginException {
        return m_delegate == null? false : m_delegate.abort();
    }

    @Override
    public boolean logout() throws LoginException {
        return m_delegate == null? false : m_delegate.logout();
    }

    public static void setClassloader(final ClassLoader cl) {
        m_classLoader = cl;
    }

}
