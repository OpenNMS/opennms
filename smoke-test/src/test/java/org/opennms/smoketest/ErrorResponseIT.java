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

package org.opennms.smoketest;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.opennms.core.web.HttpClientWrapper;

/**
 * Verifies that the OpenNMS ReST endpoints handle uncatched Exceptions accordingly and do not return the general JSP error page.
 * See HZN-1108.
 */
public class ErrorResponseIT extends OpenNMSSeleniumTestCase {

    @Test
    public void verifyErrorResponseV1() throws IOException {
        try (HttpClientWrapper client = createClientWrapper()) {
            // "INVALID-XML" is not a valid graphml definition, therefore unmarshalling will fail.
            HttpPost httpPost = new HttpPost(getBaseUrl() + "opennms/rest/graphml/test-graph");
            httpPost.setHeader("Accept", "application/xml");
            httpPost.setHeader("Content-Type", "application/xml");
            httpPost.setEntity(new StringEntity("INVALID-XML"));
            CloseableHttpResponse response = client.execute(httpPost);
            verify(response);
        }
    }

    @Test
    public void verifyErrorResponseV2() throws IOException {
        try (HttpClientWrapper client = createClientWrapper()) {
            // The FIQL parser expects == and cannot handle =.
            HttpGet httpGet = new HttpGet(getBaseUrl() + "opennms/api/v2/nodes?_s=label=*");
            httpGet.setHeader("Accept", "application/json");
            CloseableHttpResponse response = client.execute(httpGet);
            verify(response);
        }
    }

    private static HttpClientWrapper createClientWrapper() {
        HttpClientWrapper wrapper = HttpClientWrapper.create();
        wrapper.addBasicCredentials(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
        return wrapper;
    }

    private static void verify(CloseableHttpResponse response) throws IOException {
        assertEquals(500, response.getStatusLine().getStatusCode());

        // Verify response entity. It should contain error information and should not be the JSP
        final String responseEntity = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                .lines().collect(Collectors.joining("\n")).trim();
        assertThat(responseEntity, not(containsString("<!DOCTYPE html>")));
        assertThat(responseEntity, not(containsString("<html")));
        assertThat(responseEntity, not(containsString("</html>")));
    }
}
