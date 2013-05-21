/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.mock.EventWrapper;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockUtil;



/**
 * Anticipates outages based on events
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 */
public class OutageAnticipator implements EventListener {
    
    private final MockDatabase m_db;
    private int m_expectedOpenCount;
    private int m_expectedOutageCount;
    
    private final Map<EventWrapper, List<Outage>> m_pendingOpens = new HashMap<EventWrapper, List<Outage>>();
    private final Map<EventWrapper, List<Outage>> m_pendingCloses = new HashMap<EventWrapper, List<Outage>>();
    private final Set<Outage> m_expectedOutages = new HashSet<Outage>();
    
    public OutageAnticipator(MockDatabase db) {
        m_db = db;
        reset();
    }

    /**
     * 
     */
    public synchronized void reset() {
        m_expectedOpenCount = m_db.countOpenOutages();
        m_expectedOutageCount = m_db.countOutages();
        m_expectedOutages.clear();
        m_expectedOutages.addAll(m_db.getOutages());
       
    }

    /**
     * @param element
     * @param lostService
     */
    public synchronized void anticipateOutageOpened(MockElement element, final Event lostService) {
        MockVisitor outageCounter = new MockVisitorAdapter() {
            @Override
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
    protected synchronized boolean anticipatesClose(MockService svc) {
        return anticipates(m_pendingCloses, svc);
    }

    private synchronized boolean anticipates(Map<EventWrapper, List<Outage>> pending, MockService svc) {
        for (List<Outage> outageList : pending.values()) {
            for (Outage outage : outageList) {
                if (outage.isForService(svc)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param outageMap
     * @param outageEvent
     * @param svc
     */
    protected synchronized void addToOutageList(Map<EventWrapper, List<Outage>> outageMap, Event outageEvent, Outage outage) {
        EventWrapper w = new EventWrapper(outageEvent);
        List<Outage> list = outageMap.get(w);
        if (list == null) {
            list = new LinkedList<Outage>();
            outageMap.put(w, list);
        }
        list.add(outage);
    }
    
    protected synchronized void removeFromOutageList(Map<EventWrapper, List<Outage>> outageMap, Event outageEvent, Outage outage) {
        EventWrapper w = new EventWrapper(outageEvent);
        List<Outage> list = outageMap.get(w);
        if (list == null) {
            return;
        }
        list.remove(outage);
        
    }


    
    public synchronized void deanticipateOutageClosed(MockElement element, final Event regainService) {
        MockVisitor outageCounter = new MockVisitorAdapter() {
            @Override
            public void visitService(MockService svc) {
                if (anticipatesClose(svc)) {
                    // Decrease the open ones.. leave the total the same
                    m_expectedOpenCount++;
                    
                    for (Outage outage : m_db.getOpenOutages(svc)) {
                        MockUtil.println("Deanticipating outage closed: "+outage);

                        removeFromOutageList(m_pendingCloses, regainService, outage);
                    }
                }
            }
        };
        element.visit(outageCounter);
        
    }

    public synchronized void anticipateOutageClosed(MockElement element, final Event regainService) {
        MockVisitor outageCounter = new MockVisitorAdapter() {
            @Override
            public void visitService(MockService svc) {
                if ((m_db.hasOpenOutage(svc) || anticipatesOpen(svc)) && !anticipatesClose(svc)) {
                    // Decrease the open ones.. leave the total the same
                    m_expectedOpenCount--;
                    
                    for (Outage outage : m_db.getOpenOutages(svc)) {
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

    public synchronized boolean checkAnticipated() {
        int openCount = m_db.countOpenOutages();
        int outageCount = m_db.countOutages();
        
        if (openCount != m_expectedOpenCount || outageCount != m_expectedOutageCount) {
            return false;
        } 
        
        if (m_pendingOpens.size() != 0 || m_pendingCloses.size() != 0) {
            return false;
        }
        
        Set<Outage> currentOutages = new HashSet<Outage>(m_db.getOutages());
        if (!m_expectedOutages.equals(currentOutages)) {
            for (Outage expectedOutage : m_expectedOutages) {
                if (currentOutages.contains(expectedOutage)) {
                    currentOutages.remove(expectedOutage);
                } else {
                    log().warn("Expected outage " + expectedOutage.toDetailedString() + " not in current Set");
                }
            }
            for (Outage unexpectedOutage : currentOutages) {
                log().warn("Unexpected outage "+unexpectedOutage.toDetailedString()+" in database");
            }
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventListener#getName()
     */
    @Override
    public String getName() {
        return "OutageAnticipator";
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventListener#onEvent(org.opennms.netmgt.xml.event.Event)
     */
    @Override
    public synchronized void onEvent(Event e) {
        for (Outage outage : getOutageList(m_pendingOpens, e)) {
            outage.setLostEvent(e.getDbid(), MockEventUtil.convertEventTimeIntoTimestamp(e.getTime()));
            m_expectedOutages.add(outage);
        }
        clearOutageList(m_pendingOpens, e);
        
        for (Outage outage : getOutageList(m_pendingCloses, e)) {
            closeExpectedOutages(e, outage);
        }
        clearOutageList(m_pendingCloses, e);
    }

    private synchronized void closeExpectedOutages(Event e, Outage pendingOutage) {
        for (Outage outage : m_expectedOutages) {
            if (pendingOutage.equals(outage)) {
                outage.setRegainedEvent(e.getDbid(), MockEventUtil.convertEventTimeIntoTimestamp(e.getTime()));
            }
        }
    }

    /**
     * @param pending
     * @param e
     */
    private synchronized void clearOutageList(Map<EventWrapper, List<Outage>> pending, Event e) {
        pending.remove(new EventWrapper(e));
    }

    /**
     * @param pending
     * @param e
     * @return
     */
    private synchronized List<Outage> getOutageList(Map<EventWrapper, List<Outage>> pending, Event e) {
        EventWrapper w = new EventWrapper(e);
        if (pending.containsKey(w)) {
            return pending.get(w);
        }
        
        return new ArrayList<Outage>(0);
    }

    /**
     * @param ipAddr
     * @param nodeId
     * @param nodeId2
     */
    public void anticipateReparent(String ipAddr, int nodeId, int nodeId2) {
        
    }
    
    private ThreadCategory log() {
        return ThreadCategory.getInstance(OutageAnticipator.class);
    }

    
}
