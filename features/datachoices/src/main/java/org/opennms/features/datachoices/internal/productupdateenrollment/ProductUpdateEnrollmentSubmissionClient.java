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
package org.opennms.features.datachoices.internal.productupdateenrollment;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductUpdateEnrollmentSubmissionClient {
    private static final Logger LOG = LoggerFactory.getLogger(ProductUpdateEnrollmentSubmissionClient.class);

    private String endpointUrl;

    public void postForm(String json) throws Exception, IOException, InterruptedException {
        final HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .setHeader("User-Agent", "OpenNMS Product Update Enrollment")
                .build();

        LOG.info("Sending Product Update Enrollment submission form data to: {}", endpointUrl);
        final HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
           LOG.info("Product Update Enrollment submission accepted.");
        } else {
            LOG.error("Received error response from submission endpoint. Status code: {}. Body: {}.", response.statusCode(), response.body());
            throw new Exception("Received error response from submission endpoint.");
        }
    }

    public void setEndpointUrl(String url) {
        this.endpointUrl = url;
    }
}
