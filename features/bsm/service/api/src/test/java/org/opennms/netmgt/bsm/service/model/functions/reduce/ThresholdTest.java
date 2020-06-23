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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

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
