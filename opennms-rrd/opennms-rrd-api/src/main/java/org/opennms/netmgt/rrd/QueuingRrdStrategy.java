/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides queuing implementation of RrdStrategy.
 *
 * In order to provide a more scalable collector. We created a queuing
 * RrdStrategy that enabled the system to amortize the high cost of opening an
 * round robin database across multiple updates.
 *
 * This RrdStrategy implementation enqueues the create and update operations on
 * a per file basis and maintains a set of threads that process enqueued work
 * file by file.
 *
 * If the I/O system can keep up with the collection threads while performing
 * only a single update per file then eventually all the data is processed and
 * the threads sleep until there is more work to do.
 *
 * If the I/O system is initially slower than than the collection threads then
 * work will enqueue here and the write threads will get behind. As this happens
 * each file will eventually have more than a single update enqueued and
 * therefore the number of updates pushed thru the system will increase because
 * more then one will be output per 'open' Eventually, the I/O system and the
 * collection system will balance out. When this happens all data will be
 * collected but will not be output to the rrd files until the next time the
 * file is processed by the write threads.
 *
 * As another performance improving strategy. The queue distinguishes between
 * files with significant vs insignificant updates. Files with only insignificant
 * updates are put at the lowest priority and are only written when the highest
 * priority updates have been written
 *
 * This implementation delegates all the actual writing to another RrdStrategy
 * implementation.
 *
 * System properties effecting the operation:
 *
 * org.opennms.rrd.queuing.writethreads: (default 2) The number of rrd write
 * threads that process the queue
 *
 * org.opennms.rrd.queuing.queueCreates: (default false) indicates whether rrd
 * file creates should be queued or processed synchronously
 *
 * org.opennms.rrd.queuing.maxInsigUpdateSeconds: (default 0) the number of
 * seconds over which all files with significant updates only should be promoted
 * onto the significant less. This is to ensure they don't stay unprocessed
 * forever. Zero means not promotion.
 *
 * org.opennms.rrd.queuing.modulus: (default 10000) the number of updates the
 * get enqueued between statistics output
 *
 * org.opennms.rrd.queuing.category: (default "queued") the log routing prefix
 * to place the statistics output in
 *
 *
 *
 * TODO: Promote files when ZeroUpdate operations can't be merged. This may be a
 * collection miss which we want to push thru. It should also help with memory.
 *
 * TODO: Set an upper bound on enqueued operations
 *
 * TODO: Provide an event that will write data for a particular file... Say
 * right before we try to graph it.
 *
 * @author ranger
 * @version $Id: $
 */
public class QueuingRrdStrategy implements RrdStrategy<QueuingRrdStrategy.CreateOperation,String>, Runnable {

    private Logger m_log = LoggerFactory.getLogger(QueuingRrdStrategy.class);

    private Properties m_configurationProperties;

    /**
     * <p>getConfigurationProperties</p>
     *
     * @return a {@link java.util.Properties} object.
     */
    public Properties getConfigurationProperties() {
        return m_configurationProperties;
    }

    /** {@inheritDoc} */
    @Override
    public void setConfigurationProperties(final Properties configurationParameters) {
        m_configurationProperties = configurationParameters;
    }

    RrdStrategy<Object, Object> m_delegate;

    private static final int UPDATE = 0;
    private static final int CREATE = 1;

    private String m_category = "queued";

    private int m_writeThreads = 0;

    private boolean m_queueCreates;

    private boolean m_prioritizeSignificantUpdates;

    private long m_inSigHighWaterMark;

    private long m_sigHighWaterMark;

    private long m_queueHighWaterMark;

    private long m_modulus;

    private long m_maxInsigUpdateSeconds;

    private long m_writeThreadSleepTime;

    private long m_writeThreadExitDelay;

    /**
     * <p>getWriteThreads</p>
     *
     * @return a int.
     */
    public int getWriteThreads() {
        return m_writeThreads;
    }

    /**
     * <p>setWriteThreads</p>
     *
     * @param writeThreads a int.
     */
    public void setWriteThreads(int writeThreads) {
        m_writeThreads = writeThreads;
    }

    /**
     * <p>queueCreates</p>
     *
     * @return a boolean.
     */
    public boolean queueCreates() {
        return m_queueCreates;
    }

    /**
     * <p>setQueueCreates</p>
     *
     * @param queueCreates a boolean.
     */
    public void setQueueCreates(boolean queueCreates) {
        m_queueCreates = queueCreates;
    }

    /**
     * <p>prioritizeSignificantUpdates</p>
     *
     * @return a boolean.
     */
    public boolean prioritizeSignificantUpdates() {
        return m_prioritizeSignificantUpdates;
    }

    /**
     * <p>setPrioritizeSignificantUpdates</p>
     *
     * @param prioritizeSignificantUpdates a boolean.
     */
    public void setPrioritizeSignificantUpdates(boolean prioritizeSignificantUpdates) {
        m_prioritizeSignificantUpdates = prioritizeSignificantUpdates;
    }

    /**
     * <p>getInSigHighWaterMark</p>
     *
     * @return a long.
     */
    public long getInSigHighWaterMark() {
        return m_inSigHighWaterMark;
    }

