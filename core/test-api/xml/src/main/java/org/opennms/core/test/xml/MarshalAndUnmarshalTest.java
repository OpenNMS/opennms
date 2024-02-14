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

import java.io.IOException;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class MarshalAndUnmarshalTest<T> {

    private final T object;
    private final Class<T> type;
    private final String expectedJson;
    private final String expectedXml;

    protected MarshalAndUnmarshalTest(Class<T> type, T object, String expectedJson, String expectedXml) {
        this.object = Objects.requireNonNull(object);
        this.type = Objects.requireNonNull(type);
        this.expectedJson = Objects.requireNonNull(expectedJson);
        this.expectedXml = Objects.requireNonNull(expectedXml);
    }

    @Test
    public void testMarshalAndUnmarshalJson() throws IOException {
        // serialize object
        String jsonString = JsonTest.marshalToJson(object);
        JsonTest.assertJsonEquals(expectedJson, jsonString);

        // deserialize object
        T deserializedObject = JsonTest.unmarshalFromJson(jsonString, type);
        Assert.assertEquals(object, deserializedObject);
    }

    @Test
    public void testMarshalAndUnmarshalXml() {
        // serialize object
        final String xmlString = XmlTest.marshalToXmlWithJaxb(object);
        XmlTest.assertXmlEquals(expectedXml, xmlString);

        // deserialize object
        T unmarshalled = XmlTest.unmarshalFromXmlWithJaxb(xmlString, type);
        Assert.assertEquals(object, unmarshalled);
    }

}
