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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.functions.map.Increase;

public class ThresholdTest {

    @Test
    public void verifyReduce() {
        // Example from http://www.opennms.org/wiki/BusinessServiceMonitoring
        Threshold threshold = new Threshold();
        threshold.setThreshold(0.75f);

        Map<Edge, Status> edgeStatusMap = new HashMap<>();
        edgeStatusMap.put(new TestEdge(2, new Increase()), Status.MAJOR);
        edgeStatusMap.put(new TestEdge(2, new Increase()), Status.CRITICAL);
        edgeStatusMap.put(new TestEdge(1, new Increase()), Status.WARNING);
        Assert.assertEquals(Status.MAJOR, threshold.reduce(edgeStatusMap).get());

        // Another Example with higher threshold
        threshold.setThreshold(1.0f);
        Assert.assertEquals(Status.WARNING, threshold.reduce(edgeStatusMap).get());

        // Another Example
        threshold.setThreshold(1.00f);
        edgeStatusMap.clear();
        edgeStatusMap.put(new TestEdge(1, new Increase()), Status.CRITICAL);
        edgeStatusMap.put(new TestEdge(1, new Increase()), Status.MINOR);
        Assert.assertEquals(Status.MINOR, threshold.reduce(edgeStatusMap).get());
    }
}
