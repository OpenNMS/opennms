/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
