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
package org.opennms.netmgt.endpoints.grafana.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

import com.google.common.base.Preconditions;

public class GrafanaServerConfiguration {

    public static int DEFAULT_CONNECT_TIMEOUT_IN_SECONDS = 30;
    public static int DEFAULT_READ_TIMEOUT_IN_SECONDS = 30;

    private final String url;
    private final String apiKey;
    private final int connectTimeoutSeconds;
    private final int readTimeoutSeconds;

    public static GrafanaServerConfiguration fromEnv() {
        final File configFile = Paths.get(System.getProperty("user.home"), ".grafana", "server.properties").toFile();
        try (InputStream input = new FileInputStream(configFile)) {
            final Properties prop = new Properties();
            prop.load(input);
            final String url = prop.getProperty("url");
            final String apiKey = prop.getProperty("apiKey");
            final int connectTimeout = Integer.parseInt(prop.getProperty("connectTimeout", Integer.toString(DEFAULT_CONNECT_TIMEOUT_IN_SECONDS)));
            final int readTimeout = Integer.parseInt(prop.getProperty("readTimeout", Integer.toString(DEFAULT_READ_TIMEOUT_IN_SECONDS)));
            return new GrafanaServerConfiguration(url, apiKey, connectTimeout, readTimeout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GrafanaServerConfiguration(String url, String apiKey, Integer connectTimeoutSeconds, Integer readTimeoutSeconds) {
        this.url = Objects.requireNonNull(url);
        this.apiKey = Objects.requireNonNull(apiKey);
        this.connectTimeoutSeconds = connectTimeoutSeconds == null ? DEFAULT_CONNECT_TIMEOUT_IN_SECONDS : connectTimeoutSeconds;
        this.readTimeoutSeconds = readTimeoutSeconds == null ? DEFAULT_READ_TIMEOUT_IN_SECONDS : readTimeoutSeconds;

        Preconditions.checkArgument(this.connectTimeoutSeconds >= 0, "connectTimeoutSeconds must be >= 0");
        Preconditions.checkArgument(this.readTimeoutSeconds >= 0, "readTimeoutSeconds must be >= 0");
    }

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    @Override
    public String toString() {
        return "GrafanaServerConfiguration{" +
                "url='" + url + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", connectTimeoutSeconds=" + connectTimeoutSeconds +
                ", readTimeoutSeconds=" + readTimeoutSeconds +
                '}';
    }
}
