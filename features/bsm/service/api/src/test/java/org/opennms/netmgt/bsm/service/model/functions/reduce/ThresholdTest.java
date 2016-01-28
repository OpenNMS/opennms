/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.model.functions.reduce;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.functions.map.Increase;
import org.opennms.netmgt.bsm.service.model.mapreduce.MapFunction;

public class ThresholdTest {

    private static class TestEdge implements Edge {

        private final int weight;
        private final MapFunction mapFunction;

        private TestEdge(int weight, MapFunction mapFunction) {
            this.weight = weight;
            this.mapFunction = mapFunction;
        }

        @Override
        public Long getId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Type getType() {
            return Type.IP_SERVICE;
        }

        @Override
        public MapFunction getMapFunction() {
            return mapFunction;
        }

        @Override
        public int getWeight() {
            return weight;
        }

        @Override
        public Set<String> getReductionKeys() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BusinessService getSource() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Status getOperationalStatus() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMapFunction(MapFunction mapFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSource(BusinessService source) {
            throw new UnsupportedOperationException();
        }
    }

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
