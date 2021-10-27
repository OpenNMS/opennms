/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.cm;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.RestSessionIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnlinkdDBConfigIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(RestSessionIT.class);

    private JSONObject queryUri(final String uri, final String header) throws IOException {
        final HttpGet httpGet = new HttpGet(getBaseUrlExternal() + uri);
        final HttpHost targetHost = new HttpHost(httpGet.getURI().getHost(), httpGet.getURI().getPort(), httpGet.getURI().getScheme());
        System.out.println("****************************** URI :"+httpGet.getURI());
        System.out.println("****************************** HOST :"+httpGet.getURI().getHost());
        System.out.println("****************************** PORT :"+httpGet.getURI().getPort());
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), new UsernamePasswordCredentials("admin", "admin"));
        final AuthCache authCache = new BasicAuthCache();
        final BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        final CloseableHttpResponse closeableHttpResponse = HttpClients.createDefault().execute(targetHost, httpGet, context);
        System.out.println(closeableHttpResponse.getEntity());
        JSONObject result = null;
        if (closeableHttpResponse.getEntity() != null) {
            // return it as a String
             result = new JSONObject(EntityUtils.toString(closeableHttpResponse.getEntity()));
            System.out.println(EntityUtils.toString(closeableHttpResponse.getEntity()));



        }

        closeableHttpResponse.close();
        return result;
    }

    /**
     * Verifies that session will not be created by calls to the ReST V1 Api.
     * <p>
     * See NMS-8093.
     */
    @Test
    public void checkEnlinkdConfiguration() throws ClientProtocolException, IOException, InterruptedException {
        final String uri = "/opennms/rest/cm/enlinkd/default";
        LOG.info("Checking for data loading from db '{}'", uri);
        final JSONObject result  = queryUri(uri, "DB-Config");

        assertEquals(10,result.get("rescanThreads"));
        assertEquals(10,result.get("rescanThreads"));

    }

}
