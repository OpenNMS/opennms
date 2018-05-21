/**
 * Copyright (c) 2013, impossibl.com
 * Copyright (c) 2016, OpenNMS group inc
 * <p>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of impossibl.com nor the names of its contributors may
 * be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.opennms.plugins.com.impossibl.postgres.jdbc;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;

public class TimestampUtilsTest {

    @Test
    public void shouldAcceptDateTimeStringWithTAsDateTimeSeparator() throws SQLException {
        // see https://issues.opennms.org/browse/HZN-1282
        String dateTime = "2018-03-22T15:51:01.384-05:00";
        Timestamp timestamp = new TimestampUtils().toTimestamp(null, dateTime);
        ZonedDateTime expectedDateTime =
                ZonedDateTime.of(2018, Month.MARCH.getValue(), 22, 15, 51, 1,
                        384000000, ZoneId.of("UTC-5"));
        assertEquals(expectedDateTime.toInstant().toEpochMilli(), timestamp.getTime());
    }

    @Test
    public void shouldAcceptDateTimeStringWithBlankAsDateTimeSeparator() throws SQLException {
        String dateTime = "2018-03-22 15:51:01.384-05:00";
        Timestamp timestamp = new TimestampUtils().toTimestamp(null, dateTime);
        ZonedDateTime expectedDateTime =
                ZonedDateTime.of(2018, Month.MARCH.getValue(), 22, 15, 51, 1,
                        384000000, ZoneId.of("UTC-5"));
        assertEquals(expectedDateTime.toInstant().toEpochMilli(), timestamp.getTime());
    }
}