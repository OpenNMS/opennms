/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
 ******************************************************************************/

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.config.util.json.YesNoDeserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class YesNoDeserializerTest {
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDeserialize() throws IOException {
        YesNoDeserializer deserializer = new YesNoDeserializer();

        Assert.assertTrue(deserializer.deserialize(genToken("\"true\""), null));
        Assert.assertTrue(deserializer.deserialize(genToken("true"), null));
        Assert.assertTrue(deserializer.deserialize(genToken("\"yes\""), null));
        Assert.assertTrue(deserializer.deserialize(genToken("\"YES\""), null));

        Assert.assertFalse(deserializer.deserialize(genToken("\"false\""), null));
        Assert.assertFalse(deserializer.deserialize(genToken("false"), null));
        Assert.assertFalse(deserializer.deserialize(genToken("\"no\""), null));
        Assert.assertFalse(deserializer.deserialize(genToken("\"NO\""), null));
        Assert.assertFalse(deserializer.deserialize(genToken("\"2342134\""), null));
    }

    private JsonParser genToken(String value) throws IOException {
        String jsonStr = String.format("{\"value\": %s}", value);

        InputStream stream = new ByteArrayInputStream(jsonStr.getBytes(StandardCharsets.UTF_8));
        JsonParser parser = mapper.getFactory().createParser(stream);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING
        return parser;
    }
}
