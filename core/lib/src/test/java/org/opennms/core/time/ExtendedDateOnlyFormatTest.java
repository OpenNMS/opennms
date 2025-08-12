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

package org.opennms.core.time;

import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExtendedDateOnlyFormatTest {
    @Test
    public void shouldOutputDateIncludingTimeZone() throws IOException {
        test("LLLL d, yyyy", Instant.now());
    }

    @Test
    public void shouldBeResilientAgainstNull() throws IOException {
        assertNull(new ExtendedDateOnlyFormat().format((Instant)null, null));
        assertNull(new ExtendedDateOnlyFormat().format((Date)null, null));
    }

    @Test
    public void shouldHonorSystemSettings() throws IOException {
        String format = "yyy-MM-dd";
        System.setProperty(ExtendedDateOnlyFormat.SYSTEM_PROPERTY_UI_DATE_ONLY_FORMAT, format);
        test(format, Instant.now());
        System.clearProperty(ExtendedDateOnlyFormat.SYSTEM_PROPERTY_UI_DATE_ONLY_FORMAT);
    }

    public void test(String expectedPattern, Instant time) {
        String output = new ExtendedDateOnlyFormat().format(time, ZoneId.systemDefault());
        assertEquals(DateTimeFormatter.ofPattern(expectedPattern).withZone(ZoneId.systemDefault()).format(time), output);
    }
}
