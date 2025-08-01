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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Instant;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.junit.Test;

public class SrcDstAsTestSFLow {
    private BsonDocument getBsonDocument(final long value) {
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        bsonDocumentWriter.writeStartDocument();
        bsonDocumentWriter.writeStartDocument("flows");
        bsonDocumentWriter.writeStartDocument("0:1003");
        bsonDocumentWriter.writeName("src_as");
        bsonDocumentWriter.writeInt64(value);
        bsonDocumentWriter.writeEndDocument();
        bsonDocumentWriter.writeEndDocument();
        bsonDocumentWriter.writeEndDocument();
        return bsonDocument;
    }

    @Test
    public void testSrcDstAs() {
        long value = 2147483648L;
        final BsonDocument bson = getBsonDocument(value);
        final SFlow flow = new SFlow(null, bson, Instant.now());
        assertThat(flow.getSrcAs(), is(equalTo(value)));
        assertThat(flow.getDstAs(), is(equalTo(null)));
    }
}
