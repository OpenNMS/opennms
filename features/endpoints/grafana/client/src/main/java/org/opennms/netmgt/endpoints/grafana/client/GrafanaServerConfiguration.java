/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
