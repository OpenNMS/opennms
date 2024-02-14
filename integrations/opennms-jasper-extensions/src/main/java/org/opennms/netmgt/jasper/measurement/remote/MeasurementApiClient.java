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
package org.opennms.netmgt.jasper.measurement.remote;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Posts a given query to a given url using optional username and password.
 */
class MeasurementApiClient {

    protected static final int CONNECT_TIMEOUT = 2500;

    protected static final int READ_TIMEOUT = 10000;

    private static final Logger LOG = LoggerFactory.getLogger(MeasurementApiClient.class);

    private final int connectTimeout;

    private final int readTimeout;

    private HttpURLConnection connection;

    public MeasurementApiClient() {
        this(CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    public MeasurementApiClient(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public Result execute(final boolean useSsl, final String url, final String username, final String password, final String query) throws IOException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "The provided URL must not be empty or null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(query), "The provided query must not be empty or null");
        log(url, username, password, query);
        connect(useSsl, url, username, password);
        write(query.getBytes(), connection.getOutputStream());
        Result result = createResult(connection);
        LOG.debug("Request to URL '{}' returned with status: {} ({})", url, result.getResponseCode(), result.getResponseMessage());
        return result;
    }

    public void disconnect() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    private String createBasicAuthHeader(String username, String password) {
        final String pass = String.format("%s:%s", username, password);
        final String basicAuthHeader = "Basic " + new String(BaseEncoding.base64().encode(pass.getBytes()));
        return basicAuthHeader;
    }

    private void connect(final boolean useSsl, final String url, final String username, final String password) throws IOException {
        connection = (HttpURLConnection) new URL(url).openConnection();

        // if ssl is enabled and the connection is not an SSL-Exception abort
        if (useSsl && !(connection instanceof HttpsURLConnection)) {
            throw new SSLException("A secure connection is expected but was not established. Use SSL = " + useSsl + ", URL = " + url);
        }

        // if ssl is not enabled, but the connection is SSL-enabled, warn
        if (!useSsl && connection instanceof HttpsURLConnection) {
            LOG.warn("A secure connection was established even if it was not intended. Use SSL = {}, URL = {}", useSsl, url);
        }

        // verify if authentication is required
        if (isAuthenticationRequired(username, password)) {
            connection.setRequestProperty("Authorization", createBasicAuthHeader(username, password));
        }
        connection.setAllowUserInteraction(false);
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/xml");
        connection.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());
        connection.setRequestProperty("Content-Type", "application/xml");
        connection.setInstanceFollowRedirects(false); // we do not want to follow redirects, otherwise 200 OK might be returned
        connection.connect();
    }

    private static void write(byte[] input, OutputStream outputStream) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
        ByteStreams.copy(inputStream, outputStream);
    }

    protected static boolean isAuthenticationRequired(String username, String password) {
        return !Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password);
    }

    private static void log(String url, String username, String password, String query) {
        LOG.info("Connecting to {}", url);

        if (isAuthenticationRequired(username, password)) {
            LOG.info("Using authentication: YES");
            LOG.info("Using username {}", username);
            LOG.info("Using password {}", "*******");
        } else {
            LOG.info("Using authentication: NO");
        }
        LOG.info("Query Request: {}", query);
    }

    private static Result createResult(HttpURLConnection connection) throws IOException {
        Result result = new Result();
        result.setResponseCode(connection.getResponseCode());
        result.setResponseMessage(connection.getResponseMessage());
        result.setSecureConnection(connection instanceof HttpsURLConnection);
        if (result.wasSuccessful()) {
            result.setInputStream(connection.getInputStream());
        } else {
            result.setErrorStream(connection.getErrorStream());
        }
        return result;
    }
}
