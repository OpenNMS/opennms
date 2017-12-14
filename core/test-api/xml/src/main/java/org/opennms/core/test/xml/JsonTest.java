/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
