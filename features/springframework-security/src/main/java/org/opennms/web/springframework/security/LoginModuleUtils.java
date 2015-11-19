package org.opennms.web.springframework.security;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.opennms.netmgt.config.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

public abstract class LoginModuleUtils {
    public static volatile Logger LOG = LoggerFactory.getLogger(LoginModuleUtils.class);

    protected LoginModuleUtils() {}

    public static boolean doLogin(final LoginHandler handler) throws LoginException {
        LOG.debug("OpenNMSLoginModule: login()");
        final Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);
        try {
            handler.callbackHandler().handle(callbacks);
        } catch (final IOException ioe) {
            LOG.debug("IO exception while attempting to prompt for username and password.", ioe);
            throw new LoginException(ioe.getMessage());
        } catch (final UnsupportedCallbackException uce) {
            LOG.debug("Username or password prompt not supported.", uce);
            throw new LoginException(uce.getMessage() + " not available to obtain information from user.");
        }

        final String user = ((NameCallback) callbacks[0]).getName();
        handler.setUser(user);
        if (user == null) {
            final String msg = "Username can not be null.";
            LOG.debug(msg);
            throw new LoginException(msg);
        }

        // password callback get value
        if (((PasswordCallback) callbacks[1]).getPassword() == null) {
            final String msg = "Password can not be null.";
            LOG.debug(msg);
            throw new LoginException(msg);
        }
        final String password = new String(((PasswordCallback) callbacks[1]).getPassword());

        final User configUser;
        final SpringSecurityUser onmsUser;
        try {
            configUser = handler.userConfig().getUser(user);
            onmsUser = handler.springSecurityUserDao().getByUsername(user);
        } catch (final Exception e) {
            final String message = "Failed to retrieve user " + user + " from OpenNMS UserConfig.";
            LOG.debug(message, e);
            throw new LoginException(message);
        }

        if (configUser == null) {
            final String msg = "User  " + user + " does not exist.";
            LOG.debug(msg);
            throw new FailedLoginException(msg);
        }

        if (!handler.userConfig().comparePasswords(user, password)) {
            final String msg = "Login failed: passwords did not match.";
            LOG.debug(msg);
            throw new FailedLoginException(msg);
        };

        boolean allowed = false;
        final Set<Principal> principals = new HashSet<Principal>();
        for (final GrantedAuthority auth : onmsUser.getAuthorities()) {
            final Set<Principal> ps = handler.createPrincipals(auth);
            for (final Principal p : ps) {
                if ("admin".equals(p.getName())) {
                    allowed = true;
                }
            }
            principals.addAll(ps);
        }
        handler.setPrincipals(principals);

        if (!allowed) {
            final String msg = "User " + user + " is not an administrator!  OSGi console access is forbidden.";
            LOG.debug(msg);
            throw new LoginException(msg);
        }
        LOG.debug("Successfully logged in {}.", user);
        return true;
    }
}