    /**
     * <p>setInSigHighWaterMark</p>
     *
     * @param inSigHighWaterMark a long.
     */
    public void setInSigHighWaterMark(long inSigHighWaterMark) {
        m_inSigHighWaterMark = inSigHighWaterMark;
    }

    /**
     * <p>getSigHighWaterMark</p>
     *
     * @return a long.
     */
    public long getSigHighWaterMark() {
        return m_sigHighWaterMark;
    }

    /**
     * <p>setSigHighWaterMark</p>
     *
     * @param sigHighWaterMark a long.
     */
    public void setSigHighWaterMark(long sigHighWaterMark) {
        m_sigHighWaterMark = sigHighWaterMark;
    }

    /**
     * <p>getQueueHighWaterMark</p>
     *
     * @return a long.
     */
    public long getQueueHighWaterMark() {
        return m_queueHighWaterMark;
    }

    /**
     * <p>setQueueHighWaterMark</p>
     *
     * @param queueHighWaterMark a long.
     */
    public void setQueueHighWaterMark(long queueHighWaterMark) {
        m_queueHighWaterMark = queueHighWaterMark;
    }

    /**
     * <p>getModulus</p>
     *
     * @return a long.
     */
    public long getModulus() {
        return m_modulus;
    }

    /**
     * <p>setModulus</p>
     *
     * @param modulus a long.
     */
    public void setModulus(long modulus) {
        m_modulus = modulus;
    }

    /**
     * <p>getCategory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCategory() {
        return m_category;
    }

    /**
     * <p>setCategory</p>
     *
     * @param category a {@link java.lang.String} object.
     */
    public void setCategory(final String category) {
        m_category = category;

        m_log = LoggerFactory.getLogger(m_category);
    }

    /**
     * <p>getMaxInsigUpdateSeconds</p>
     *
     * @return a long.
     */
    public long getMaxInsigUpdateSeconds() {
        return m_maxInsigUpdateSeconds;
    }

    /**
     * <p>setMaxInsigUpdateSeconds</p>
     *
     * @param maxInsigUpdateSeconds a long.
     */
    public void setMaxInsigUpdateSeconds(long maxInsigUpdateSeconds) {
        m_maxInsigUpdateSeconds = maxInsigUpdateSeconds;
    }

    /**
     * <p>getWriteThreadSleepTime</p>
     *
     * @return a long.
     */
    public long getWriteThreadSleepTime() {
        return m_writeThreadSleepTime;
    }

    /**
     * <p>setWriteThreadSleepTime</p>
     *
     * @param writeThreadSleepTime a long.
     */
    public void setWriteThreadSleepTime(long writeThreadSleepTime) {
        m_writeThreadSleepTime = writeThreadSleepTime;
    }

    /**
     * <p>getWriteThreadExitDelay</p>
     *
     * @return a long.
     */
    public long getWriteThreadExitDelay() {
        return m_writeThreadExitDelay;
    }

    /**
     * <p>setWriteThreadExitDelay</p>
     *
     * @param writeThreadExitDelay a long.
     */
    public void setWriteThreadExitDelay(long writeThreadExitDelay) {
        m_writeThreadExitDelay = writeThreadExitDelay;
    }

    LinkedList<String> filesWithSignificantWork = new LinkedList<String>();

    LinkedList<String> filesWithInsignificantWork = new LinkedList<String>();

    Map<String, LinkedList<Operation>> pendingFileOperations = new HashMap<String, LinkedList<Operation>>();

    Map<Thread, String> fileAssignments = new HashMap<Thread, String>();

    Set<String> reservedFiles = new HashSet<String>();

    private long m_totalOperationsPending = 0;

    private long m_enqueuedOperations = 0;

    private long m_dequeuedOperations = 0;

    private long m_significantOpsEnqueued = 0;

    private long m_significantOpsDequeued = 0;

    private long m_significantOpsCompleted = 0;

    private long m_dequeuedItems = 0;

    private long m_createsCompleted = 0;

    private long m_updatesCompleted = 0;

    private long m_errors = 0;

    int threadsRunning = 0;

    private long m_startTime = 0;

    private long m_promotionCount = 0;

    long lastLap = System.currentTimeMillis();

    long lastStatsTime = 0;

    long lastEnqueued = 0;

    long lastDequeued = 0;

    long lastSignificantEnqueued = 0;

    long lastSignificantDequeued = 0;

    long lastSignificantCompleted = 0;

    long lastDequeuedItems = 0;

    long lastOpsPending = 0;

    /**
     * This is the base class for an enqueue able operation
     */
    static abstract class Operation {
        final String fileName;
        final int type;
        final Object data;
        final boolean significant;

        Operation(final String fileName, final int type, final Object data, final boolean significant) {
            this.fileName = fileName;
            this.type = type;
            this.data = data;
            this.significant = significant;
        }

        int getCount() {
            return 1;
        }

        String getFileName() {
            return this.fileName;
        }

        int getType() {
            return this.type;
        }

        Object getData() {
            return this.data;
        }

        boolean isSignificant() {
            return significant;
        }

        void addToPendingList(LinkedList<Operation> pendingOperations) {
            pendingOperations.add(this);
        }

