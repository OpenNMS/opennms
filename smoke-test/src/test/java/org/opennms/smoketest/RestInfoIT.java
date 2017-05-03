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

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.elasticsearch.common.base.Strings;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        //  {"packageDescription":"OpenNMS","displayVersion":"20.0.0-SNAPSHOT","packageName":"opennms","version":"20.0.0"}
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, String> infoMap = mapper.readValue(json, new TypeReference<Map<String, String>>(){});

        // Verify that some value is present for each of the known keys
        for (String key : Arrays.asList("packageDescription", "displayVersion", "packageName", "version")) {
            assertTrue(String.format("Expected value for key '%s', but none was found. Info returned: %s", key, json),
                    !Strings.isNullOrEmpty(infoMap.get(key)));
        }
    }
}
