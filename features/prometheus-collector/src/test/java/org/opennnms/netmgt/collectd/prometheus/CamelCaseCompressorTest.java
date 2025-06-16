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
package org.opennnms.netmgt.collectd.prometheus;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.opennms.netmgt.collectd.prometheus.CamelCaseCompressor.compress;

import org.junit.Test;

public class CamelCaseCompressorTest {

    @Test
    public void canCompressTrivialInputs() {
        assertThat(compress(null,0), equalTo(null));
        assertThat(compress("",0), equalTo(""));
        assertThat(compress("a",0), equalTo(""));
        assertThat(compress("abc",0), equalTo(""));
    }

    @Test
    public void canCompressCamelCase() {
        assertThat(compress("numBytesInPerSecond",19), equalTo("numBytesInPerSecond"));
        assertThat(compress("numBuffersInRemotePerSecond",19), equalTo("numBuffInRemoPerSec"));
        assertThat(compress("currentInputWatermark",19), equalTo("currentInputWaterma"));
        assertThat(compress("lastCheckpointAlignmentBuffered",19), equalTo("lastCheckAlignBuffe"));
        assertThat(compress("GarbageCollector_PS_Scavenge_Time",19), equalTo("GarbCol_PS_Sca_Time"));
        assertThat(compress("Memory_Direct_TotalCapacity",19), equalTo("Memor_Dire_TotaCapa"));
    }

}
