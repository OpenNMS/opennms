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
package org.opennms.core.camel;

import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.camel.component.http.HttpClientConfigurer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.opennms.core.utils.AnyServerX509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomHttpClientConfigurer implements HttpClientConfigurer {
    private static final KeyManager[] EMPTY_KEYMANAGER_ARRAY = new KeyManager[0];
    private static final Logger LOG = LoggerFactory.getLogger(CustomHttpClientConfigurer.class);

    private String m_username = "admin";
    private String m_password = "admin";

    @Override
    public void configureHttpClient(final HttpClient client) {
        try {
            final SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(EMPTY_KEYMANAGER_ARRAY, new TrustManager[] { new AnyServerX509TrustManager() }, new SecureRandom());
            SSLContext.setDefault(ctx);

            final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(getUsername(), getPassword());
            client.getState().setCredentials(AuthScope.ANY, credentials);
            client.getParams().setAuthenticationPreemptive(true);
            LOG.debug("Configuring HTTP client with modified trust manager, username={}, password=xxxxxxxx", getUsername());
        } catch (final Exception e) {
            throw new CustomConfigurerException(e);
        }
    }


    public String getUsername() {
        return m_username;
    }

    public void setUsername(String username) {
        m_username = username;
    }

    public String getPassword() {
        return m_password;
    }

    public void setPassword(String password) {
        m_password = password;
    }


}
