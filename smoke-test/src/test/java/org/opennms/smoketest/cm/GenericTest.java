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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.junit.Test;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

public class GenericTest {
    public static  String getConfigurationsNames(String uri){
        HttpGet request = new HttpGet(uri);
        request.addHeader("accept", "application/json");
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("admin", "admin")
        );
        String entityBody ="";
        try (CloseableHttpClient httpClientConf= HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build())
        {
            HttpResponse httpResponse = httpClientConf.execute(request) ;
            HttpEntity entity =httpResponse.getEntity();
            entityBody = IOUtils.toString( entity.getContent(), StandardCharsets.UTF_8 );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entityBody;
    }
    @Test
    public void testConfigurationLoadFromConfiguration() throws IOException {
        String uriSchemaNames = "http://20.102.41.29:8980/opennms/rest/cm";
        String configNames= getConfigurationsNames(uriSchemaNames);
        String configName= configNames.replace("[\"","").replace("\"]","");
        String uriSchema = uriSchemaNames+"/schema/"+configName ;
        String schema = getConfigurationsNames(uriSchema);


            JSONObject result = new JSONObject(schema);
            JSONObject path = new JSONObject(result.get("paths").toString());
            JSONObject get = new JSONObject(path.get("/opennms/rest/cm/"+configName).toString());
            JSONObject getDetails = new JSONObject(get.get("get").toString());
            String  tag =getDetails.get("tags").toString();
            boolean  responseCodeCheck = getDetails.get("responses").toString().contains("200");
             assertEquals(configNames,tag);
             assertEquals(true,responseCodeCheck);
            }



    }
