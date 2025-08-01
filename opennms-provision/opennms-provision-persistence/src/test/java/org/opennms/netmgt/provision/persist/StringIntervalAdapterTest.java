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
package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;

import org.joda.time.Duration;
import org.junit.Test;

public class StringIntervalAdapterTest {
    final StringIntervalAdapter stringIntervalAdapter = new StringIntervalAdapter();
    final static long ONE_DAY_MS = 1000 * 3600 * 24;
    final static long ONE_SECOND_MS = 1000;

    @Test
    public void marshalTest() {
        assertEquals("0", stringIntervalAdapter.marshal(Duration.ZERO));
        assertEquals("-1s", stringIntervalAdapter.marshal(Duration.ZERO.minus(ONE_SECOND_MS)));
        assertEquals("1d", stringIntervalAdapter.marshal(Duration.ZERO.plus(ONE_DAY_MS)));
    }

    @Test
    public void unmarshalTest() {
        assertEquals(Duration.ZERO, stringIntervalAdapter.unmarshal("0"));
        assertEquals(Duration.ZERO, stringIntervalAdapter.unmarshal(" 0 "));
        assertEquals(Duration.ZERO.minus(ONE_SECOND_MS), stringIntervalAdapter.unmarshal("-1s"));
        assertEquals(Duration.ZERO.minus(ONE_SECOND_MS), stringIntervalAdapter.unmarshal(" -1s "));
        assertEquals(Duration.ZERO.plus(ONE_DAY_MS), stringIntervalAdapter.unmarshal("1d"));
        assertEquals(Duration.ZERO.plus(ONE_DAY_MS), stringIntervalAdapter.unmarshal(" 1d "));
        // see NMS-15768
        assertEquals(Duration.ZERO.minus(ONE_SECOND_MS), stringIntervalAdapter.unmarshal("-1"));
        assertEquals(Duration.ZERO.minus(ONE_SECOND_MS), stringIntervalAdapter.unmarshal(" -1 "));
    }
}
