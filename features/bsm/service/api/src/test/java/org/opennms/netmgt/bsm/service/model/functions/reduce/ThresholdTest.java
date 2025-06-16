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
package org.opennms.netmgt.bsm.service.model.functions.reduce;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.opennms.netmgt.bsm.service.model.Status;

public class ThresholdTest {

    @Test
    public void verifyReduce() {
        // Example from http://www.opennms.org/wiki/BusinessServiceMonitoring
        assertEquals(Status.MAJOR, applyThreshold(0.75f, Status.MAJOR, Status.MAJOR, Status.CRITICAL, Status.CRITICAL, Status.WARNING));

        // Another Example with higher threshold
        assertEquals(Status.WARNING, applyThreshold(1.0f, Status.MAJOR, Status.MAJOR, Status.CRITICAL, Status.CRITICAL, Status.WARNING));

        // Another Example
        assertEquals(Status.MINOR, applyThreshold(1.0f, Status.CRITICAL, Status.MINOR));
    }

    private Status applyThreshold(float threshold, Status...statuses) {
        Threshold t = new Threshold();
        t.setThreshold(threshold);
        return t.reduce(StatusUtils.toListWithIndices(Arrays.asList(statuses))).get().getStatus();
    }
}
