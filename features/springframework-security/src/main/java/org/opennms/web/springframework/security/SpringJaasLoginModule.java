package org.opennms.web.springframework.security;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.jaas.SecurityContextLoginModule;

public class SpringJaasLoginModule extends SecurityContextLoginModule {
    private static final Log log = LogFactory.getLog(SpringJaasLoginModule.class);

    public SpringJaasLoginModule() {
        log.debug("SpringJaasLoginModule initialized");
    }

    @Override
    public boolean abort() throws LoginException {
        log.debug("abort() called");
        return super.abort();
    }

    @Override
    public boolean commit() throws LoginException {
        log.debug("commit() called");
        return super.commit();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        log.debug("initialize(" + subject + ", " + callbackHandler + ", " + sharedState + ", " + options + ") called");
        super.initialize(subject, callbackHandler, sharedState, options);
    }

    @Override
    public boolean login() throws LoginException {
        log.debug("login() called");
        return super.login();
    }

    @Override
    public boolean logout() throws LoginException {
        log.debug("logout() called");
        return super.logout();
    }
}