        abstract Object process(Object rrd) throws Exception;

    }

    /**
     * This class represents an operation to create an rrd file
     */
    public class CreateOperation extends Operation {

        CreateOperation(String fileName, Object rrdDef) {
            super(fileName, CREATE, rrdDef, true);
        }

        @Override
        Object process(Object rrd) throws Exception {
            // if the rrd is already open we are confused
            if (rrd != null) {
                m_log.debug("WHAT! rrd open but not created?");
                m_delegate.closeFile(rrd);
                rrd = null;
            }

            // create the file
            m_delegate.createFile(getData());

            // keep stats
            setCreatesCompleted(getCreatesCompleted() + 1);

            // return the file
            return rrd;

        }

    }

    /**
     * Represents an update to a rrd file.
     */
    public class UpdateOperation extends Operation {

        UpdateOperation(String fileName, String data) {
            super(fileName, UPDATE, data, true);
        }

        UpdateOperation(String fileName, String data, boolean significant) {
            super(fileName, UPDATE, data, significant);
        }

        @Override
        Object process(Object rrd) throws Exception {
            // open the file if we need to
            if (rrd == null) rrd = m_delegate.openFile(getFileName());

            final String update = (String) getData();

            try {
                // process the update
                m_delegate.updateFile(rrd, "", update);
            } catch (final Throwable e) {
                final String error = String.format("Error processing update for file %s: %s", getFileName(), update);
                m_log.debug(error, e);
                throw new Exception(error, e);
            }

            // keep stats
            setUpdatesCompleted(getUpdatesCompleted() + 1);
            if (getUpdatesCompleted() % m_modulus == 0) {
                logStats();
            }
            // return the open rrd for further processing
            return rrd;

        }

    }

    /**
     * Represents an update whose value is 0. These operations can be merged
     * together and take up less memory
     */
    public class ZeroUpdateOperation extends UpdateOperation {

        long timeStamp;

        long interval = 0;

        int count;

        ZeroUpdateOperation(String fileName, long intitialTimeStamp) {
            super(fileName, "0", false);
            timeStamp = intitialTimeStamp;
            count = 1;
        }

        @Override
        Object process(Object rrd) throws Exception {
            long ts = getFirstTimeStamp();
            for (int i = 0; i < count; i++) {
                // open the file if we need to
                if (rrd == null)
                    rrd = m_delegate.openFile(getFileName());

                String update = ts + ":0";
                try {
                    // process the update
                    m_delegate.updateFile(rrd, "", update);
                } catch (Throwable e) {
                    throw new Exception("Error processing update " + i + " for file " + getFileName() + ": " + update, e);
                }
                ts += getInterval();

                // keep stats
                setUpdatesCompleted(getUpdatesCompleted() + 1);
                if (getUpdatesCompleted() % m_modulus == 0) {
                    logStats();
                }
            }
            return rrd;
        }

        @Override
        public int getCount() {
            return count;
        }

        public void setCount(int newCount) {
            this.count = newCount;
        }

        public long getFirstTimeStamp() {
            return timeStamp;
        }

        public long getLastTimeStamp() {
            return timeStamp + interval * (count - 1);
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(long newInterval) {
            interval = newInterval;
        }

        public void mergeUpdates(ZeroUpdateOperation op) throws IllegalArgumentException {
            long opSpacing = op.getFirstTimeStamp() - getLastTimeStamp();
            long tolerance = getInterval() / 5;

            if (opSpacing == 0) {
                throw new IllegalArgumentException("unable to merge op because the spacing " + opSpacing + " is 0");

            }
            if (getInterval() > 0 && Math.abs(opSpacing - getInterval()) >= tolerance) {
                throw new IllegalArgumentException("unable to merge op because the spacing " + opSpacing + " is different than the current interval " + getInterval());
            }
            if (getInterval() > 0 && op.getInterval() > 0 && Math.abs(op.getInterval() - getInterval()) >= tolerance) {
                throw new IllegalArgumentException("unable to merge op because the new op interval " + op.getInterval() + " is different than the current interval " + getInterval());
            }

            int newCount = getCount() + op.getCount();
            long newInterval = ((getCount() - 1) * getInterval() + (op.getCount() - 1) + op.getInterval() + opSpacing) / (newCount - 1);

            setCount(newCount);
            setInterval(newInterval);

        }

        @Override
        void addToPendingList(LinkedList<Operation> pendingOperations) {
            if (pendingOperations.size() > 0 && pendingOperations.getLast() instanceof ZeroUpdateOperation) {
                ZeroUpdateOperation zeroOp = (ZeroUpdateOperation) pendingOperations.getLast();
                try {
                    zeroOp.mergeUpdates(this);
                } catch (IllegalArgumentException e) {
                    m_log.debug("Unable to mergeUpdates {}", e.getMessage());
                    super.addToPendingList(pendingOperations);
                }
            } else {
                super.addToPendingList(pendingOperations);
            }
        }
    }

    /**
     * <p>makeCreateOperation</p>
     *
     * @param fileName a {@link java.lang.String} object.
     * @param rrdDef a {@link java.lang.Object} object.
     * @return a {@link org.opennms.netmgt.rrd.QueuingRrdStrategy.Operation} object.
     */
    CreateOperation makeCreateOperation(String fileName, Object rrdDef) {
        return new CreateOperation(fileName, rrdDef);
    }

