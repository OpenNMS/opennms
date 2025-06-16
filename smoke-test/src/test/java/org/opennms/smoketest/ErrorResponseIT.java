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
public class ErrorResponseIT extends OpenNMSSeleniumIT {

    @Test
    public void verifyErrorResponseV1() throws IOException {
        try (HttpClientWrapper client = createClientWrapper()) {
            // "INVALID-XML" is not a valid graphml definition, therefore unmarshalling will fail.
            HttpPost httpPost = new HttpPost(getBaseUrlExternal() + "opennms/rest/graphml/test-graph");
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
            HttpGet httpGet = new HttpGet(getBaseUrlExternal() + "opennms/api/v2/nodes?_s=label=*");
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
