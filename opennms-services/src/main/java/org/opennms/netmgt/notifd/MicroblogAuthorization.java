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
