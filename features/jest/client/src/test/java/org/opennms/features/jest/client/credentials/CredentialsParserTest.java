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
package org.opennms.features.jest.client.credentials;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.MapScope;
import org.opennms.core.mate.api.Scope;

public class CredentialsParserTest {
    @Test
    public void verifyParsing() {
        final ElasticCredentials configuration = new ElasticCredentials(){};

        // Valid
        configuration.withCredentials(new CredentialsScope("192.168.0.1:9200", "ulf", "ulf"));
        configuration.withCredentials(new CredentialsScope("http://192.168.0.2:9200", "${scv:elastic:username}", "${scv:elastic:password}"));
        configuration.withCredentials(new CredentialsScope("https://192.168.0.3:9300", "ulf", "ulf3"));

        // Invalid
        configuration.withCredentials(new CredentialsScope("http://192.168.0.1:x", "ulf", "ulf"));
        configuration.withCredentials(new CredentialsScope("192.168.0.1", "ulf", null));

        final Map<ContextKey, String> scopeMap = new HashMap<>();
        scopeMap.put(new ContextKey("scv","elastic:username"),"foo");
        scopeMap.put(new ContextKey("scv","elastic:password"),"bar");

        final EntityScopeProvider entityScopeProvider = mock(EntityScopeProvider.class);
        when(entityScopeProvider.getScopeForScv()).thenReturn(new MapScope(Scope.ScopeName.DEFAULT, scopeMap));

        final Map<AuthScope, Credentials> credentials = new CredentialsParser().parse(configuration.getCredentialsScopes(), entityScopeProvider);

        // Verify
        assertThat(credentials.size(), is(3));
        assertThat(credentials, Matchers.hasEntry(new AuthScope(new HttpHost("192.168.0.1", 9200, "http")), new UsernamePasswordCredentials("ulf", "ulf")));
        assertThat(credentials, Matchers.hasEntry(new AuthScope(new HttpHost("192.168.0.2", 9200,"http")), new UsernamePasswordCredentials("foo", "bar")));
        assertThat(credentials, Matchers.hasEntry(new AuthScope(new HttpHost("192.168.0.3", 9300, "https")), new UsernamePasswordCredentials("ulf", "ulf3")));
    }
}