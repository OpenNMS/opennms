/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.searchbox.client.AbstractJestClient;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

// TODO MVR merge with RestClientFactory in opennms-es-rest module/project

// TODO MVR allow setting proxy
// TODO MVR allow polling
// TODO MVR allow setting password for each url, etc.
// TODO MVR support for async
public class ClientFactory {

    private final HttpClientConfig.Builder clientConfigBuilder;

    private JestClient client;

    public ClientFactory(String elasticURL, String elasticUser, String elasticPassword) {
        final List<String> urls = parseUrl(elasticURL);
        final String user = elasticUser != null && !elasticUser.isEmpty() ? elasticUser : null;
        final String password = elasticPassword != null && !elasticPassword.isEmpty() ? elasticPassword : null;

        // Ensure urls is set
        if (urls.isEmpty()) {
            throw new IllegalArgumentException("No urls have been provided");
        }

        // TODO MVR Fix the following hack:
        // This is a hack, due to our osgi-jetty-bridge: The annotation at the NetflowDocument
        // living in the api module (on the jetty side) are not accessible directly (only as proxy) by the Gson lirary
        // living in the osgi container (osgi-jest-complete module) and therefore @SerializedName annotations don't work.
        // So we instantiate a custom gson with a custom field name policy.
        final Gson gson = new GsonBuilder()
                .setDateFormat(AbstractJestClient.ELASTIC_SEARCH_DATE_FORMAT)
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        clientConfigBuilder = new HttpClientConfig.Builder(elasticURL)
                .multiThreaded(true)
                .defaultMaxTotalConnectionPerRoute(2)
                .maxTotalConnection(20)
                .gson(gson);

        // Apply optional credentials
        if (user != null && password != null) {
            clientConfigBuilder.defaultCredentials(user, password);
        }
    }

    public void setMultiThreaded(boolean multiThreaded) {
        clientConfigBuilder.multiThreaded(multiThreaded);
    }

    public void setConnTimeout(int timeout) {
        clientConfigBuilder.connTimeout(timeout);
    }

    public void setReadTimeout(int timeout) {
        clientConfigBuilder.readTimeout(timeout);
    }

    public void setDefaultMaxTotalConnectionPerRoute(int count) {
        clientConfigBuilder.defaultMaxTotalConnectionPerRoute(count);
    }

    public void setMaxTotalConnection(int count) {
        clientConfigBuilder.maxTotalConnection(count);
    }

    public void setMaxConnectionIdleTime(int timeout, TimeUnit unit) {
        clientConfigBuilder.maxConnectionIdleTime(timeout, unit);
    }

    private List<String> parseUrl(String elasticURL) {
        if (elasticURL != null) {
            final Set<String> endpoints = Arrays.stream(elasticURL.split(","))
                .filter(url -> url != null && !url.trim().isEmpty())
                .map(url -> url.trim()).collect(Collectors.toSet());
            return new ArrayList<>(endpoints);
        }
        return Collections.emptyList();
    }

    public void destroy() {
        try {
            if (this.client != null) {
                this.client.shutdownClient();
            }
        } finally {
            this.client = null;
        }
    }

    public JestClient getClient() {
        if (this.client == null) {
            JestClientFactory factory = new JestClientFactory();
            factory.setHttpClientConfig(this.clientConfigBuilder.build());
            this.client = factory.getObject();
        }
        return this.client;
    }

}
