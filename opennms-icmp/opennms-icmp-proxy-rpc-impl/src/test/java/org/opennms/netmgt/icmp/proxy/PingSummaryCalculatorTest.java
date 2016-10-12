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

package org.opennms.netmgt.icmp.proxy;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class PingSummaryCalculatorTest {

    @Test
    public void testPacketLoss() {
        // Verify if all OK
        PingSummaryCalculator calculator = createCalculator(4, 0, 0);
        Assert.assertEquals(4, calculator.getPacketsReceived());
        Assert.assertEquals(0, calculator.getPacketLoss(), 0);

        // Verify if 1 ERROR
        calculator = createCalculator(4, 1, 0);
        Assert.assertEquals(3, calculator.getPacketsReceived());
        Assert.assertEquals(0.25, calculator.getPacketLoss(), 0);

        // Verify if 1 Timeout
        calculator = createCalculator(4, 0, 1);
        Assert.assertEquals(3, calculator.getPacketsReceived());
        Assert.assertEquals(0.25, calculator.getPacketLoss(), 0);

        // Verify if 1 Timeout and 1 Error
        calculator = createCalculator(4, 1, 1);
        Assert.assertEquals(2, calculator.getPacketsReceived());
        Assert.assertEquals(0.5, calculator.getPacketLoss(), 0);
    }

    private PingSummaryCalculator createCalculator(int numberRequests, int numberErrors, int numberTimeouts) {
        Preconditions.checkArgument(numberErrors + numberTimeouts < numberRequests, "numberErrors + numberTimeouts must not be greater than numberRequests");
        Preconditions.checkArgument(numberErrors >= 0, "numberErrors must be >= 0");
        Preconditions.checkArgument(numberTimeouts >= 0, "numberTimeouts must be >= 0");

        final List<PingSequence> sequences = Lists.newArrayList();
        for (int i=0; i<numberRequests; i++) {
            if (numberErrors > 0) {
                sequences.add(new PingSequence(i, new Exception()));
                numberErrors--;
            } else if (numberTimeouts > 0) {
                final PingResponse timeoutResponse = new PingResponse();
                timeoutResponse.setRtt(Double.POSITIVE_INFINITY);
                sequences.add(new PingSequence(i, timeoutResponse));
                numberTimeouts--;
            } else {
                final PingResponse pingResponse = new PingResponse();
                pingResponse.setRtt(1000);
                sequences.add(new PingSequence(i, pingResponse));
            }
        }
        Assert.assertEquals(numberRequests, sequences.size());
        return new PingSummaryCalculator(sequences);
    }
}