    /**
     * <p>makeUpdateOperation</p>
     *
     * @param fileName a {@link java.lang.String} object.
     * @param owner a {@link java.lang.String} object.
     * @param update a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.rrd.QueuingRrdStrategy.Operation} object.
     */
    Operation makeUpdateOperation(String fileName, String owner, String update) {
        try {
            int colon = update.indexOf(':');
            if ((colon >= 0) && (Double.parseDouble(update.substring(colon + 1)) == 0.0)) {
                long initialTimeStamp = Long.parseLong(update.substring(0, colon));
                if (initialTimeStamp == 0)
                    m_log.debug("ZERO ERROR: created a zero update with ts=0 for file: {}, data: {}", fileName, update);

                return new ZeroUpdateOperation(fileName, initialTimeStamp);
            }
        } catch (NumberFormatException e) {

        }
        return new UpdateOperation(fileName, update);
    }

    //
    // Queue management functions.
    //
    // TODO: Put this all in a class of its own. This is really ugly.
    //

    /**
     * Add an operation to the queue.
     *
     * @param op a {@link org.opennms.netmgt.rrd.QueuingRrdStrategy.Operation} object.
     */
    private void addOperation(final Operation op) {
        synchronized (this) {
            if (queueIsFull()) {
                m_log.error("RRD Data Queue is Full!! Discarding operation for file {}", op.getFileName());
                return;
            }

            if (op.isSignificant() && sigQueueIsFull()) {
                m_log.error("RRD Data Significant Queue is Full!! Discarding operation for file {}", op.getFileName());
                return;
            }

            if (!op.isSignificant() && inSigQueueIsFull()) {
                m_log.error("RRD Insignificant Data Queue is Full!! Discarding operation for file {}", op.getFileName());
                return;
            }

            storeAssignment(op);

            setTotalOperationsPending(getTotalOperationsPending() + 1);
            setEnqueuedOperations(getEnqueuedOperations() + 1);
            if (op.isSignificant())
                setSignificantOpsEnqueued(getSignificantOpsEnqueued() + 1);
            notifyAll();
            ensureThreadsStarted();
        }
    }


    private boolean queueIsFull() {
        if (m_queueHighWaterMark <= 0)
            return false;
        else
            return getTotalOperationsPending() >= m_queueHighWaterMark;
    }

    private boolean sigQueueIsFull() {
        if (m_sigHighWaterMark <= 0)
            return false;
        else
            return getTotalOperationsPending() >= m_sigHighWaterMark;
    }

    private boolean inSigQueueIsFull() {
        if (m_inSigHighWaterMark <= 0)
            return false;
        else
            return getTotalOperationsPending() >= m_inSigHighWaterMark;
    }

    /**
     * Ensure that we have threads started to process the queue.
     */
    private synchronized void ensureThreadsStarted() {
        if (threadsRunning < m_writeThreads) {
            threadsRunning++;
            new Thread(this, this.getClass().getSimpleName() + "-" + threadsRunning).start();
        }
    }

    /**
     * Get the operations for the next file that should be worked on.
     *
     * @return a linkedList of operations to be processed all for the same file.
     */
    private LinkedList<Operation> getNext() {
        LinkedList<Operation> ops = null;
        synchronized (this) {

            // turn in our previous assignment
            completeAssignment();

            String newAssignment;
            // wait until there is work to do
            while ((newAssignment = selectNewAssignment()) == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            // initialize start time for stats
            if (getStartTime() == 0)
                setStartTime(System.currentTimeMillis());

            // reserve the assignment and take work items
            ops = takeAssignment(newAssignment);

            // keep stats
            if (ops != null) {
                for(Operation op : ops) {
                    setTotalOperationsPending(getTotalOperationsPending()-op.getCount());
                    setDequeuedOperations(getDequeuedOperations() + op.getCount());
                    if (op.isSignificant()) {
                        setSignificantOpsDequeued(getSignificantOpsDequeued() + op.getCount());
                    }
                }
                setDequeuedItems(getDequeuedItems() + 1);
            }
        }

        return ops;

    }

    /**
     * We need to track which files are being processed by which threads so that
     * we don't try to process updates for the same file on more than one
     * thread.
     */
    private synchronized void storeAssignment(Operation op) {
        // look and see if there a pending ops list for this file
        LinkedList<Operation> pendingOperations = pendingFileOperations.get(op.getFileName());

        // if not then we create an ops list for the file and add the file to
        // the work items list
        if (pendingOperations == null) {
            pendingOperations = new LinkedList<Operation>();
            pendingFileOperations.put(op.getFileName(), pendingOperations);

            // add the file to the correct list based on what type of work we
            // are adding.  (if we aren't prioritizing then every file is counted as
            // signficant
            if (!m_prioritizeSignificantUpdates || op.isSignificant())
                filesWithSignificantWork.addLast(op.getFileName());
            else
                filesWithInsignificantWork.addLast(op.getFileName());
        } else if (m_prioritizeSignificantUpdates && op.isSignificant() && hasOnlyInsignificant(pendingOperations)) {
            // only do this when we are prioritizing as this bumps files from inSig
            // up to insig
            // promote the file to the significant list if this is the first
            // significant
            filesWithSignificantWork.addLast(op.getFileName());
        }

        promoteAgedFiles();

        op.addToPendingList(pendingOperations);
    }

    /**
     * Ensure that files with insignificant changes are getting promoted if
     * necessary
     *
     */
    private synchronized void promoteAgedFiles() {

        // no need to do this is we aren't prioritizing
        if (!m_prioritizeSignificantUpdates) return;

        // the num seconds to update files is 0 then use unfair prioritization
        if (m_maxInsigUpdateSeconds == 0 || filesWithInsignificantWork.isEmpty())
            return;

        // calculate the elapsed time we first queued updates
        long now = System.currentTimeMillis();
        long elapsedMillis = Math.max(now - getStartTime(), 1);

        // calculate the milliseconds between promotions necessary to age
        // insignificant files into
        // the significant queue
        double millisPerPromotion = ((m_maxInsigUpdateSeconds * 1000.0) / filesWithInsignificantWork.size());

        // calculate the number of millis since start until the next file needs
        // to be promotoed
        long nextPromotionMillis = (long) (millisPerPromotion * getPromotionCount());

        // if more time has elapsed than the next promotion time then promote a
        // file
        if (elapsedMillis > nextPromotionMillis) {
            String file = filesWithInsignificantWork.removeFirst();
            filesWithSignificantWork.addFirst(file);
            setPromotionCount(getPromotionCount() + 1);
        }

    }

    /** {@inheritDoc} */
    @Override
    public synchronized void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        filesWithSignificantWork.addAll(0, rrdFiles);
        m_delegate.promoteEnqueuedFiles(rrdFiles);
    }

