/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.opennms.core.web.HttpClientWrapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;

import static org.junit.Assert.assertEquals;

public class BSMAdminIT extends OpenNMSSeleniumTestCase {

    @Test
    public void canReadBusinessServices() throws ClientProtocolException, IOException {
        // Create a new Business Service via the REST API
        String servicePrefix = UUID.randomUUID().toString();
        createServiceWithPrefix(servicePrefix);

        // Verify that the Business Service appears in the list
        m_driver.get(BASE_URL + "opennms/admin/bsm/index.jsp");
        findElementByXpath("//*[contains(text(), '" + servicePrefix + "-name')]");

        // The Business Service's attributes should also be displayed
        WebElement el = findElementByXpath("//*[contains(text(), '" + servicePrefix + "-key')]");
        WebElement parent = el.findElement(By.xpath(".."));
        assertEquals(servicePrefix + "-key: " + servicePrefix + "-value", parent.getText());
    }

    /**
     * Creates a BusinessService with a single attribute.
     *
     * The name of the service, and its key-value attributes are all prefixed
     * with the given value.
     */
    private static void createServiceWithPrefix(String prefix) throws ClientProtocolException, IOException {
        String businessServiceApiUrl = BASE_URL + "opennms/api/v2/business-services";

        try (HttpClientWrapper httpClient = HttpClientWrapper.create()) {
            httpClient.addBasicCredentials(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
            httpClient.usePreemptiveAuth();
            HttpPost request = new HttpPost(businessServiceApiUrl);
            request.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());

            StringEntity params = new StringEntity("{" +
                    "\"name\":\"" + prefix + "-name\"," +
                    "\"attributes\":{\"attribute\":[{\"key\":\"" + prefix + "-key\",\"value\":\"" + prefix + "-value\"}]}" +
                    "}");
            request.setEntity(params);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                assertEquals(201, response.getStatusLine().getStatusCode());
            }
        }
    }
}
