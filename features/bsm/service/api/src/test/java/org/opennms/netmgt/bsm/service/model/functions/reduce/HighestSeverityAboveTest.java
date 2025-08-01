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

import java.util.Optional;

import org.junit.Test;
import org.opennms.netmgt.bsm.service.model.Status;

import com.google.common.collect.Lists;

public class HighestSeverityAboveTest {

    @Test
    public void testReduce() {
        HighestSeverityAbove reduceFunction = new HighestSeverityAbove();
        reduceFunction.setThreshold(Status.MAJOR);

        assertEquals(Optional.empty(), reduceFunction.reduce(Lists.newArrayList()));
        assertEquals(Optional.empty(), reduceFunction.reduce(StatusUtils.toListWithIndices(Lists.newArrayList(
                Status.MINOR, Status.MAJOR, Status.WARNING))));

        assertEquals(Status.CRITICAL, reduceFunction.reduce(StatusUtils.toListWithIndices(Lists.newArrayList(
                Status.MINOR, Status.MAJOR, Status.WARNING, Status.CRITICAL))).get().getStatus());
    }
}