    /**
     * Return true if and only if all the operations in the list are
     * insignificant
     */
    private boolean hasOnlyInsignificant(List<Operation> pendingOps) {
        for(Operation op : pendingOps) {
            if (op.isSignificant()) {
                return false;
            }
        }
        return true;
    }

    /**
     * register the file that the currentThread is be working on. This enables
     * us to ensure that another thread doesn't try to work on operations for
     * that file.  Note: this is not synchronized as it is called from getNext which
     * is thread safe
     */
    private LinkedList<Operation> takeAssignment(String newAssignment) {

        // make the file as reserved by the current thread
        fileAssignments.put(Thread.currentThread(), newAssignment);
        reservedFiles.add(newAssignment);

        // get the assignments work list and return it
        return pendingFileOperations.remove(newAssignment);
    }

    /**
     * Return the name of the next file with available work
     */
    private String selectNewAssignment() {
        for (Iterator<String> it = filesWithSignificantWork.iterator(); it.hasNext();) {
            String fn = it.next();
            if (!reservedFiles.contains(fn)) {
                it.remove();
                return fn;
            }
        }
        for (Iterator<String> it = filesWithInsignificantWork.iterator(); it.hasNext();) {
            String fn = it.next();
            if (!reservedFiles.contains(fn)) {
                it.remove();
                return fn;
            }
        }
        return null;
    }

    /**
     * Record that fact that the current thread has finished process operations
     * for its current assignment
     */
    private synchronized void completeAssignment() {
        // remove any existing reservation of the current thread
        String previousAssignment = fileAssignments.remove(Thread.currentThread());
        if (previousAssignment != null)
            reservedFiles.remove(previousAssignment);
    }

    /**
     * <p>Constructor for QueuingRrdStrategy.</p>
     *
     * @param delegate a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     */
    public QueuingRrdStrategy(RrdStrategy<Object, Object> delegate) {
        m_delegate = delegate;
    }

    /**
     * <p>getDelegate</p>
     *
     * @return a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     */
    public RrdStrategy<Object, Object> getDelegate() {
        return m_delegate;
    }

    //
    // RrdStrategy Implementation.. These methods just enqueue the calls as
    // operations
    //

    /*
     * (non-Javadoc)
     *
     * @see RrdStrategy#closeFile(java.lang.Object)
     */
    /**
     * <p>closeFile</p>
     *
     * @param rrd a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public void closeFile(String rrd) throws Exception {
        // no need to do anything here
    }

    /** {@inheritDoc} */
    @Override
    public CreateOperation createDefinition(String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) throws Exception {
        String fileName = directory + File.separator + rrdName + m_delegate.getDefaultFileExtension();
        Object def = m_delegate.createDefinition(creator, directory, rrdName, step, dataSources, rraList);
        return makeCreateOperation(fileName, def);
    }


