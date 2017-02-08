/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/
package org.opennms.minion.core.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.minion.core.api.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScvEnabledRestClientImpl implements RestClient {
    public static final Logger LOG = LoggerFactory.getLogger(ScvEnabledRestClientImpl.class);

    private final URL url;
    private final String username;
    private final String password;

    public ScvEnabledRestClientImpl(String url, SecureCredentialsVault scv, String scvAlias) throws MalformedURLException {
        this.url = new URL(url);
        final Credentials amqCredentials = scv.getCredentials(scvAlias);
        if (amqCredentials == null) {
            LOG.warn("No credentials found in SCV for alias '{}'. Using default credentials.", scvAlias);
            username = "admin";
            password = "admin";
        } else {
            username = amqCredentials.getUsername();
            password = amqCredentials.getPassword();
        }
    }

    @Override
    public void ping() throws Exception {
        // Setup a client with pre-emptive authentication
        HttpHost target = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(username, password));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider).build();
        try {
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(target, basicAuth);

            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            // Issue a simple GET against the Info endpoint
            HttpGet httpget = new HttpGet(url.toExternalForm() + "/rest/info");
            CloseableHttpResponse response = httpclient.execute(target, httpget, localContext);
            response.close();
        } finally {
            httpclient.close();
        }
    }
}
