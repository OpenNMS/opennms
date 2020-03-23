package org.opennms.web.controller;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Open redirects are a security issue:
 * https://www.netsparker.com/blog/web-security/open-redirection-vulnerability-information-prevention/
 * Therefor we need to make sure that we react only on allowed redirects.
 * This class helps validate redirect requests against a whitelist.
 */
public class RedirectRestricter {
    private final Set<String> allowedRedirects;

    private RedirectRestricter(final RedirectRestricterBuilder builder) {
        this.allowedRedirects = builder.allowedRedirects;
    }

    public boolean isRedirectAllowed(final String redirect) {
        if(redirect == null || redirect.isEmpty()) {
            return false;
        }
        String redirectWithoutParameters =  new StringTokenizer(redirect, "?", false).nextToken();
        return this.allowedRedirects.contains(redirectWithoutParameters);
    }

    /** Returns the given redirect if allowed, otherwise null. */
    public String getRedirectOrNull(final String redirect) {
        return isRedirectAllowed(redirect) ? redirect : null;
    }

    public static RedirectRestricterBuilder builder() {
        return new RedirectRestricterBuilder();
    }

    public static class RedirectRestricterBuilder {
        private final Set<String> allowedRedirects = new HashSet<>();

        public RedirectRestricterBuilder allowRedirect(final String allowedRedirect) {
            Objects.requireNonNull(allowedRedirect, "allowed redirect cannot be null.");
            if(allowedRedirect.isEmpty()) {
                throw new IllegalArgumentException("allowed redirect cannot be empty.");
            }
            this.allowedRedirects.add(allowedRedirect);
            return this;
        }

        public RedirectRestricter build () {
            return new RedirectRestricter(this);
        }
    }
}
