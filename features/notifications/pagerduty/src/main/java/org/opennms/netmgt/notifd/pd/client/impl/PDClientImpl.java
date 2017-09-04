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

package org.opennms.netmgt.notifd.pd.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.netmgt.notifd.pd.client.api.PDClient;
import org.opennms.netmgt.notifd.pd.client.api.PDEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PDClientImpl implements PDClient {
    private static final Logger LOG = LoggerFactory.getLogger(PDClientImpl.class);

    private final HttpClientWrapper clientWrapper;
    private final ObjectMapper mapper = new ObjectMapper();

    public PDClientImpl() {
        clientWrapper = HttpClientWrapper.create()
                .useSystemProxySettings();
    }

    public void sendEvent(PDEvent event) throws IOException {
        final HttpPost post = new HttpPost("https://events.pagerduty.com/v2/enqueue");
        post.addHeader("accept", "application/json");
        final StringEntity body = new StringEntity(mapper.writeValueAsString(event));
        body.setContentType("application/json");
        post.setEntity(body);

        try(CloseableHttpResponse response = clientWrapper.execute(post)) {
            final int responseStatusCode = response.getStatusLine().getStatusCode();
            final String responseBody = EntityUtils.toString(response.getEntity());
            LOG.debug("Response ({}): {}", responseStatusCode, responseBody);
        }

        // Response (202): {"status":"success","message":"Event processed"," dedup_key":"2054032b780743fe87c80707d111ea59"}
        // Response (400): {"status":"invalid event","message":"Event object is invalid","errors":["'payload.source' is missing or blank"]}
    }

    @Override
    public void close() throws IOException {
        clientWrapper.close();
    }
}
