package org.opennms.container.jaas;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.modules.AbstractKarafLoginModule;
import org.opennms.netmgt.config.api.UserConfig;
import org.opennms.netmgt.config.users.User;
import org.opennms.web.springframework.security.SpringSecurityUser;
import org.opennms.web.springframework.security.SpringSecurityUserDao;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

public class OpenNMSLoginModule extends AbstractKarafLoginModule {
    private static final transient Logger LOG = LoggerFactory.getLogger(OpenNMSLoginModule.class);
    private static transient volatile BundleContext m_context;

    private UserConfig m_userConfig;
    private SpringSecurityUserDao m_userDao;

    @Override
    public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
        LOG.info("OpenNMS Login Module initializing.");
        super.initialize(subject, callbackHandler, options);
    }

    @Override
    public boolean login() throws LoginException {
        final Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);
        try {
            callbackHandler.handle(callbacks);
        } catch (final IOException ioe) {
            throw new LoginException(ioe.getMessage());
        } catch (final UnsupportedCallbackException uce) {
            throw new LoginException(uce.getMessage() + " not available to obtain information from user.");
        }

        user = ((NameCallback) callbacks[0]).getName();
        if (user == null) {
            throw new LoginException("Username can not be null.");
        }

        // password callback get value
        if (((PasswordCallback) callbacks[1]).getPassword() == null) {
            throw new LoginException("Password can not be null.");
        }
        final String password = new String(((PasswordCallback) callbacks[1]).getPassword());

        final User configUser;
        final SpringSecurityUser onmsUser;
        try {
            configUser = getUserConfig().getUser(user);
            onmsUser = getSpringSecurityUserDao().getByUsername(user);
        } catch (final Exception e) {
            final String message = "Failed to retrieve user " + user + " from OpenNMS UserConfig.";
            LOG.debug(message, e);
            throw new LoginException(message);
        }

        if (configUser == null) {
            throw new FailedLoginException("User  " + user + " does not exist.");
        }

        if (!getUserConfig().comparePasswords(user, password)) {
            throw new FailedLoginException("Login failed: passwords did not match.");
        };

        boolean allowed = false;
        principals = new HashSet<Principal>();
        for (final GrantedAuthority auth : onmsUser.getAuthorities()) {
            // not sure if karaf is OK with ROLE_* or wants lower-case *
            final String role = auth.getAuthority().toLowerCase().replaceFirst("^role_", "");
            if ("admin".equals(role)) {
                allowed = true;
            }
            principals.add(new RolePrincipal(role));
            principals.add(new RolePrincipal(auth.getAuthority()));
        }

        if (!allowed) {
            throw new LoginException("User " + user + " is not an administrator!  OSGi console access is forbidden.");
        }

        if (debug) {
            LOG.debug("Successfully logged in {}.", user);
        }
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        LOG.debug("Aborting {} login.", user);
        clear();
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        LOG.debug("Logging out user {}.", user);
        subject.getPrincipals().removeAll(principals);
        principals.clear();
        return true;
    }

    public UserConfig getUserConfig() {
        if (m_userConfig == null) {
            m_userConfig = getFromRegistry(UserConfig.class);
        }
        return m_userConfig;
    }

    public SpringSecurityUserDao getSpringSecurityUserDao() {
        if (m_userDao == null) {
            m_userDao = getFromRegistry(SpringSecurityUserDao.class);
        }
        return m_userDao;
    }

    public <T> T getFromRegistry(final Class<T> clazz) {
        if (m_context == null) {
            LOG.warn("No bundle context.  Unable to get class {} from the registry.", clazz);
            return null;
        }
        final ServiceReference<T> ref = m_context.getServiceReference(clazz);
        return m_context.getService(ref);
    }

    public static synchronized void setContext(final BundleContext context) {
        m_context = context;
    }
    
    public static synchronized BundleContext getContext() {
        if (m_context == null) {
            m_context = FrameworkUtil.getBundle(OpenNMSLoginModule.class).getBundleContext();
        }
        return m_context;
    }
}
