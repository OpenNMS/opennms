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

import org.opennms.netmgt.snmp.proxy.ProxiableTracker;

public abstract class CollectionTracker implements Collectable, ProxiableTracker {
    
    private CollectionTracker m_parent;
    private boolean m_failed = false;
    private boolean m_timedOut = false;
    private boolean m_finished = false;
    
    
    public CollectionTracker() {
        this(null);
    }
    
    public CollectionTracker(CollectionTracker parent) {
        m_parent = parent;
    }

    public void setParent(CollectionTracker parent) {
        m_parent = parent;
    }
    
    public CollectionTracker getParent() {
        return m_parent;
    }

    public boolean failed() { return m_failed || m_timedOut; }
    
    public boolean timedOut() { return m_timedOut; }
    
    public abstract void setMaxRepetitions(int maxRepetitions);
    
    public abstract void setMaxRetries(int maxRetries);

    public void setFailed(boolean failed) {
        m_failed = failed;
    }
    
    public void setTimedOut(boolean timedOut) {
        m_timedOut = timedOut;
    }
    
    protected void storeResult(SnmpResult res) {
        if (m_parent != null) {
            m_parent.storeResult(res);
        }
    }
    
    public boolean isFinished() {
        return m_finished;
    }
    
    public final void setFinished(boolean finished) {
        m_finished = finished;
    }

    public abstract ResponseProcessor buildNextPdu(PduBuilder pduBuilder) throws SnmpException;

    protected void reportTooBigErr(String msg) {
        if (m_parent != null) {
            m_parent.reportTooBigErr(msg);
        }
    }
    
    protected void reportGenErr(String msg) {
        if (m_parent != null) {
            m_parent.reportGenErr(msg);
        }
    }
    
    protected void reportNoSuchNameErr(String msg) {
        if (m_parent != null) {
            m_parent.reportNoSuchNameErr(msg);
        }
    }

    protected void reportFatalErr(final ErrorStatusException ex) {
        if (m_parent != null) {
            m_parent.reportFatalErr(ex);
        }
    }

    protected void reportNonFatalErr(final ErrorStatus status) {
        if (m_parent != null) {
            m_parent.reportNonFatalErr(status);
        }
    }

    @Override
    public CollectionTracker getCollectionTracker() {
        return this;
    }
}
