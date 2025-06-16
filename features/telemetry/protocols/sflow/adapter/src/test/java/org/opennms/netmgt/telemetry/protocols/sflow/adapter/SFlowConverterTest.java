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
package org.opennms.netmgt.telemetry.protocols.sflow.adapter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.bson.BsonDocument;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.flows.api.Flow;

import com.google.common.io.Files;

public class SFlowConverterTest {
    private BsonDocument bsonDocument;

    @Before
    public void setupBsonDocument() throws Exception {
        final URL resourceURL = getClass().getResource("/sflow.json");
        this.bsonDocument = BsonDocument.parse(Files.toString(new File(resourceURL.getFile()), StandardCharsets.UTF_8));
    }

    @Test
    public void canConvertBsonDocument() throws Exception {
        assertThat(bsonDocument.getDocument("data"), notNullValue());
        assertThat(bsonDocument.getInt64("time"), notNullValue());
        assertThat(bsonDocument.getInt64("time").getValue(), is(1521618510235L));
        assertThat(bsonDocument.getDocument("data").getArray("samples"), notNullValue());
        assertThat(bsonDocument.getDocument("data").getArray("samples").size(), is(7));

        final List<Flow> flows = SFlowAdapter.convertDocument(bsonDocument, Instant.now());

        // There are six flows int the document, but two are skipped, because they don't contain IPv{4,6} information
        assertThat(flows.size(), is(5));
    }

    private boolean compareValues(final List<Flow> flows, final Integer in, final Integer out) {
        for (final Flow flow : flows) {
            if (Objects.equals(flow.getInputSnmp(), in) && Objects.equals(flow.getOutputSnmp(), out)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void checkSnmpInputOutputValues() throws Exception {
        assertThat(bsonDocument.getDocument("data"), notNullValue());
        assertThat(bsonDocument.getInt64("time"), notNullValue());
        assertThat(bsonDocument.getInt64("time").getValue(), is(1521618510235L));
        assertThat(bsonDocument.getDocument("data").getArray("samples"), notNullValue());
        assertThat(bsonDocument.getDocument("data").getArray("samples").size(), is(7));

        final List<Flow> flows = SFlowAdapter.convertDocument(bsonDocument, Instant.now());

        assertThat(compareValues(flows, 4, 17), is(true));
        assertThat(compareValues(flows, 4, 42), is(true));
        assertThat(compareValues(flows, 17, 4), is(true));
        assertThat(compareValues(flows, 5, null), is(true));
        assertThat(compareValues(flows, null, 18), is(true));
        assertThat(flows.size(), is(5));
    }
}
