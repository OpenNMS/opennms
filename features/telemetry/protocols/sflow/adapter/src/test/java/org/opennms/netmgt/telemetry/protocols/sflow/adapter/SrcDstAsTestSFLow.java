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

package org.opennms.netmgt.telemetry.protocols.sflow.adapter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
        final SFlow flow = new SFlow(null, bson);
        assertThat(flow.getSrcAs(), is(equalTo(value)));
        assertThat(flow.getDstAs(), is(equalTo(null)));
    }
}
