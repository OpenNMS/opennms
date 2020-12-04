/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
