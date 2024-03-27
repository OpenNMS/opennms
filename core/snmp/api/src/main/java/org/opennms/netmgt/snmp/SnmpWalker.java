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
package org.opennms.netmgt.snmp;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class SnmpWalker implements AutoCloseable {

    protected abstract static class WalkerPduBuilder extends PduBuilder {
        protected WalkerPduBuilder(int maxVarsPerPdu) {
            super(maxVarsPerPdu);
        }
        
        public abstract void reset();
    }
    
    private final String m_name;
    private final CollectionTracker m_tracker;

    private final CountDownLatch m_signal;

    private final InetAddress m_address;
    private WalkerPduBuilder m_pduBuilder;
    private ResponseProcessor m_responseProcessor;
    private final int m_maxVarsPerPdu;
    private boolean m_error = false;
    private String m_errorMessage = "";
    private Throwable m_errorThrowable = null;

    private SnmpWalkCallback m_callback;

    protected SnmpWalker(InetAddress address, String name, int maxVarsPerPdu, int maxRepetitions, int maxRetries, CollectionTracker tracker) {
        m_address = address;
        m_signal = new CountDownLatch(1);
        
        m_name = name;

        m_tracker = tracker;
        m_tracker.setMaxRepetitions(maxRepetitions);
        m_tracker.setMaxRetries(maxRetries);
        
        m_maxVarsPerPdu = maxVarsPerPdu;
    }

    /**
     * Sets an (optional) callback that will be triggered when the walk was successfully completed,
     * or failed due to some error.
     *
     * @param callback the callback
     */
    public void setCallback(SnmpWalkCallback callback) {
        m_callback = callback;
    }

    protected abstract WalkerPduBuilder createPduBuilder(int maxVarsPerPdu);
    
    public void start() {
        m_pduBuilder = createPduBuilder(m_maxVarsPerPdu);
        try {
            buildAndSendNextPdu();
        } catch (Throwable e) {
            handleFatalError(e);
        }
    }
    
    public final int getMaxVarsPerPdu() {
        return (m_pduBuilder == null ? m_maxVarsPerPdu : m_pduBuilder.getMaxVarsPerPdu());
    }

    protected void buildAndSendNextPdu() throws SnmpException {
        if (m_tracker.isFinished()) {
            handleDone();
        } else {
            m_pduBuilder.reset();
            m_responseProcessor = m_tracker.buildNextPdu(m_pduBuilder);
            sendNextPdu(m_pduBuilder);
        }
    }

    protected abstract void sendNextPdu(WalkerPduBuilder pduBuilder) throws SnmpException;

    protected void handleDone() {
        finish();
    }

    /**
     * <P>
     * Returns the success or failure code for collection of the data.
     * </P>
     */
    public boolean failed() {
        return m_error;
    }
    
    public boolean timedOut() {
        return m_tracker.timedOut();
    }

    protected void handleAuthError(String msg) {
        m_tracker.setFailed(true);
        processError("Authentication error processing", msg, null);
    }

    protected void handleError(String msg) {
        // XXX why do we set timedOut to false here?  should we be doing this everywhere?
        m_tracker.setTimedOut(false);
        processError("Error retrieving", msg, null);
    }

    protected void handleError(String msg, Throwable t) {
        // XXX why do we set timedOut to false here?  should we be doing this everywhere?
        m_tracker.setTimedOut(false);
        processError("Error retrieving", msg, t);
    }

    protected void handleFatalError(Throwable e) {
        m_tracker.setFailed(true);
        processError("Unexpected error occurred processing", e.toString(), e);
    }
    
    protected void handleTimeout(String msg) {
        m_tracker.setTimedOut(true);
        processError("Timeout retrieving", msg, new SnmpAgentTimeoutException(getName(), m_address));
    }

    private void processError(String reason, String cause, Throwable t) {
        String logMessage = reason + " " + getName() + " for " + m_address + ": " + cause;

        m_error = true;
        m_errorMessage = logMessage;
        m_errorThrowable = t;
        
        finish();
    }

    private void finish() {
        signal();
        // Trigger the callback after the latch was decreased and the session was closed.
        if (m_callback != null) {
            Throwable t = null;
            if (failed()) {
                t = getErrorThrowable();
                if (t == null) {
                    // Not all of the failures provide an exception, so we generate one if necessary
                    t = new SnmpException(getErrorMessage());
                }
            }
            m_callback.complete(this, t);
        }
    }

    @Override
    public abstract void close();

    public final String getName() {
        return m_name;
    }

    private void signal() {
        synchronized (this) {
            notifyAll();
        }
        if (m_signal != null) {
            m_signal.countDown();
        }
    }

    public void waitFor() throws InterruptedException {
        m_signal.await();
    }

    public boolean waitFor(long timeout) throws InterruptedException {
        return m_signal.await(timeout, TimeUnit.MILLISECONDS);
        /*
         * NOTE: It is wrong to call handleTimeout here (which someone added). A timeout waiting for an agent respond
         * is NOT the same as deciding you want to wait a while to see if the walker has finished and then do something
         * else and then come back and potentially wait for another few millis.
         */ 
    }
    
    // processErrors returns true if we need to retry the request and false otherwise
    protected boolean processErrors(int errorStatus, int errorIndex) throws SnmpException {
        return m_responseProcessor.processErrors(errorStatus, errorIndex);
    }
    
    protected void processResponse(SnmpObjId receivedOid, SnmpValue val) throws SnmpException {
        m_responseProcessor.processResponse(receivedOid, val);
    }

    protected final InetAddress getAddress() {
        return m_address;
    }

    public final String getErrorMessage() {
        return m_errorMessage;
    }

    public final Throwable getErrorThrowable() {
        return m_errorThrowable;
    }

}
