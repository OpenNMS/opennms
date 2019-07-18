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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.junit.Test;

public class SrcDstAsTestV5 {
    private BsonDocument getBsonDocument(final String key1, final long value1, final String key2, final long value2) {
        final BsonDocument bsonDocument = new BsonDocument();
        final BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(bsonDocument);
        bsonDocumentWriter.writeStartDocument();
        bsonDocumentWriter.writeName(key1);
        bsonDocumentWriter.writeInt64(value1);
        bsonDocumentWriter.writeName(key2);
        bsonDocumentWriter.writeInt64(value2);
        bsonDocumentWriter.writeEndDocument();
        return bsonDocument;
    }

    @Test
    public void testSrcDstAs() {
        long value1 = 2147483648L;
        long value2 = 2147483649L;
        final BsonDocument bson = getBsonDocument("srcAs", value1, "dstAs", value2);
        final Netflow5Flow flow = new Netflow5Flow(bson);
        assertThat(flow.getSrcAs(), is(equalTo(value1)));
        assertThat(flow.getDstAs(), is(equalTo(value2)));
    }
}
