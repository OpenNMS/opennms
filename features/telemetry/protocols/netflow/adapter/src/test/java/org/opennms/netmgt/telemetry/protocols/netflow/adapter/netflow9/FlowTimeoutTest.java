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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow9;

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

        bsonDocumentWriter.writeInt64("@unixSecs", 0);
        bsonDocumentWriter.writeInt64("@sysUpTime", 0);

        bsonDocumentWriter.writeInt64("FIRST_SWITCHED", 123000);
        bsonDocumentWriter.writeInt64("LAST_SWITCHED", 987000);

        bsonDocumentWriter.writeEndDocument();

        final Netflow9Flow flow = new Netflow9Flow(bsonDocument);

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

        bsonDocumentWriter.writeInt64("@unixSecs", 0);
        bsonDocumentWriter.writeInt64("@sysUpTime", 0);

        bsonDocumentWriter.writeInt64("FIRST_SWITCHED", 123000);
        bsonDocumentWriter.writeInt64("LAST_SWITCHED", 987000);

        bsonDocumentWriter.writeInt64("IN_BYTES", 10);
        bsonDocumentWriter.writeInt64("IN_PKTS", 10);

        bsonDocumentWriter.writeInt64("FLOW_ACTIVE_TIMEOUT", 10);
        bsonDocumentWriter.writeInt64("FLOW_INACTIVE_TIMEOUT", 300);

        bsonDocumentWriter.writeEndDocument();

        final Netflow9Flow flow = new Netflow9Flow(bsonDocument);

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

        bsonDocumentWriter.writeInt64("@unixSecs", 0);
        bsonDocumentWriter.writeInt64("@sysUpTime", 0);

        bsonDocumentWriter.writeInt64("FIRST_SWITCHED", 123000);
        bsonDocumentWriter.writeInt64("LAST_SWITCHED", 987000);

        bsonDocumentWriter.writeInt64("IN_BYTES", 0);
        bsonDocumentWriter.writeInt64("IN_PKTS", 0);

        bsonDocumentWriter.writeInt64("FLOW_ACTIVE_TIMEOUT", 10);
        bsonDocumentWriter.writeInt64("FLOW_INACTIVE_TIMEOUT", 300);

        bsonDocumentWriter.writeEndDocument();

        final Netflow9Flow flow = new Netflow9Flow(bsonDocument);

        assertThat(flow.getTimeout().isPresent(), is(true));
        assertThat(flow.getTimeout().get().getActive(), is(10000L));
        assertThat(flow.getTimeout().get().getInactive(), is(300000L));

        assertThat(flow.getFirstSwitched(), is(123000L));
        assertThat(flow.getDeltaSwitched(), is(987000L - 300000L));
        assertThat(flow.getLastSwitched(), is(987000L));
    }
}
