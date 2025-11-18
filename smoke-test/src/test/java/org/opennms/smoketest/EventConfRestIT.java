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

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.RestClient;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.preemptive;
import static org.junit.Assert.assertEquals;

public class EventConfRestIT {

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINIMAL;

    private RestClient restClient;


    @Before
    public void setUp() {
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms";
        RestAssured.authentication = preemptive()
                .basic(AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME,
                        AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD);
        restClient = stack.opennms().getRestClient();
    }

    @Test
    public void testUploadAllEventConfFilesAtOnce() throws IOException {
        File eventsDir = new File("../opennms-base-assembly/src/main/filtered/etc/examples/events");

        if (!eventsDir.exists() || !eventsDir.isDirectory()) {
            throw new IllegalStateException("Events directory not found: " + eventsDir.getAbsolutePath());
        }

        File[] eventFiles = eventsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
        if (eventFiles == null || eventFiles.length == 0) {
            throw new IllegalStateException("No XML event files found in: " + eventsDir.getAbsolutePath());
        }

        // "eventconf/upload" is API path
        Response response = restClient.uploadFiles("upload",new String[]{"eventconf", "upload"},eventFiles);

        assertEquals(200, response.getStatus());
        String jsonResponse = response.readEntity(String.class);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(jsonResponse, Map.class);

        List<Map<String, Object>> successList = (List<Map<String, Object>>) responseMap.get("success");
        int successCount = successList != null ? successList.size() : 0;

        assertEquals("Mismatch in successfully uploaded file count!", eventFiles.length, successCount);
    }
}