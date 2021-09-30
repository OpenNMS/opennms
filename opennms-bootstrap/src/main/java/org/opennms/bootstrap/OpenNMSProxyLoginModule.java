/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
