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

package org.opennms.plugins.elasticsearch.rest.credentials;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.hamcrest.Matchers;
import org.junit.Test;

public class CredentialsParserTest {
    @Test
    public void verifyParsing() {
        final ElasticCredentials configuration = new ElasticCredentials(){};

        // Valid
        configuration.withCredentials(new CredentialsScope("192.168.0.1:9200", "ulf", "ulf"));
        configuration.withCredentials(new CredentialsScope("http://192.168.0.2:9200", "ulf", "ulf2"));
        configuration.withCredentials(new CredentialsScope("https://192.168.0.3:9300", "ulf", "ulf3"));

        // Invalid
        configuration.withCredentials(new CredentialsScope("http://192.168.0.1:x", "ulf", "ulf"));
        configuration.withCredentials(new CredentialsScope("192.168.0.1", "ulf", null));

        final Map<AuthScope, Credentials> credentials = new CredentialsParser().parse(configuration.getCredentialsScopes());

        // Verify
        assertThat(credentials.size(), is(3));
        assertThat(credentials, Matchers.hasEntry(new AuthScope(new HttpHost("192.168.0.1", 9200, "http")), new UsernamePasswordCredentials("ulf", "ulf")));
        assertThat(credentials, Matchers.hasEntry(new AuthScope(new HttpHost("192.168.0.2", 9200,"http")), new UsernamePasswordCredentials("ulf", "ulf2")));
        assertThat(credentials, Matchers.hasEntry(new AuthScope(new HttpHost("192.168.0.3", 9300, "https")), new UsernamePasswordCredentials("ulf", "ulf3")));
    }
}