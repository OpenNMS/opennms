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
package org.opennms.features.jest.client.template;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TemplateMergerTest {
    @Test
    public void verifyEmptyTemplateMergeWithEmptySettings() {
        assertEquals("{}", new TemplateMerger().merge("{}", new IndexSettings()));
        assertEquals(new JsonObject(), new TemplateMerger().merge(new JsonObject(), new IndexSettings()));
    }

    @Test
    public void verifyEmptyTemplateMergeWithNullSettings() {
        assertEquals("{}", new TemplateMerger().merge("{}", null));
        assertEquals(new JsonObject(), new TemplateMerger().merge(new JsonObject(), null));
    }

    @Test
    public void verifyEmptyTemplateMergeWithFullyDefinedSettings() {
        final String expectedJson = "{" +
            "  settings: {" +
            "    index: {" +
            "      number_of_shards: 5," +
            "      number_of_replicas: 10," +
            "      refresh_interval: 10s," +
            "      routing_partition_size: 20" +
            "    }" +
            "  }," +
            "  \"index_patterns\":[\"prefix*\"]" +
            "}";

        // Configure settings
        final IndexSettings settings = new IndexSettings();
        settings.setIndexPrefix("prefix");
        settings.setNumberOfShards(5);
        settings.setNumberOfReplicas(10);
        settings.setRefreshInterval("10s");
        settings.setRoutingPartitionSize(20);

        // Verify
        final JsonElement expectedJsonObject = new JsonParser().parse(expectedJson);
        assertEquals(new Gson().toJson(expectedJsonObject), new TemplateMerger().merge("{}", settings));
        assertEquals(new JsonParser().parse(expectedJson), new TemplateMerger().merge(new JsonObject(), settings));
    }

    @Test
    public void verifyIndexPrefixHandling() {
        final String expectedJson = "{\"index_patterns\":[\"prefix-test-*\"],\"settings\":{\"index\":{}}}";

        // Configure settings
        final IndexSettings settings = new IndexSettings();
        settings.setIndexPrefix("prefix-");

        // Verify
        final JsonElement expectedJsonObject = new JsonParser().parse(expectedJson);
        assertEquals(new Gson().toJson(expectedJsonObject), new TemplateMerger().merge("{\"index_patterns\":[\"test-*\"]}", settings));
    }
}