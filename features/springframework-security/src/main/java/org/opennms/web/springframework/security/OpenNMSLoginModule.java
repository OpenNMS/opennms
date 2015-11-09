package org.opennms.web.springframework.security;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.opennms.netmgt.config.api.UserConfig;
import org.opennms.netmgt.config.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

public class OpenNMSLoginModule implements LoginModule {
    private static final transient Logger LOG = LoggerFactory.getLogger(OpenNMSLoginModule.class);

    private static transient volatile UserConfig m_userConfig;
    private static transient volatile SpringSecurityUserDao m_springSecurityUserDao;

    protected Subject m_subject;
    protected CallbackHandler m_callbackHandler;
    protected Map<String, ? super Object> m_sharedState;
    protected Map<String, ? super Object> m_options;

    protected String m_user;
    protected Set<Principal> m_principals = new HashSet<Principal>();
    private boolean m_debug;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
        LOG.info("OpenNMS Login Module initializing.");
        for (final Entry<String, ?> entry : options.entrySet()) {
            LOG.debug("{} = {}", entry.getKey(), entry.getValue());
        }
        m_subject = subject;
        m_callbackHandler = callbackHandler;
        m_sharedState = (Map<String, ? super Object>) sharedState;
        m_options = (Map<String, ? super Object>) options;
        m_debug = Boolean.parseBoolean((String) options.get("debug"));
    }

    @Override
    public boolean login() throws LoginException {
        LOG.debug("OpenNMSLoginModule: login()");
        final Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);
        try {
            m_callbackHandler.handle(callbacks);
        } catch (final IOException ioe) {
            throw new LoginException(ioe.getMessage());
        } catch (final UnsupportedCallbackException uce) {
            throw new LoginException(uce.getMessage() + " not available to obtain information from user.");
        }

        m_user = ((NameCallback) callbacks[0]).getName();
        if (m_user == null) {
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
            configUser = getUserConfig().getUser(m_user);
            onmsUser = getSpringSecurityUserDao().getByUsername(m_user);
        } catch (final Exception e) {
            final String message = "Failed to retrieve user " + m_user + " from OpenNMS UserConfig.";
            LOG.debug(message, e);
            throw new LoginException(message);
        }

        if (configUser == null) {
            throw new FailedLoginException("User  " + m_user + " does not exist");
        }

        if (!getUserConfig().comparePasswords(m_user, password)) {
            throw new FailedLoginException("Login failed: passwords did not match.");
        };

        boolean allowed = false;
        m_principals = new HashSet<Principal>();
        for (final GrantedAuthority auth : onmsUser.getAuthorities()) {
            final AuthorityPrincipal principal = new AuthorityPrincipal(auth);
            if ("admin".equals(principal.getName())) {
                allowed = true;
            }
            m_principals.add(principal);
        }

        if (!allowed) {
            throw new LoginException("User " + m_user + " is not an administrator!  OSGi console access is forbidden.");
        }
        if (m_debug) {
            LOG.debug("Successfully logged in {}.", m_user);
        }
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        LOG.debug("Aborting {} login.", m_user);
        m_user = null;
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        LOG.debug("Logging out user {}.", m_user);
        m_subject.getPrincipals().removeAll(m_principals);
        m_principals.clear();
        return true;
    }

    public static synchronized UserConfig getUserConfig() {
        return m_userConfig;
    }

    public static synchronized void setUserConfig(final UserConfig userConfig) {
        m_userConfig = userConfig;
    }

    public static synchronized SpringSecurityUserDao getSpringSecurityUserDao() {
        return m_springSecurityUserDao;
    }

    public static synchronized void setSpringSecurityUserDao(final SpringSecurityUserDao userDao) {
        m_springSecurityUserDao = userDao;
    }

    @Override
    public boolean commit() throws LoginException {
        if (m_principals.isEmpty()) {
            return false;
        }
        m_subject.getPrincipals().addAll(m_principals);
        return true;
    }
}
