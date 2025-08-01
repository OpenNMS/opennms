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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.opennms.netmgt.config.microblog.MicroblogProfile;
import org.opennms.netmgt.dao.api.MicroblogConfigurationDao;
import org.opennms.netmgt.dao.jaxb.DefaultMicroblogConfigurationDao;
import org.opennms.netmgt.notifd.MicroblogAuthorization.MicroblogAuthorizationException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class MicroblogClient {
    private final MicroblogConfigurationDao m_configDao;

    private SecureCredentialsVault m_secureCredentialsVault = JCEKSSecureCredentialsVault.defaultScv();

    public MicroblogClient(final MicroblogConfigurationDao dao) {
        m_configDao = dao;
    }

    public MicroblogClient(final Resource configResource) {
        final DefaultMicroblogConfigurationDao dao = new DefaultMicroblogConfigurationDao();
        dao.setConfigResource(configResource);
        dao.afterPropertiesSet();
        m_configDao = dao;
    }

    public MicroblogClient(final Resource configResource, final SecureCredentialsVault secureCredentialsVault) {
        this(configResource);
        m_secureCredentialsVault = secureCredentialsVault;
    }

    public static void main(final String[] args) throws Exception {
        System.out.println("=== Configure Microblog Authentication ===");
        System.out.println("");

        final String configPath = System.getProperty("opennms.home") + File.separator + "etc" + File.separator + "microblog-configuration.xml";
        final File configFile = new File(configPath);
        if (!configFile.exists()) usage();

        String profile = null;
        if (args.length > 0) {
            profile = args[0];
        }

        try {
            final MicroblogClient client = new MicroblogClient(new FileSystemResource(configFile));

            int step = 1;
            final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            if (!client.hasOAuth(profile)) {
                System.out.println("This utility is for connecting OpenNMS notifications with Twitter, or any other");
                System.out.println("microblog site which uses OAuth.  These examples will use Twitter URLs, but you");
                System.out.println("should be able to do the equivalent with any Twitter-compatible site like identi.ca.");
                System.out.println("");
                System.out.println("If you wish to use a username and password instead, just enter them into your");
                System.out.println("microblog-configuration.xml file.");
                System.out.println("");
                System.out.println("Step " + step++ + ".  Go to https://twitter.com/oauth_clients/new and create a Twitter");
                System.out.println("\"application\" for your OpenNMS install.  If you have already created an application,");
                System.out.println("you can get the info you need for the next steps at https://dev.twitter.com/apps/");
                System.out.println("instead.  Make sure you go to 'Keys and Access Tokens' and configure the 'Access Level'");
                System.out.println("to allow 'Read, Write and Access direct messages'.");
                System.out.println("");
                System.out.print("Step " + step++ + ".  Enter your consumer key: ");
                final String consumerKey = br.readLine();
                System.out.println("");
                System.out.print("Step " + step++ + ".  Enter your consumer secret: ");
                final String consumerSecret = br.readLine();
                System.out.println("");
                
                client.getProfile(profile).setOauthConsumerKey(consumerKey);
                client.getProfile(profile).setOauthConsumerSecret(consumerSecret);

                if (!client.hasOAuth(profile)) {
                    System.err.println("Something went wrong, either your consumer key or secret were empty.  Bailing.");
                    System.exit(1);
                }
            }

            final MicroblogAuthorization auth = client.requestAuthorization(profile);

            System.out.println("Step " + step++ + ".  Go to " + auth.getUrl());
            System.out.println("in your browser and authorize OpenNMS.");
            System.out.println("");
            System.out.print("Step " + step++ + ".  Type your PIN from the web page, or hit ENTER if there is no PIN: ");

            final String pin = br.readLine();
            AccessToken token = null;
            if (pin == null || pin.length() == 0 || !pin.matches("^[0-9]*$")) {
                System.err.println("No pin, or pin input was not numeric.  Trying pinless auth.");
                token = auth.retrieveToken();
            } else {
                token = auth.retrieveToken(pin);
            }

            System.out.println("");
            System.out.println("Step " + step + ".  There is no step " + step++ + ".");
            System.out.println("");
            System.out.print("Saving tokens to "+ configPath + "... ");
            client.saveAccessToken(profile, token);
            System.out.println("done");
            System.out.println("");
        } catch (final Exception e) {
            System.err.println("Failed to get access token.");
            if (e instanceof RuntimeException) throw (RuntimeException)e;
            throw new RuntimeException(e);
        }
    }

    private void saveAccessToken(final String profile, final AccessToken token) throws IOException {
        final MicroblogProfile mp = getProfile(profile);
        mp.setOauthAccessToken(token.getToken());
        mp.setOauthAccessTokenSecret(token.getTokenSecret());
        m_configDao.saveProfile(mp);
    }

    public MicroblogProfile getProfile(final String profile) {
        final MicroblogProfile mp = m_configDao.getProfile(profile);
        if (mp != null) {
            return mp;
        } else {
            return m_configDao.getDefaultProfile();
        }
    }

    private static void usage() {
        System.out.println("usage: microblog-auth [profile]");
        System.out.println("");
        System.out.println("  profile: The profile in microblog-configuration.xml to update. (optional)");
        System.out.println("");
        System.exit(1);
    }

    public boolean isOAuthUsable(final String profile) {
        return hasOAuth(profile) && hasOAuthAccessToken(profile);
    }

    public boolean hasOAuth(final String profile) {
        final MicroblogProfile mp = getProfile(profile);
        if (mp == null) return false;

        return !isEmpty(mp.getOauthConsumerKey()) && !isEmpty(mp.getOauthConsumerSecret());
    }

    public boolean hasOAuthAccessToken(final String profile) {
        final MicroblogProfile mp = getProfile(profile);
        if (mp == null) return false;

        return !isEmpty(mp.getOauthAccessToken()) && !isEmpty(mp.getOauthAccessTokenSecret());
    }

    public boolean hasBasicAuth(final String profile) {
        final MicroblogProfile mp = getProfile(profile);
        if (mp == null) return false;

        return !isEmpty(mp.getAuthenUsername()) && !isEmpty(mp.getAuthenPassword());
    }

    private static boolean isEmpty(final Optional<String> value) {
        return !value.isPresent() || "".equals(value.get());
    }

    public MicroblogAuthorization requestAuthorization(final String profile) throws MicroblogAuthorizationException {
    	return new MicroblogAuthorization(getTwitter(profile));
    }

    public Twitter getTwitter(final String profile) {
        final MicroblogProfile mp = getProfile(profile);

        final ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setClientURL(mp.getServiceUrl());

        final Scope scope = new SecureCredentialsVaultScope(m_secureCredentialsVault);

        if (!isEmpty(mp.getOauthConsumerKey()))       builder.setOAuthConsumerKey(Interpolator.interpolate(mp.getOauthConsumerKey().orElse(null), scope).output);
        if (!isEmpty(mp.getOauthConsumerSecret()))    builder.setOAuthConsumerSecret(Interpolator.interpolate(mp.getOauthConsumerSecret().orElse(null), scope).output);
        if (!isEmpty(mp.getOauthAccessToken()))       builder.setOAuthAccessToken(Interpolator.interpolate(mp.getOauthAccessToken().orElse(null),scope).output);
        if (!isEmpty(mp.getOauthAccessTokenSecret())) builder.setOAuthAccessTokenSecret(Interpolator.interpolate(mp.getOauthAccessTokenSecret().orElse(null),scope).output);
        if (!isEmpty(mp.getAuthenUsername()))         builder.setUser(Interpolator.interpolate(mp.getAuthenUsername().orElse(null), scope).output);
        if (!isEmpty(mp.getAuthenPassword()))         builder.setPassword(Interpolator.interpolate(mp.getAuthenPassword().orElse(null), scope).output);

        return new TwitterFactory(builder.build()).getInstance();
    }
}
