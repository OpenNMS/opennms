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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.opennms.netmgt.model.OnmsAlarm;

/**
 * Tests NBI Supporting abstract class.
 *
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class AbstractNorthbounderTest {

    /**
     * The Class TestNorthbounder.
     */
    public static class TestNorthbounder extends AbstractNorthbounder {

        /** The alarms. */
        private List<NorthboundAlarm> m_alarms;

        /** The accepting flag. */
        private boolean m_accepting;

        /** The forward alarms called. */
        private CountDownLatch m_forwardAlarmsCalled = new CountDownLatch(1);

        /** The accepts called. */
        private CountDownLatch m_acceptsCalled = new CountDownLatch(1);

        /**
         * Instantiates a new test northbounder.
         */
        public TestNorthbounder() {
            super("TestNorthbounder");
        }

        /* (non-Javadoc)
         * @see org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder#accepts(org.opennms.netmgt.alarmd.api.NorthboundAlarm)
         */
        @Override
        protected boolean accepts(NorthboundAlarm alarm) {
            m_acceptsCalled.countDown();
            return m_accepting;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        /* (non-Javadoc)
         * @see org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder#forwardAlarms(java.util.List)
         */
        @Override
        public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
            m_alarms = alarms;
            m_forwardAlarmsCalled.countDown();
        }

        /**
         * Wait for forward to be called.
         *
         * @param waitTime the wait time
         * @throws InterruptedException the interrupted exception
         */
        public void waitForForwardToBeCalled(long waitTime) throws InterruptedException {
            m_forwardAlarmsCalled.await(waitTime, TimeUnit.MILLISECONDS);
        }

        /**
         * Wait for accepts to be called.
         *
         * @param waitTime the wait time
         * @throws InterruptedException the interrupted exception
         */
        public void waitForAcceptsToBeCalled(long waitTime) throws InterruptedException {
            m_acceptsCalled.await(waitTime, TimeUnit.MILLISECONDS);
        }

        /**
         * Gets the alarms.
         *
         * @return the alarms
         */
        public List<NorthboundAlarm> getAlarms() {
            return m_alarms;
        }

        /**
         * Checks if is accepting.
         *
         * @return true, if is accepting
         */
        public boolean isAccepting() {
            return m_accepting;
        }

        /**
         * Sets the accepting.
         *
         * @param accepting the new accepting
         */
        public void setAccepting(boolean accepting) {
            m_accepting = accepting;
        }

    }

    /**
     * Test alarm forwarding.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void testAlarmForwarding() throws InterruptedException {
        TestNorthbounder tnb = new TestNorthbounder();
        tnb.setAccepting(true);

        tnb.start();

        NorthboundAlarm a = createNorthboundAlarm(1);

        tnb.onAlarm(a);

        tnb.waitForAcceptsToBeCalled(2000);
        tnb.waitForForwardToBeCalled(2000);

        assertNotNull(tnb.getAlarms());
        assertTrue(tnb.getAlarms().contains(a));
    }

    /**
     * Test alarm not accepted.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void testAlarmNotAccepted() throws InterruptedException {
        TestNorthbounder tnb = new TestNorthbounder();
        tnb.setAccepting(false);

        tnb.start();

        tnb.onAlarm(createNorthboundAlarm(1));

        tnb.waitForAcceptsToBeCalled(2000);

        Thread.sleep(100);

        assertNull(tnb.getAlarms());
    }

    /**
     * Test alarm forwarding with nagles.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void testAlarmForwardingWithNagles() throws InterruptedException {
        TestNorthbounder tnb = new TestNorthbounder();
        tnb.setAccepting(true);

        tnb.setNaglesDelay(500);
        tnb.start();

        NorthboundAlarm a1 = createNorthboundAlarm(1);
        NorthboundAlarm a2 = createNorthboundAlarm(2);
        NorthboundAlarm a3 = createNorthboundAlarm(3);

        tnb.onAlarm(a1);
        Thread.sleep(100);
        tnb.onAlarm(a2);
        Thread.sleep(100);
        tnb.onAlarm(a3);

        tnb.waitForAcceptsToBeCalled(2000);
        tnb.waitForForwardToBeCalled(2000);

        assertNotNull(tnb.getAlarms());

        assertEquals(3, tnb.getAlarms().size());

        assertTrue(tnb.getAlarms().contains(a1));
        assertTrue(tnb.getAlarms().contains(a2));
        assertTrue(tnb.getAlarms().contains(a3));
    }

    /**
     * Creates the northbound alarm.
     *
     * @param alarmid the alarmid
     * @return the northbound alarm
     */
    private NorthboundAlarm createNorthboundAlarm(int alarmid) {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(alarmid);
        alarm.setUei("uei.opennms.org/test/httpNorthBounder");

        return new NorthboundAlarm(alarm);
    }

}
