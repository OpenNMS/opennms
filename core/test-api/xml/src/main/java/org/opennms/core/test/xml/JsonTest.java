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
package org.opennms.core.test.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.junit.Assert;
import org.opennms.core.xml.JacksonUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JsonTest {

    private static final Logger LOG = LoggerFactory.getLogger(JsonTest.class);

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = JacksonUtils.createDefaultObjectMapper();

    private JsonTest(){}

    public static <T> String marshalToJson(T object) throws IOException {
        LOG.debug("Reference Object: {}", object);
        String json = DEFAULT_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        LOG.debug("JSON: {}", json);
        return json;
    }

    public static <T> T unmarshalFromJson(String json, Class<T> expectedResultType) throws IOException {
        return DEFAULT_OBJECT_MAPPER.readValue(json, expectedResultType);
    }

    // reads all data from the given input stream
    public static String read(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            IOUtils.copy(inputStream, output);
            return output.toString();
        }
    }

    public static void assertJsonEquals(String expected, String actual) {
        try {
            JSONAssert.assertEquals(expected, actual, true);
        } catch (AssertionError e) {
            System.out.println("Actual JSON: " + actual);
            System.out.println("Expected JSON: " + expected);
            throw e;
        }  catch (JSONException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
