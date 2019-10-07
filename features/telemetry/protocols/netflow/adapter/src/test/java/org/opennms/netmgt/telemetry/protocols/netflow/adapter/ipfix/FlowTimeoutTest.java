/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.ipfix;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.junit.Test;

public class FlowTimeoutTest {
    @Test
    public void testWithoutTimeout() {
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        bsonDocumentWriter.writeStartDocument();

        bsonDocumentWriter.writeStartDocument("flowStartSeconds");
        bsonDocumentWriter.writeInt64("epoch", 123);
        bsonDocumentWriter.writeEndDocument();

        bsonDocumentWriter.writeStartDocument("flowEndSeconds");
        bsonDocumentWriter.writeInt64("epoch", 987);
        bsonDocumentWriter.writeEndDocument();

        bsonDocumentWriter.writeEndDocument();

        final IpfixFlow flow = new IpfixFlow(bsonDocument);

        assertThat(flow.getTimeout().isPresent(), is(false));

        assertThat(flow.getFirstSwitched(), is(123000L));
        assertThat(flow.getDeltaSwitched(), is(123000L)); // Timeout is same as first
        assertThat(flow.getLastSwitched(), is(987000L));
    }

    @Test
    public void testWithActiveTimeout() {
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        bsonDocumentWriter.writeStartDocument();

        bsonDocumentWriter.writeStartDocument("flowStartSeconds");
        bsonDocumentWriter.writeInt64("epoch", 123);
        bsonDocumentWriter.writeEndDocument();

        bsonDocumentWriter.writeStartDocument("flowEndSeconds");
        bsonDocumentWriter.writeInt64("epoch", 987);
        bsonDocumentWriter.writeEndDocument();

        bsonDocumentWriter.writeInt64("octetDeltaCount", 10);
        bsonDocumentWriter.writeInt64("packetDeltaCount", 10);

        bsonDocumentWriter.writeInt64("flowActiveTimeout", 10);
        bsonDocumentWriter.writeInt64("flowInactiveTimeout", 300);

        bsonDocumentWriter.writeEndDocument();

        final IpfixFlow flow = new IpfixFlow(bsonDocument);

        assertThat(flow.getTimeout().isPresent(), is(true));
        assertThat(flow.getTimeout().get().getActive(), is(10000L));
        assertThat(flow.getTimeout().get().getInactive(), is(300000L));

        assertThat(flow.getFirstSwitched(), is(123000L));
        assertThat(flow.getDeltaSwitched(), is(987000L - 10000L));
        assertThat(flow.getLastSwitched(), is(987000L));
    }

    @Test
    public void testWithInactiveTimeout() {
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        bsonDocumentWriter.writeStartDocument();

        bsonDocumentWriter.writeStartDocument("flowStartSeconds");
        bsonDocumentWriter.writeInt64("epoch", 123);
        bsonDocumentWriter.writeEndDocument();

        bsonDocumentWriter.writeStartDocument("flowEndSeconds");
        bsonDocumentWriter.writeInt64("epoch", 987);
        bsonDocumentWriter.writeEndDocument();

        bsonDocumentWriter.writeInt64("octetDeltaCount", 0);
        bsonDocumentWriter.writeInt64("packetDeltaCount", 0);

        bsonDocumentWriter.writeInt64("flowActiveTimeout", 10);
        bsonDocumentWriter.writeInt64("flowInactiveTimeout", 300);

        bsonDocumentWriter.writeEndDocument();

        final IpfixFlow flow = new IpfixFlow(bsonDocument);

        assertThat(flow.getTimeout().isPresent(), is(true));
        assertThat(flow.getTimeout().get().getActive(), is(10000L));
        assertThat(flow.getTimeout().get().getInactive(), is(300000L));

        assertThat(flow.getFirstSwitched(), is(123000L));
        assertThat(flow.getDeltaSwitched(), is(987000L - 300000L));
        assertThat(flow.getLastSwitched(), is(987000L));
    }
}
