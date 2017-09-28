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

package org.opennms.features.topology.netutils.internal.service;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Preconditions;

public class PingResultTest {

    @Test
    public void testPingResult() {
        PingRequest request = new PingRequest()
                .withNumberRequests(4)
                .withPackageSize(64);

        // Verify if all OK
        PingResult result = createResult(request, 0, 0);
        Assert.assertEquals(Boolean.TRUE, result.isComplete());
        Assert.assertEquals(0, result.getSummary().getPacketLoss(), 0);

        // Verify if 1 ERROR
        result = createResult(request, 1, 0);
        Assert.assertEquals(Boolean.TRUE, result.isComplete());
        Assert.assertEquals((double) 1 / (double) 4, result.getSummary().getPacketLoss(), 0);

        // Verify if 1 Timeout
        result = createResult(request, 0, 1);
        Assert.assertEquals(Boolean.TRUE, result.isComplete());
        Assert.assertEquals((double) 1/ (double) 4, result.getSummary().getPacketLoss(), 0);

        // Verify if 1 Timeout and 1 Error
        result = createResult(request, 1, 1);
        Assert.assertEquals(Boolean.TRUE, result.isComplete());
        Assert.assertEquals(0.5, result.getSummary().getPacketLoss(), 0);
    }

    private PingResult createResult(PingRequest request, int numberErrors, int numberTimeouts) {
        Preconditions.checkArgument(numberErrors + numberTimeouts < request.getNumberRequests(), "numberErrors + numberTimeouts must not be greater than numberRequests");
        Preconditions.checkArgument(numberErrors >= 0, "numberErrors must be >= 0");
        Preconditions.checkArgument(numberTimeouts >= 0, "numberTimeouts must be >= 0");

        final PingResult result = new PingResult(request);

        for (int i=0; i<request.getNumberRequests(); i++) {
            Assert.assertEquals(Boolean.FALSE, result.isComplete());
            DummyEchoPacket packet = new DummyEchoPacket(i, i, System.currentTimeMillis() + 100, System.currentTimeMillis());
            if (numberErrors > 0) {
                result.addSequence(new PingSequence(packet, new Exception()));
                numberErrors--;
            } else if (numberTimeouts > 0) {
                result.addSequence(new PingSequence(packet, true));
                numberTimeouts--;
            } else {
                result.addSequence(new PingSequence(packet));
            }
        }
        return result;
    }
}
