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
package org.opennms.core.profiler;

import static org.opennms.core.profiler.ProfilerAspect.humanReadable;

import org.junit.Assert;
import org.junit.Test;

public class ProfilerAspectTest {

    @Test
    public void testHumanReadable() {
        Assert.assertEquals("0ms", humanReadable(0));
        Assert.assertEquals("10ms", humanReadable(10));
        Assert.assertEquals("1s 0ms", humanReadable(1000));
        Assert.assertEquals("1s 50ms", humanReadable(1050));

        Assert.assertEquals("5m 0s 0ms", humanReadable(5 * 1000 * 60));
        Assert.assertEquals("5m 0s 100ms", humanReadable(5* 1000 * 60 + 100));

        Assert.assertEquals("5m 2s 100ms", humanReadable(5* 1000 * 60 + 2000 + 100));

        Assert.assertEquals("2h 0m 0s 0ms", humanReadable(2 * 60 * 1000 * 60 ));
        Assert.assertEquals("2h 7m 3s 1ms", humanReadable(2 * 60 * 1000 * 60 + 7 * 1000 * 60 + 3000 + 1));

        Assert.assertEquals("13s 106ms", humanReadable(13106));
    }
}