    /*
     * (non-Javadoc)
     *
     * @see RrdStrategy#createFile(java.lang.Object)
     */
    /**
     * <p>createFile</p>
     *
     * @param op a {@link Operation} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public void createFile(CreateOperation op) throws Exception {
        if (m_queueCreates) {
            addOperation(op);
        } else {
            m_delegate.createFile(op.getData());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see RrdStrategy#openFile(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public String openFile(String fileName) throws Exception {
        return fileName;
    }

    /*
     * (non-Javadoc)
     *
     * @see RrdStrategy#updateFile(java.lang.Object, java.lang.String, java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public void updateFile(String rrdFile, String owner, String data) throws Exception {
        addOperation(makeUpdateOperation((String) rrdFile, owner, data));
    }

    /** {@inheritDoc} */
    @Override
    public Double fetchLastValue(String rrdFile, String ds, int interval) throws NumberFormatException, RrdException {
        // TODO: handle queued values with fetch. Fetch could pull values off
        // the queue or force
        // an immediate file update.
        return m_delegate.fetchLastValue(rrdFile, ds, interval);
    }

    /** {@inheritDoc} */
    @Override
    public Double fetchLastValue(String rrdFile, String ds, String consolidationFunction, int interval) throws NumberFormatException, RrdException {
        // TODO: handle queued values with fetch. Fetch could pull values off
        // the queue or force
        // an immediate file update.
        return m_delegate.fetchLastValue(rrdFile, ds, consolidationFunction, interval);
    }

    /** {@inheritDoc} */
    @Override
    public Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range) throws NumberFormatException, RrdException {
        // TODO: handle queued values with fetch. Fetch could pull values off
        // the queue or force
        // an immediate file update.
        return m_delegate.fetchLastValueInRange(rrdFile, ds, interval, range);
    }

    /** {@inheritDoc} */
    @Override
    public InputStream createGraph(String command, File workDir) throws IOException, RrdException {
        return m_delegate.createGraph(command, workDir);
    }

    //
    // These methods are run by the write threads the process the queues.
    //

    /**
     * <p>run</p>
     */
    @Override
    public void run() {
        try {

            long waitStart = -1L;
            long delayed = 0;
            while (delayed < m_writeThreadExitDelay) {
                if (getTotalOperationsPending() > 0) {
                    delayed = 0;
                    waitStart = -1L;
                    processPendingOperations();
                } else {
                    if (waitStart < 0) {
                        waitStart = System.currentTimeMillis();
                    }
                    try {
                        Thread.sleep(m_writeThreadSleepTime);
                    } catch (InterruptedException e) {
                    }
                    long now = System.currentTimeMillis();
                    delayed = now - waitStart;
                }

            }
        } finally {
            synchronized (this) {
                threadsRunning--;
                completeAssignment();
            }
        }
    }

    /**
     * Actually process the operations be calling the underlying delegate
     * strategy
     */
    private void processPendingOperations() {
        Logging.withPrefix(m_category, new Runnable() {
            @Override public void run() {
                Object rrd = null;
                String fileName = null;

                try {
                    final LinkedList<Operation> ops = getNext();
                    if (ops == null) {
                        return;
                    }
                    // update stats correctly we update them even if an exception occurs
                    // while we are processing
                    for (final Operation op : ops) {
                        if (op.isSignificant()) {
                            setSignificantOpsCompleted(getSignificantOpsCompleted() + 1);
                        }

                    }
                    // now we actually process the events
                    for (final Operation op : ops) {
                        fileName = op.getFileName();
                        rrd = op.process(rrd);
                    }
                } catch (final Throwable e) {
                    setErrors(getErrors() + 1);
                    logLapTime("Error updating file " + fileName + ": " + e.getMessage());
                    m_log.debug("Error updating file {}: {}", fileName, e.getMessage(), e);
                } finally {
                    processClose(rrd);
                }
            }
        });
    }

    /**
     * close the rrd file
     */
    private void processClose(final Object rrd) {
        if (rrd != null) {
            try {
                m_delegate.closeFile(rrd);
            } catch (final Throwable e) {
                setErrors(getErrors() + 1);
                logLapTime("Error closing rrd " + rrd + ": " + e.getMessage());
                m_log.debug("Error closing rrd {}: {}", rrd, e.getMessage(), e);
            }
        }
    }

