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
