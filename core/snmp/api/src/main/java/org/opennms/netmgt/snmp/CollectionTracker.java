/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;


public abstract class CollectionTracker implements Collectable {
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

    public abstract ResponseProcessor buildNextPdu(PduBuilder pduBuilder);

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