    /**
     * Print queue statistics.
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStats() {
        long now = System.currentTimeMillis();

        long currentElapsedMillis = Math.max(now - lastStatsTime, 1);
        long totalElapsedMillis = Math.max(now - getStartTime(), 1);

        long currentEnqueuedOps = (getEnqueuedOperations() - lastEnqueued);
        long currentDequeuedOps = (getDequeuedOperations() - lastDequeued);
        long currentDequeuedItems = (getDequeuedItems() - lastDequeuedItems);

        long currentSigOpsEnqueued = (getSignificantOpsEnqueued() - lastSignificantEnqueued);
        long currentSigOpsDequeued = (getSignificantOpsDequeued() - lastSignificantDequeued);
        //long currentSigOpsCompleted = (significantOpsCompleted - lastSignificantCompleted);

        long currentEnqueueRate = (long) (currentEnqueuedOps * 1000.0 / currentElapsedMillis);
        long currentSigEnqueueRate = (long) (currentSigOpsEnqueued * 1000.0 / currentElapsedMillis);
        long currentInsigEnqueueRate = (long) ((currentEnqueuedOps - currentSigOpsEnqueued) * 1000.0 / currentElapsedMillis);
        long overallEnqueueRate = (long) (getEnqueuedOperations() * 1000.0 / totalElapsedMillis);
        long overallSigEnqueueRate = (long) (getSignificantOpsEnqueued() * 1000.0 / totalElapsedMillis);
        long overallInsigEnqueueRate = (long) ((getEnqueuedOperations() - getSignificantOpsEnqueued()) * 1000.0 / totalElapsedMillis);

        long currentDequeueRate = (long) (currentDequeuedOps * 1000.0 / currentElapsedMillis);
        long currentSigDequeueRate = (long) (currentSigOpsDequeued * 1000.0 / currentElapsedMillis);
        long currentInsigDequeueRate = (long) ((currentDequeuedOps - currentSigOpsDequeued) * 1000.0 / currentElapsedMillis);
        long overallDequeueRate = (long) (getDequeuedOperations() * 1000.0 / totalElapsedMillis);
        long overallSigDequeueRate = (long) (getSignificantOpsDequeued() * 1000.0 / totalElapsedMillis);
        long overallInsigDequeueRate = (long) ((getDequeuedOperations() - getSignificantOpsDequeued()) * 1000.0 / totalElapsedMillis);

        long currentItemDequeueRate = (long) (currentDequeuedItems * 1000.0 / currentElapsedMillis);
        long overallItemDequeueRate = (long) (getDequeuedItems() * 1000.0 / totalElapsedMillis);

        String stats = "\nQS:\t" + "totalOperationsPending=" + getTotalOperationsPending() +
                ", significantOpsPending=" + (getSignificantOpsEnqueued() - getSignificantOpsCompleted()) +
                ", filesWithSignificantWork=" + filesWithSignificantWork.size() +
                ", filesWithInsignificantWork=" + filesWithInsignificantWork.size()

                + "\nQS:\t" + ", createsCompleted=" + getCreatesCompleted() +
                ", updatesCompleted=" + getUpdatesCompleted() +
                ", errors=" + getErrors() +
                ", promotionRate=" + ((double) (getPromotionCount() * 1000.0 / totalElapsedMillis)) +
                ", promotionCount=" + getPromotionCount()

                + "\nQS:\t" + ", currentEnqueueRates=(" + currentSigEnqueueRate + "/" + currentInsigEnqueueRate + "/" + currentEnqueueRate + ")" +
                ", currentDequeueRate=(" + currentSigDequeueRate + "/" + currentInsigDequeueRate + "/" + currentDequeueRate + ")" +
                ", currentItemDequeRate=" + currentItemDequeueRate +
                ", currentOpsPerUpdate=" + (currentDequeuedOps / Math.max(currentDequeuedItems, 1.0)) +
                ", currentPrcntSignificant=" + (currentSigOpsEnqueued * 100.0 / Math.max(currentEnqueuedOps, 1.0)) + "%" + ", elapsedTime=" + ((currentElapsedMillis + 500) / 1000)

                + "\nQS:\t" + ", overallEnqueueRate=(" + overallSigEnqueueRate + "/" + overallInsigEnqueueRate + "/" + overallEnqueueRate + ")" +
                ", overallDequeueRate=(" + overallSigDequeueRate + "/" + overallInsigDequeueRate + "/" + overallDequeueRate + ")" +
                ", overallItemDequeRate=" + overallItemDequeueRate +
                ", overallOpsPerUpdate=" + (getDequeuedOperations() / Math.max(getDequeuedItems(), 1.0)) +
                ", overallPrcntSignificant=" + (getSignificantOpsEnqueued() * 100.0 / Math.max(getEnqueuedOperations(), 1.0)) + "%" +
                ", totalElapsedTime=" + ((totalElapsedMillis + 500) / 1000);

        lastStatsTime = now;
        lastEnqueued = getEnqueuedOperations();
        lastDequeued = getDequeuedOperations();
        lastDequeuedItems = getDequeuedItems();
        lastSignificantEnqueued = getSignificantOpsEnqueued();
        lastSignificantDequeued = getSignificantOpsDequeued();
        lastSignificantCompleted = getSignificantOpsCompleted();
        lastOpsPending = getTotalOperationsPending();

        return stats;
    }

    void logStats() {
        if (m_log.isDebugEnabled()) {
            logLapTime(getStats());
        }
    }

    void logLapTime(final String message) {
        logLapTime(message, null);
    }

    void logLapTime(final String message, final Throwable t) {
        if (t != null) {
            m_log.debug("{} {}", message, getLapTime(), t);
        } else {
            m_log.debug("{} {}", message, getLapTime());
        }
    }

    /**
     * <p>getLapTime</p>
     *
     * @return a {@link java.lang.String} object.
     */
    private String getLapTime() {
        final long newLap = System.currentTimeMillis();
        final double seconds = (newLap - lastLap) / 1000.0;
        lastLap = newLap;
        return "[" + seconds + " sec]";
    }

    /**
     * <p>getGraphLeftOffset</p>
     *
     * @return a int.
     */
    @Override
    public int getGraphLeftOffset() {
        return m_delegate.getGraphLeftOffset();
    }

    /**
     * <p>getGraphRightOffset</p>
     *
     * @return a int.
     */
    @Override
    public int getGraphRightOffset() {
        return m_delegate.getGraphRightOffset();
    }

