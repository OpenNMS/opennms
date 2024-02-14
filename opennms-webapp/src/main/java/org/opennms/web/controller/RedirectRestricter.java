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
