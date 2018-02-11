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
import java.util.Objects;

import org.apache.http.HttpEntity;
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
import org.apache.http.util.EntityUtils;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.minion.core.api.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ScvEnabledRestClientImpl implements RestClient {
    public static final Logger LOG = LoggerFactory.getLogger(ScvEnabledRestClientImpl.class);

    /**
     * Name of the key at which the server's version can be found
     * in the info map returned by '/rest/info'
     */
    private static final String VERSION_KEY = "version";

    private final URL url;
    private final SecureCredentialsVault scv;
    private final String scvAlias;

    public ScvEnabledRestClientImpl(String url, SecureCredentialsVault scv, String scvAlias) throws MalformedURLException {
        this.url = new URL(url);
        this.scv = Objects.requireNonNull(scv);
        this.scvAlias = Objects.requireNonNull(scvAlias);
    }

    private UsernamePasswordCredentials getCredentials() {
        // Perform the lookup in SVC on every call, so that the client
        // does not need to be reloaded when the credentials are changed
        final Credentials credentials = scv.getCredentials(scvAlias);
        if (credentials == null) {
            LOG.warn("No credentials found in SCV for alias '{}'. Using default credentials.", scvAlias);
            return new UsernamePasswordCredentials("admin", "admin");
        }
        return new UsernamePasswordCredentials(credentials.getUsername(), credentials.getPassword());
    }

	// Setup a client with pre-emptive authentication
    private CloseableHttpResponse getResponse(HttpGet httpget)
            throws Exception {
        CloseableHttpResponse response = null;
        HttpHost target = new HttpHost(url.getHost(), url.getPort(),
                                       url.getProtocol());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(target.getHostName(),
                                                   target.getPort()),
                                                   getCredentials());
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(target, basicAuth);

        HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);

        response = httpclient.execute(target, httpget, localContext);
        return response;
    }

    @Override
    public String getVersion() throws Exception {
        // Issue a simple GET against the Info endpoint
        final HttpGet httpget = new HttpGet(url.toExternalForm() + "/rest/info");
        try (CloseableHttpResponse response = getResponse(httpget)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IllegalStateException(String.format("Oups. We were expecting a status code of 200, but got %d instead.",
                        response.getStatusLine().getStatusCode()));
            }

            final HttpEntity entity = response.getEntity();
            final String json = EntityUtils.toString(entity);
            final Gson g = new Gson();
            try {
                final JsonObject info = g.fromJson(json, JsonObject.class);
                return info.get(VERSION_KEY).getAsString();
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Failed to parse JSON: " + json, e);
            }
        }
    }

    @Override
	public void ping() throws Exception {
        if (getVersion() == null) {
            throw new Exception("Server did not return a version.");
        }
        // We were able to successfully retrieve's the server's version!
	}

    @Override
    public String getSnmpV3Users() throws Exception {
        String responseString = null;
        CloseableHttpResponse response = null;
        HttpGet httpget = new HttpGet(url.toExternalForm()
                + "/rest/config/trapd");

        response = getResponse(httpget);
        HttpEntity entity = response.getEntity();
        responseString = EntityUtils.toString(entity);
        response.close();
        return responseString;
    }

}
