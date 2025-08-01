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
package org.opennms.netmgt.notifd;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class MicroblogAuthorization {
    private final Twitter m_twitter;
    private final RequestToken m_requestToken;

    public MicroblogAuthorization(final Twitter twitter) throws MicroblogAuthorizationException {
        m_twitter = twitter;
        try {
            m_requestToken = twitter.getOAuthRequestToken();
        } catch (final TwitterException e) {
            throw new MicroblogAuthorizationException("Unable to get OAuth request token", e);
        }
    }

    public String getUrl() {
        return m_requestToken.getAuthenticationURL();
    }

    public AccessToken retrieveToken() throws MicroblogAuthorizationException {
        try {
            return m_twitter.getOAuthAccessToken(m_requestToken);
        } catch (final TwitterException e) {
            throw new MicroblogAuthorizationException(e);
        }
    }

    public AccessToken retrieveToken(final String pin) throws MicroblogAuthorizationException {
        try {
            return m_twitter.getOAuthAccessToken(m_requestToken, pin);
        } catch (final TwitterException e) {
            throw new MicroblogAuthorizationException(e);
        }
    }

    public static class MicroblogAuthorizationException extends Exception {
        private static final long serialVersionUID = -2319636949583935715L;

        public MicroblogAuthorizationException() {
            super();
        }

        public MicroblogAuthorizationException(final String message) {
            super(message);
        }

        public MicroblogAuthorizationException(final Throwable t) {
            super(t);
        }

        public MicroblogAuthorizationException(final String message, final Throwable t) {
            super(message, t);
        }

    }

}
