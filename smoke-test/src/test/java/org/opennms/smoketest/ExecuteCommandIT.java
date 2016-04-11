/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import static org.opennms.smoketest.OpenNMSSeleniumTestCase.BASE_URL;
import static org.opennms.smoketest.OpenNMSSeleniumTestCase.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.OpenNMSSeleniumTestCase.BASIC_AUTH_USERNAME;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.web.HttpClientWrapper;

import com.google.common.io.ByteStreams;

public class ExecuteCommandIT {

    // Verifies that the ping command still works.
    // See http://issues.opennms.org/browse/NMS-8264.
    @Test
    public void verifyPing() throws IOException {
        try (HttpClientWrapper httpClient = HttpClientWrapper.create()) {
            httpClient.addBasicCredentials(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
            httpClient.usePreemptiveAuth();
            HttpGet request = new HttpGet(BASE_URL + "opennms/ExecCommand?command=ping&address=127.0.0.1&timeout=1&numberOfRequest=4&packetSize=8");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                Assert.assertEquals(200, response.getStatusLine().getStatusCode());
                byte[] contentBytes = ByteStreams.toByteArray(response.getEntity().getContent());
                String content = new String(contentBytes);
                Assert.assertTrue(content.contains("4 packets received"));
            }
        }
    }
}
