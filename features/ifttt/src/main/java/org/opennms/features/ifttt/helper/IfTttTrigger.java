/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.features.ifttt.helper;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public IfTttTrigger() {
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
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
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