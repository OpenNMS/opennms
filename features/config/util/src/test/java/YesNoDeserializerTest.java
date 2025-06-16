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
