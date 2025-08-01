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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.StatusWithIndices;

public class ExponentialPropagationTest {

    @Test
    public void testEmpty() {
        reduceAndVerify(Optional.empty(), Collections.emptyList(), 2.0);
    }

    @Test
    public void testSingleInput() {
        reduceAndVerify(Optional.of(Status.NORMAL), Collections.emptyList(),
                2.0, Status.NORMAL);

        reduceAndVerify(Optional.of(Status.WARNING), Arrays.asList(0),
                2.0, Status.WARNING);
    }

    @Test
    public void testIndeterminate() {
        reduceAndVerify(Optional.empty(), Collections.emptyList(),
                2.0, Status.INDETERMINATE, Status.INDETERMINATE, Status.INDETERMINATE);
    }

    @Test
    public void testBaseTwo() {
        reduceAndVerify(Optional.of(Status.NORMAL), Collections.emptyList(),
                2.0, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL);

        reduceAndVerify(Optional.of(Status.WARNING), Arrays.asList(0),
                2.0, Status.WARNING, Status.NORMAL, Status.NORMAL, Status.NORMAL);

        reduceAndVerify(Optional.of(Status.MINOR), Arrays.asList(0, 1),
                2.0, Status.WARNING, Status.WARNING, Status.NORMAL, Status.NORMAL);

        reduceAndVerify(Optional.of(Status.MINOR), Arrays.asList(0, 1, 2),
                2.0, Status.WARNING, Status.WARNING, Status.WARNING, Status.NORMAL);

        reduceAndVerify(Optional.of(Status.MAJOR), Arrays.asList(0, 1, 2, 3),
                2.0, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING);

        reduceAndVerify(Optional.of(Status.MINOR), Arrays.asList(0, 1),
                2.0, Status.MINOR, Status.WARNING, Status.NORMAL);

        reduceAndVerify(Optional.of(Status.MAJOR), Arrays.asList(0, 1, 2),
                2.0, Status.MINOR, Status.WARNING, Status.WARNING);
    }

    @Test
    public void testBaseThree() {
        reduceAndVerify(Optional.of(Status.NORMAL), Collections.emptyList(),
                3.0, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL);

        reduceAndVerify(Optional.of(Status.WARNING), Arrays.asList(0),
                3.0, Status.WARNING, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL);

        reduceAndVerify(Optional.of(Status.MINOR),  Arrays.asList(0, 1, 2),
                3.0, Status.WARNING, Status.WARNING, Status.WARNING, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL);

        reduceAndVerify(Optional.of(Status.MINOR), Arrays.asList(0, 1, 2, 3),
                3.0, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL, Status.NORMAL);

        reduceAndVerify(Optional.of(Status.MAJOR), Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8),
                3.0, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING);

        reduceAndVerify(Optional.of(Status.MINOR), Arrays.asList(0, 1, 2, 3, 4, 5),
                3.0, Status.MINOR, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.NORMAL);

        reduceAndVerify(Optional.of(Status.MAJOR), Arrays.asList(0, 1, 2, 3, 4, 5, 6),
                3.0, Status.MINOR, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING, Status.WARNING);
    }

    private void reduceAndVerify(Optional<Status> expectedStatus, List<Integer> expectedCauseIndices, double base, Status...statuses) {
        // Reduce
        ExponentialPropagation exp = new ExponentialPropagation();
        exp.setBase(base);
        Optional<StatusWithIndices> reduced = exp.reduce(StatusUtils.toListWithIndices(Arrays.asList(statuses)));

        // Verify the resulting status
        assertEquals(expectedStatus, StatusUtils.getStatus(reduced));

        // Verify the cause indices
        if (reduced.isPresent() || expectedCauseIndices.size() > 0) {
            assertEquals(expectedCauseIndices, reduced.get().getIndices());
        }
    }
}
