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
package org.opennms.netmgt.mock;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.xml.event.Event;



/**
 * Anticipates outages based on events
 * @author brozow
 */
public class OutageAnticipator implements EventListener {
    
    private MockDatabase m_db;
    private int m_expectedOpenCount;
    private int m_expectedOutageCount;
    
    private Map m_pendingOpens = new HashMap();
    private Map m_pendingCloses = new HashMap();
    private Set m_expectedOutages = new HashSet();
    
    public OutageAnticipator(MockDatabase db) {
        m_db = db;
        reset();
    }

    /**
     * 
     */
    public void reset() {
        m_expectedOpenCount = m_db.countOpenOutages();
        m_expectedOutageCount = m_db.countOutages();
        m_expectedOutages.clear();
        m_expectedOutages.addAll(m_db.getOutages());
       
    }

    /**
     * @param element
     * @param lostService
     */
    public void anticipateOutageOpened(MockElement element, final Event lostService) {
        MockVisitor outageCounter = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                if (!m_db.hasOpenOutage(svc) || anticipatesClose(svc)) {
                    m_expectedOpenCount++;
                    m_expectedOutageCount++;
                    Outage outage = new Outage(svc);
                    MockUtil.println("Anticipating outage open: "+outage);
                    addToOutageList(m_pendingOpens, lostService, outage);
                }
            }
        };
        element.visit(outageCounter);
    }

    /**
     * @param svc
     * @return
     */
    protected boolean anticipatesClose(MockService svc) {
        return anticipates(m_pendingCloses, svc);
    }

    private boolean anticipates(Map pending, MockService svc) {
        Collection vals = pending.values();
        for (Iterator it = vals.iterator(); it.hasNext();) {
            List outageList = (List) it.next();
            for (Iterator iter = outageList.iterator(); iter.hasNext();) {
                Outage outage = (Outage) iter.next();
                if (outage.isForService(svc))
                    return true;
            }
        }
        return false;
    }

    /**
     * @param outageMap
     * @param outageEvent
     * @param svc
     */
    protected void addToOutageList(Map outageMap, Event outageEvent, Outage outage) {
        EventWrapper w = new EventWrapper(outageEvent);
        List list = (List)outageMap.get(w);
        if (list == null) {
            list = new LinkedList();
            outageMap.put(w, list);
        }
        list.add(outage);
    }
    
    protected void removeFromOutageList(Map outageMap, Event outageEvent, Outage outage) {
        EventWrapper w = new EventWrapper(outageEvent);
        List list = (List)outageMap.get(w);
        if (list == null) return;
        list.remove(outage);
        
    }


    
    public void deanticipateOutageClosed(MockElement element, final Event regainService) {
        MockVisitor outageCounter = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                if (anticipatesClose(svc)) {
                    // descrease the open ones.. leave the total the same
                    m_expectedOpenCount++;
                    
                    Collection openOutages = m_db.getOpenOutages(svc);
                    for (Iterator it = openOutages.iterator(); it.hasNext();) {
                        Outage outage = (Outage) it.next();
                        MockUtil.println("Deanticipating outage closed: "+outage);

                        removeFromOutageList(m_pendingCloses, regainService, outage);
                    }
                }
            }
        };
        element.visit(outageCounter);
        
    }

    public void anticipateOutageClosed(MockElement element, final Event regainService) {
        MockVisitor outageCounter = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                if ((m_db.hasOpenOutage(svc) || anticipatesOpen(svc)) && !anticipatesClose(svc)) {
                    // descrease the open ones.. leave the total the same
                    m_expectedOpenCount--;
                    
                    Collection openOutages = m_db.getOpenOutages(svc);
                    for (Iterator it = openOutages.iterator(); it.hasNext();) {
                        Outage outage = (Outage) it.next();
                        MockUtil.println("Anticipating outage closed: "+outage);

                        addToOutageList(m_pendingCloses, regainService, outage);
                    }
                }
            }
        };
        element.visit(outageCounter);
    }
    
    /**
     * @param svc
     * @return
     */
    protected boolean anticipatesOpen(MockService svc) {
        return anticipates(m_pendingOpens, svc);
    }

    public int getExpectedOpens() {
        return m_expectedOpenCount;
    }
    
    public int getActualOpens() {
        return m_db.countOpenOutages();
    }
    
    public int getExpectedOutages() {
        return m_expectedOutageCount;
    }
    
    public int getActualOutages() {
        return m_db.countOutages();
    }

    public boolean checkAnticipated() {
        int openCount = m_db.countOpenOutages();
        int outageCount = m_db.countOutages();
        
        if (openCount != m_expectedOpenCount || outageCount != m_expectedOutageCount) {
            return false;
        } 
        
        if (m_pendingOpens.size() != 0 || m_pendingCloses.size() != 0) 
            return false;
        
        Set currentOutages = new HashSet(m_db.getOutages());
        return m_expectedOutages.equals(currentOutages);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventListener#getName()
     */
    public String getName() {
        return "OutageAnticipator";
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventListener#onEvent(org.opennms.netmgt.xml.event.Event)
     */
    public void onEvent(Event e) {
        Collection pendingOpens = getOutageList(m_pendingOpens, e);
        for (Iterator it = pendingOpens.iterator(); it.hasNext();) {
            Outage outage = (Outage) it.next();
            outage.setLostEvent(e.getDbid(), MockUtil.convertEventTimeIntoTimestamp(e.getTime()));
            m_expectedOutages.add(outage);
        }
        clearOutageList(m_pendingOpens, e);
        
        Collection pendingCloses = getOutageList(m_pendingCloses, e);
        for (Iterator it = pendingCloses.iterator(); it.hasNext();) {
            Outage outage = (Outage) it.next();
            closeExpectedOutages(e, outage);
        }
        clearOutageList(m_pendingCloses, e);
    }

    private void closeExpectedOutages(Event e, Outage pendingOutage) {
        for (Iterator it = m_expectedOutages.iterator(); it.hasNext();) {
            Outage outage = (Outage) it.next();
            if (pendingOutage.equals(outage)) {
                outage.setRegainedEvent(e.getDbid(), MockUtil.convertEventTimeIntoTimestamp(e.getTime()));
            }
        }
    }

    /**
     * @param pending
     * @param e
     */
    private void clearOutageList(Map pending, Event e) {
        pending.remove(new EventWrapper(e));
    }

    /**
     * @param pending
     * @param e
     * @return
     */
    private Collection getOutageList(Map pending, Event e) {
        EventWrapper w = new EventWrapper(e);
        if (pending.containsKey(w)) {
            return (Collection)pending.get(w);
        }
        
        return Collections.EMPTY_LIST;
    }

    /**
     * @param ipAddr
     * @param nodeId
     * @param nodeId2
     */
    public void anticipateReparent(String ipAddr, int nodeId, int nodeId2) {
        
    }
    
    
    
}
