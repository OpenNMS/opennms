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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

public class GenericSchemaIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(GenericSchemaIT.class);

    private HttpResponse queryUri(final String uri, final String header) throws IOException {
        final HttpGet httpGet = new HttpGet(getBaseUrlExternal() + uri);
        httpGet.setHeader(HttpHeaders.ACCEPT, header);
        final HttpHost targetHost = new HttpHost(httpGet.getURI().getHost(), httpGet.getURI().getPort(), httpGet.getURI().getScheme());
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), new UsernamePasswordCredentials("admin", "admin"));
        final AuthCache authCache = new BasicAuthCache();
        final BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        final HttpResponse closeableHttpResponse = HttpClients.createDefault().execute(targetHost, httpGet, context);
        System.out.println(closeableHttpResponse.getEntity());
        return closeableHttpResponse;
    }


    @Test
    public void testConfigurationLoadFromConfiguration() throws IOException {

        final String uri = "/opennms/rest/cm";
        HttpResponse configNames  = queryUri(uri, "application/json");
        LOG.info("Checking for data loading from db for provisiond'{}'", uri);
        HttpEntity entity = configNames.getEntity();
        String entityBody = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
        String configName= entityBody.replace("[","").replace("]","");
        List<String> list = Arrays.asList(configName.split("\\s*,\\s*"));
        for(int i =0;i<list.size();i++) {
            String schemaName = list.get(i).replaceAll("\"", "");
            String uriSchema = uri + "/schema/" + schemaName;

            HttpResponse schema = queryUri(uriSchema, "application/json");
            HttpEntity schemaEntity = schema.getEntity();
            String jsonString = EntityUtils.toString(schemaEntity);
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONObject path = new JSONObject(jsonObject.get("paths").toString());
            LOG.info("path : " + path);
            JSONObject get = new JSONObject(path.get("/rest/cm/" + schemaName).toString());
            JSONObject getDetails = new JSONObject(get.get("get").toString());
            boolean responseCodeCheck = getDetails.get("responses").toString().contains("200");
            boolean tagCheck = getDetails.get("tags").toString().contains(schemaName);
            assertEquals(true, tagCheck);
            assertEquals(true, responseCodeCheck);
        }
    }

}
