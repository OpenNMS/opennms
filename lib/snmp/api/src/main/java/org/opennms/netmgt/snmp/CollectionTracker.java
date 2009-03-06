//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Aug 29: Set defaults for m_failed, m_timedOut. - dj@opennms.org
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp;


public abstract class CollectionTracker implements Collectable {
    
    public static final int NO_ERR = 0;
    public static final int TOO_BIG_ERR = 1;
    public static final int NO_SUCH_NAME_ERR = 2;
    public static final int GEN_ERR = 5;

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
    
    abstract public void setMaxRepetitions(int maxRepetitions);
    
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
    
    public CollectionTracker getCollectionTracker() {
        return this;
    }


}
