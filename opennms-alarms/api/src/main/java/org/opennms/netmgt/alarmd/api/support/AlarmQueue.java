/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.api.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.alarmd.api.Preservable;

/**
 * Based on Matt's queue implementation of event forwarding in opennmsd (OVAPI daemon)
 * When in forwarding state, uses Nagle's algorithm to batch up alarms for forwarding by the NBI.
 * 
 * FIXME: Need to make sure the are reasonable defaults in the configuration just-in-case
 * the NBI implementations don't set the batch size, etc. 
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 */
class AlarmQueue<T extends Preservable> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AlarmQueue.class);

    /**
     * The Class State.
     */
    public abstract class State {

        /**
         * Gets the alarms to forward.
         *
         * @return the alarms to forward
         * @throws InterruptedException the interrupted exception
         */
        abstract List<T> getAlarmsToForward() throws InterruptedException;

        /**
         * Forward successful.
         *
         * @param alarms the alarms
         */
        abstract void forwardSuccessful(List<T> alarms);

        /**
         * Forward failed.
         *
         * @param alarms the alarms
         */
        abstract void forwardFailed(List<T> alarms);

        /**
         * Adds the to preserved queue.
         *
         * @param a the alarm
         */
        protected void addToPreservedQueue(T a) {
            if (m_preservedQueue.size() >= m_maxPreservedAlarms) {
                m_nextBatch.clear();
                m_preservedQueue.clear();
                m_preservedQueue.offer(m_statusFactory.createSyncLostMessage());
            }
            m_preservedQueue.offer(a);
        }

        /**
         * Discard non preserved alarms.
         */
        protected void discardNonPreservedAlarms() {
            List<T> alarms = new ArrayList<T>(m_queue.size());
            m_queue.drainTo(alarms);

            addPreservedToPreservedQueue(alarms);
        }

        /**
         * Adds the preserved to preserved queue.
         *
         * @param alarms the alarms
         */
        protected void addPreservedToPreservedQueue(List<T> alarms) {
            for(Iterator<T> it = alarms.iterator(); it.hasNext(); ) {
                T a = it.next();
                if (a.isPreserved()) {
                    addToPreservedQueue(a);
                }
            }
        }

        /**
         * Load next batch.
         */
        protected void loadNextBatch() {
            m_preservedQueue.drainTo(m_nextBatch, m_maxBatchSize - m_nextBatch.size());
        }

    }

    /** The forwarding. */
    private final State FORWARDING = new State() {

        @Override
        public List<T> getAlarmsToForward() throws InterruptedException {
            List<T> alarms = new ArrayList<T>(m_maxBatchSize);

            T a = m_queue.take();
            alarms.add(a);

            m_queue.drainTo(alarms, m_maxBatchSize - alarms.size());

            if (m_naglesDelay <= 0) {
                return alarms;
            }

            long now = System.currentTimeMillis();
            long expirationTime = now + m_naglesDelay;
            while (alarms.size() < m_maxBatchSize && now < expirationTime) {
                T alarm = m_queue.poll(expirationTime - now, TimeUnit.MILLISECONDS);

                if (alarm != null) {
                    alarms.add(alarm);
                    m_queue.drainTo(alarms, m_maxBatchSize - alarms.size());
                }

                now = System.currentTimeMillis();
            }

            return alarms;
        }

        @Override
        public void forwardSuccessful(List<T> alarms) {
            // no need to do anything here
        }

        @Override
        public void forwardFailed(List<T> alarms) {
            addPreservedToPreservedQueue(alarms);

            if (!m_preservedQueue.isEmpty()) {
                setState(FAILING);
            }
        }

        @Override
        public String toString() { return "FORWARDING"; }

    };

    /** The failing. */
    private final State FAILING = new State() {

        @Override
        public List<T> getAlarmsToForward() {
            discardNonPreservedAlarms();
            loadNextBatch();

            //Matt, why are we returning a field?
            return m_nextBatch;
        }

        @Override
        public void forwardFailed(List<T> alarms) {
            // do nothing we are already failing
        }

        @Override
        public void forwardSuccessful(List<T> alarms) {
            m_nextBatch.clear();
            if (m_preservedQueue.isEmpty()) {
                setState(FORWARDING);
            } else {
                setState(RECOVERING);
            }
        }

        @Override
        public String toString() { return "FAILING"; }

    };

    /** The recovering. */
    private final State RECOVERING = new State() {

        @Override
        public List<T> getAlarmsToForward() {
            loadNextBatch();
            return m_nextBatch;
        }

        @Override
        public void forwardFailed(List<T> alarms) {
            setState(FAILING);
        }

        @Override
        public void forwardSuccessful(List<T> alarms) {
            m_nextBatch.clear();
            if (m_preservedQueue.isEmpty()) {
                setState(FORWARDING);
            }
        }

        @Override
        public String toString() {
            return "RECOVERING"; 
        }

    };

    /** The max preserved alarms. */
    // operational parameters
    private int m_maxPreservedAlarms = 300000;

    /** The max batch size. */
    private int m_maxBatchSize = 100;

    /** The nagles delay. */
    private long m_naglesDelay = 1000;

    /** The queue. */
    // queue for all alarms to be forwarded
    private BlockingQueue<T> m_queue = new LinkedBlockingQueue<>();

    /** The preserved queue. */
    // queue for preserving alarms that are being saved during a forwarding failure
    private BlockingQueue<T> m_preservedQueue = new LinkedBlockingQueue<>();

    /** The next batch. */
    // a list of alarms that are pending due to a forwarding failure
    private List<T> m_nextBatch;

    /** The state. */
    // used to define the behavior of the getNext and forwardSuccessful and forwardFailed
    private State m_state = FORWARDING;

    // creates messages use to indicate that a connection failure has 
    /** The status factory. */
    // occurred or queue has overflowed
    private StatusFactory<T> m_statusFactory;

    /**
     * Instantiates a new alarm queue.
     *
     * @param statusFactory the status factory
     */
    public AlarmQueue(StatusFactory<T> statusFactory) {
        m_statusFactory = statusFactory;
    }


    /**
     * Sets the state.
     *
     * @param state the new state
     */
    private void setState(State state) {
        m_state = state;
        LOG.debug("Setting state of AlarmQueue to {}", m_state);
    }

    /**
     * Gets the nagles delay.
     *
     * @return the nagles delay
     */
    public long getNaglesDelay() {
        return m_naglesDelay;
    }

    /**
     * Sets the nagles delay.
     *
     * @param delay the new nagles delay
     */
    public void setNaglesDelay(long delay) {
        m_naglesDelay = delay;
    }

    /**
     * Gets the max preserved alarms.
     *
     * @return the max preserved alarms
     */
    public int getMaxPreservedAlarms() {
        return m_maxPreservedAlarms;
    }

    /**
     * Sets the max preserved alarms.
     *
     * @param maxPreservedAlarms the new max preserved alarms
     */
    public void setMaxPreservedAlarms(int maxPreservedAlarms) {
        m_maxPreservedAlarms = maxPreservedAlarms;
    }

    /**
     * Gets the max batch size.
     *
     * @return the max batch size
     */
    public int getMaxBatchSize() {
        return m_maxBatchSize;
    }

    /**
     * Sets the max batch size.
     *
     * @param maxBatchSize the new max batch size
     */
    public void setMaxBatchSize(int maxBatchSize) {
        m_maxBatchSize = maxBatchSize;
    }

    /**
     * Initialization.
     */
    public void init() {
        m_nextBatch = new ArrayList<T>(m_maxBatchSize); 
    }

    /**
     * Discard.
     *
     * @param a the alarm
     */
    public void discard(T a) {
        // do nothing
    }

    /**
     * Accept.
     *
     * @param a the alarm
     */
    public void accept(T a) {
        boolean result = m_queue.offer(a);
        LOG.debug("Alarm {} accepted, offered to blocking-queue. Actually queued? {}", a, result);
    }

    /**
     * Preserve.
     *
     * @param a the alarm
     */
    public void preserve(T a) {
        a.setPreserved(true);
        boolean result = m_queue.offer(a);
        LOG.debug("Alarm {} preserved, offered to blocking-queue. Actually queued? {}", a, result);
    }

    /**
     * Gets the alarms to forward.
     *
     * @return the alarms to forward
     * @throws InterruptedException the interrupted exception
     */
    public List<T> getAlarmsToForward() throws InterruptedException {
        return m_state.getAlarmsToForward();
    }

    /**
     * Forward successful.
     *
     * @param alarms the alarms
     */
    public void forwardSuccessful(List<T> alarms) {
        m_state.forwardSuccessful(alarms);
    }

    /**
     * Forward failed.
     *
     * @param alarms the alarms
     */
    public void forwardFailed(List<T> alarms) {
        m_state.forwardFailed(alarms);
    }

}
