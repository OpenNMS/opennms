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
package org.opennms.features.ifttt.helper;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.opennms.core.utils.RelaxedX509ExtendedTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Helper class for constructing and invoking IFTTT requests.
 */
public class IfTttTrigger {
    private static final Logger LOG = LoggerFactory.getLogger(IfTttTrigger.class);
    private final String IFTTT_URL = "https://maker.ifttt.com/trigger/%s/with/key/%s";
    private final String IFTTT_JSON = "{\"value1\":\"%s\",\"value2\":\"%s\",\"value3\":\"%s\"}";

    private String key = "key";
    private String event = "event";
    private String value1 = "";
    private String value2 = "";
    private String value3 = "";
    private SSLContext sslContext;

    public IfTttTrigger() {
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { new RelaxedX509ExtendedTrustManager() }, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LOG.error("Error creating SSL context", e);
        }
    }

    public IfTttTrigger key(final String key) {
        this.key = key;
        return this;
    }

    public IfTttTrigger value1(final String value1) {
        this.value1 = value1;
        return this;
    }

    public IfTttTrigger value2(final String value2) {
        this.value2 = value2;
        return this;
    }

    public IfTttTrigger value3(final String value3) {
        this.value3 = value3;
        return this;
    }

    public IfTttTrigger event(final String event) {
        this.event = event;
        return this;
    }

    public void trigger() {
        try (final CloseableHttpClient httpclient = HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build()) {
            LOG.debug("Sending '" + event + "' event to IFTTT.");

            final HttpPost httpPost = new HttpPost(String.format(IFTTT_URL, event, key));
            httpPost.setHeader("Content-type", "application/json");

            final StringEntity stringEntity = new StringEntity(String.format(IFTTT_JSON, value1, value2, value3));
            httpPost.setEntity(stringEntity);

            final CloseableHttpResponse closeableHttpResponse = httpclient.execute(httpPost);
            final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                LOG.warn("Received HTTP Status {} for request to {} with body {}", statusCode, httpPost.getURI(), httpPost.getEntity());
            }
        } catch (IOException e) {
            LOG.error("Error invoking request: {}", e);
        }
    }
}