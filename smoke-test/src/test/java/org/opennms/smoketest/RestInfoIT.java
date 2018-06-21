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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

/**
 * Used to validate the /rest/info endpoint
 * 
 * @author jwhite
 */
public class RestInfoIT extends OpenNMSSeleniumTestCase {

    /**
     * Verifies that each of the known keys in the output contains some value.
     *
     * See NMS-9103.
     */
    @Test
    public void canRetrieveProductInfo() throws ClientProtocolException, IOException, InterruptedException {
        // Retrieve the info summary
        final ResponseData response = getRequest(new HttpGet(getBaseUrl() + "/opennms/rest/info"));
        final String json = response.getResponseText();

        // The expected payload looks like:
        //  {"packageDescription":"OpenNMS","displayVersion":"22.0.1","packageName":"opennms","version":"22.0.1", "ticketerConfig":{"enabled":false, "plugin": null}}
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode infoObject = mapper.readTree(json);

        // Verify that some value is present for each of the known keys
        for (String key : Arrays.asList("packageDescription", "displayVersion", "packageName", "version")) {
            assertTrue(String.format("Expected value for key '%s', but none was found. Info returned: %s", key, json),
                    !Strings.isNullOrEmpty(infoObject.get(key).asText()));
        }
        assertNotNull(infoObject.get("ticketerConfig"));
    }
}
