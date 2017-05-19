/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
