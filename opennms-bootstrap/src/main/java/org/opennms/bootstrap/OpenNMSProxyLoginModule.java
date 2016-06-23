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
