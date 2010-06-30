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


/**
 * <p>Abstract CollectionTracker class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class CollectionTracker implements Collectable {
    
    /** Constant <code>NO_ERR=0</code> */
    public static final int NO_ERR = 0;
    /** Constant <code>TOO_BIG_ERR=1</code> */
    public static final int TOO_BIG_ERR = 1;
    /** Constant <code>NO_SUCH_NAME_ERR=2</code> */
    public static final int NO_SUCH_NAME_ERR = 2;
    /** Constant <code>GEN_ERR=5</code> */
    public static final int GEN_ERR = 5;

    private CollectionTracker m_parent;
    private boolean m_failed;
    private boolean m_timedOut;
    
    
    /**
     * <p>Constructor for CollectionTracker.</p>
     */
    public CollectionTracker() {
        this(null);
    }
    
    /**
     * <p>Constructor for CollectionTracker.</p>
     *
     * @param parent a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    public CollectionTracker(CollectionTracker parent) {
        m_parent = parent;
        m_failed = false;
    }

    /**
     * <p>setParent</p>
     *
     * @param parent a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    public void setParent(CollectionTracker parent) {
        m_parent = parent;
    }
    
    /**
     * <p>getParent</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    public CollectionTracker getParent() {
        return m_parent;
    }

    /**
     * <p>failed</p>
     *
     * @return a boolean.
     */
    public boolean failed() { return m_failed || m_timedOut; }
    
    /**
     * <p>timedOut</p>
     *
     * @return a boolean.
     */
    public boolean timedOut() { return m_timedOut; }
    
    /**
     * <p>setMaxRepititions</p>
     *
     * @param maxRepititions a int.
     */
    abstract public void setMaxRepititions(int maxRepititions);
    
    /**
     * <p>setFailed</p>
     *
     * @param failed a boolean.
     */
    public void setFailed(boolean failed) {
        m_failed = failed;
    }
    
    /**
     * <p>setTimedOut</p>
     *
     * @param timedOut a boolean.
     */
    public void setTimedOut(boolean timedOut) {
        m_timedOut = timedOut;
    }
    
    /**
     * <p>storeResult</p>
     *
     * @param base a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     * @param val a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    protected void storeResult(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
        if (m_parent != null)
            m_parent.storeResult(base, inst, val);
    }
    
    /**
     * <p>isFinished</p>
     *
     * @return a boolean.
     */
    public abstract boolean isFinished();

    /**
     * <p>buildNextPdu</p>
     *
     * @param pduBuilder a {@link org.opennms.netmgt.snmp.PduBuilder} object.
     * @return a {@link org.opennms.netmgt.snmp.ResponseProcessor} object.
     */
    public abstract ResponseProcessor buildNextPdu(PduBuilder pduBuilder);

    /**
     * <p>reportTooBigErr</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    protected void reportTooBigErr(String msg) {
        if (m_parent != null)
            m_parent.reportTooBigErr(msg);
    }
    
    /**
     * <p>reportGenErr</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    protected void reportGenErr(String msg) {
        if (m_parent != null)
            m_parent.reportGenErr(msg);
    }
    
    /**
     * <p>reportNoSuchNameErr</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    protected void reportNoSuchNameErr(String msg) {
        if (m_parent != null)
            m_parent.reportNoSuchNameErr(msg);
    }
    
    /**
     * <p>getCollectionTracker</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    public CollectionTracker getCollectionTracker() {
        return this;
    }

}
