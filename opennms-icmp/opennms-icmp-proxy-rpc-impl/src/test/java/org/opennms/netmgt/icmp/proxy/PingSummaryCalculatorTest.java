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
