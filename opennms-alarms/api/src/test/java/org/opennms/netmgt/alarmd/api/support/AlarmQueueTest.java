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
package org.opennms.netmgt.alarmd.api.support;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.support.AlarmQueue;
import org.opennms.netmgt.model.OnmsAlarm;

/**
 * Tests the AlarmQueue for NBI.
 *
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class AlarmQueueTest implements StatusFactory<NorthboundAlarm>{

    /** The alarm number. */
    public int alarmNumber = 0;

    /**
     * Creates the alarm.
     *
     * @return the northbound alarm
     */
    private NorthboundAlarm createAlarm() {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(++alarmNumber);
        alarm.setUei("uei.opennms.org/test/httpNorthBounder");

        return new NorthboundAlarm(alarm);
    }

    /**
     * Test regular forwarding.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void testRegularForwarding() throws InterruptedException {
        AlarmQueue<NorthboundAlarm> queue = new AlarmQueue<NorthboundAlarm>(this);
        queue.setMaxBatchSize(3);
        queue.init();

        queue.accept(createAlarm());
        queue.preserve(createAlarm());
        queue.accept(createAlarm());

        List<NorthboundAlarm> alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(3, alarms.size());

        queue.forwardSuccessful(alarms);

        queue.preserve(createAlarm());
        queue.accept(createAlarm());
        queue.preserve(createAlarm());
        queue.accept(createAlarm());

        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(3, alarms.size());

        queue.forwardSuccessful(alarms);

        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(1, alarms.size());

        queue.forwardSuccessful(alarms);
    }

    /**
     * Test failure.
     *
     * @throws InterruptedException the interrupted exception
     */
    public void testFailure() throws InterruptedException {
        AlarmQueue<NorthboundAlarm> queue = new AlarmQueue<NorthboundAlarm>(this);
        queue.setMaxBatchSize(3);
        queue.init();

        queue.accept(createAlarm());  // 1
        queue.preserve(createAlarm()); // 2
        queue.accept(createAlarm());  // 3

        List<NorthboundAlarm> alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(3, alarms.size());

        queue.forwardSuccessful(alarms);

        queue.preserve(createAlarm()); // 4
        queue.accept(createAlarm());  // 5
        queue.preserve(createAlarm()); // 6
        queue.accept(createAlarm());  // 7

        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(3, alarms.size());

        queue.forwardFailed(alarms);

        queue.accept(createAlarm()); // 8

        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(2, alarms.size());
        assertPreservedAlarm(alarms, 0, 4);
        assertPreservedAlarm(alarms, 1, 6);

        queue.forwardFailed(alarms);

        queue.preserve(createAlarm()); // 9
        queue.accept(createAlarm()); // 10
        queue.preserve(createAlarm()); // 11
        queue.accept(createAlarm()); // 12

        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(3, alarms.size());
        assertPreservedAlarm(alarms, 0, 4);
        assertPreservedAlarm(alarms, 1, 6);
        assertPreservedAlarm(alarms, 2, 9);

        queue.forwardSuccessful(alarms);

        queue.preserve(createAlarm()); // 13
        queue.accept(createAlarm()); // 14

        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(1, alarms.size());
        assertPreservedAlarm(alarms, 0, 11);

        queue.forwardSuccessful(alarms);

        alarms = queue.getAlarmsToForward();
        assertNotNull(alarms);
        assertEquals(2, alarms.size());
        assertPreservedAlarm(alarms, 0, 13);
        assertEquals(14, alarms.get(1).getId().intValue());

        queue.forwardSuccessful(alarms);
    }

    /**
     * Assert preserved alarm.
     *
     * @param alarms the alarms
     * @param index the index
     * @param id the id
     */
    private void assertPreservedAlarm(List<NorthboundAlarm> alarms, int index, int id) {
        assertTrue(alarms.get(index).isPreserved());
        assertEquals(id, alarms.get(index).getId().intValue());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.support.StatusFactory#createSyncLostMessage()
     */
    @Override
    public NorthboundAlarm createSyncLostMessage() {
        return NorthboundAlarm.SYNC_LOST_ALARM;
    }

}
