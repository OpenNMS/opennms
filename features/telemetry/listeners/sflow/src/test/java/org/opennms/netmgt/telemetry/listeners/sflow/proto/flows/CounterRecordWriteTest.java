/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.BsonWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

@RunWith(Parameterized.class)
public class CounterRecordWriteTest {
    private Record.DataFormat dataFormat;

    @Parameterized.Parameters(name = "dataFormat: {0}")
    public static Collection data() throws IOException {
        return CounterRecord.counterDataFormats.keySet();
    }

    public CounterRecordWriteTest(final Record.DataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    public static ByteBuffer byteBufferForFormat(Record.DataFormat dataFormat) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(1500);
        byte[] dataFormatBytes = dataFormat.toBytes();

        for (int i = 0; i < dataFormatBytes.length; i++) {
            byteBuffer.put(dataFormatBytes[i]);
        }

        // This combination seems to work with all the
        // record types.
        byteBuffer.putInt(768);
        byteBuffer.putInt(2);
        byteBuffer.putInt(1);
        byteBuffer.putInt(0);
        byteBuffer.putInt(16);
        byteBuffer.putInt(1);
        byteBuffer.putInt(1);
        byteBuffer.putInt(1);

        for (int i = 0; i < (1500 - 33) / 4; i++) {
            byteBuffer.putInt(0);
        }

        byteBuffer.rewind();
        return byteBuffer;
    }

    @Test
    public void testBsonWrite() throws InvalidPacketException {
        final CounterRecord counterRecord = new CounterRecord(byteBufferForFormat(this.dataFormat));
        final BsonWriter bsonWriter = new BsonDocumentWriter(new BsonDocument());
        counterRecord.writeBson(bsonWriter);
    }
}