    /**
     * <p>getGraphTopOffsetWithText</p>
     *
     * @return a int.
     */
    @Override
    public int getGraphTopOffsetWithText() {
        return m_delegate.getGraphTopOffsetWithText();
    }

    /**
     * <p>getDefaultFileExtension</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getDefaultFileExtension() {
        return m_delegate.getDefaultFileExtension();
    }

    /** {@inheritDoc} */
    @Override
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir) throws IOException, RrdException {
        return m_delegate.createGraphReturnDetails(command, workDir);
    }

    /**
     * <p>getTotalOperationsPending</p>
     *
     * @return a long.
     */
    public long getTotalOperationsPending() {
        return m_totalOperationsPending;
    }

    /**
     * <p>setTotalOperationsPending</p>
     *
     * @param totalOperationsPending a long.
     */
    public void setTotalOperationsPending(long totalOperationsPending) {
        m_totalOperationsPending = totalOperationsPending;
    }

    /**
     * <p>getCreatesCompleted</p>
     *
     * @return a long.
     */
    public long getCreatesCompleted() {
        return m_createsCompleted;
    }

    /**
     * <p>setCreatesCompleted</p>
     *
     * @param createsCompleted a long.
     */
    public void setCreatesCompleted(long createsCompleted) {
        m_createsCompleted = createsCompleted;
    }

    /**
     * <p>getUpdatesCompleted</p>
     *
     * @return a long.
     */
    public long getUpdatesCompleted() {
        return m_updatesCompleted;
    }

    /**
     * <p>setUpdatesCompleted</p>
     *
     * @param updatesCompleted a long.
     */
    public void setUpdatesCompleted(long updatesCompleted) {
        m_updatesCompleted = updatesCompleted;
    }

    /**
     * <p>getErrors</p>
     *
     * @return a long.
     */
    public long getErrors() {
        return m_errors;
    }

    /**
     * <p>setErrors</p>
     *
     * @param errors a long.
     */
    public void setErrors(long errors) {
        m_errors = errors;
    }

    /**
     * <p>getPromotionCount</p>
     *
     * @return a long.
     */
    public long getPromotionCount() {
        return m_promotionCount;
    }

    /**
     * <p>setPromotionCount</p>
     *
     * @param promotionCount a long.
     */
    public void setPromotionCount(long promotionCount) {
        m_promotionCount = promotionCount;
    }

    /**
     * <p>getSignificantOpsEnqueued</p>
     *
     * @return a long.
     */
    public long getSignificantOpsEnqueued() {
        return m_significantOpsEnqueued;
    }

    /**
     * <p>setSignificantOpsEnqueued</p>
     *
     * @param significantOpsEnqueued a long.
     */
    public void setSignificantOpsEnqueued(long significantOpsEnqueued) {
        m_significantOpsEnqueued = significantOpsEnqueued;
    }

    /**
     * <p>getSignificantOpsDequeued</p>
     *
     * @return a long.
     */
    public long getSignificantOpsDequeued() {
        return m_significantOpsDequeued;
    }

    /**
     * <p>setSignificantOpsDequeued</p>
     *
     * @param significantOpsDequeued a long.
     */
    public void setSignificantOpsDequeued(long significantOpsDequeued) {
        m_significantOpsDequeued = significantOpsDequeued;
    }

    /**
     * <p>getEnqueuedOperations</p>
     *
     * @return a long.
     */
    public long getEnqueuedOperations() {
        return m_enqueuedOperations;
    }

    /**
     * <p>setEnqueuedOperations</p>
     *
     * @param enqueuedOperations a long.
     */
    public void setEnqueuedOperations(long enqueuedOperations) {
        m_enqueuedOperations = enqueuedOperations;
    }

    /**
     * <p>getDequeuedOperations</p>
     *
     * @return a long.
     */
    public long getDequeuedOperations() {
        return m_dequeuedOperations;
    }

    /**
     * <p>setDequeuedOperations</p>
     *
     * @param dequeuedOperations a long.
     */
    public void setDequeuedOperations(long dequeuedOperations) {
        m_dequeuedOperations = dequeuedOperations;
    }

    /**
     * <p>getDequeuedItems</p>
     *
     * @return a long.
     */
    public long getDequeuedItems() {
        return m_dequeuedItems;
    }

    /**
     * <p>setDequeuedItems</p>
     *
     * @param dequeuedItems a long.
     */
    public void setDequeuedItems(long dequeuedItems) {
        m_dequeuedItems = dequeuedItems;
    }

    /**
     * <p>getSignificantOpsCompleted</p>
     *
     * @return a long.
     */
    public long getSignificantOpsCompleted() {
        return m_significantOpsCompleted;
    }

    /**
     * <p>setSignificantOpsCompleted</p>
     *
     * @param significantOpsCompleted a long.
     */
    public void setSignificantOpsCompleted(long significantOpsCompleted) {
        m_significantOpsCompleted = significantOpsCompleted;
    }

    /**
     * <p>getStartTime</p>
     *
     * @return a long.
     */
    public long getStartTime() {
        return m_startTime;
    }

    /**
     * <p>setStartTime</p>
     *
     * @param updateStart a long.
     */
    public void setStartTime(long updateStart) {
        m_startTime = updateStart;
    }


}
