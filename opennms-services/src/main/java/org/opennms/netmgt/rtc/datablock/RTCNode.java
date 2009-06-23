//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.rtc.datablock;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.rtc.RTCConstants;

/**
 * The main unit for the RTCManager.
 * <p>
 * RTCNode is the main data unit for the RTCManager - these datablocks are
 * created initially as data is read from the database and later if a
 * 'nodeGainedService' is received - each node maintains its node id, ip
 * address, service name and a list of 'RTCNodeSvcTime's
 * </p>
 * 
 * <p>
 * Also, each node knows and maintains a list of categories that this tuple
 * belongs to
 * </p>
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * 
 * @see org.opennms.netmgt.rtc.datablock.RTCNodeSvcTime
 * @see org.opennms.netmgt.rtc.datablock.RTCNodeSvcTimesList
 */
public class RTCNode {
    /**
     * The node ID.
     */
    private long m_nodeID;

    /**
     * The ip address of the interface of the node.
     */
    private String m_ip;

    /**
     * The service name.
     */
    private String m_svcName;

    /**
     * List of the lost/regained service times for this node.
     */
    private RTCNodeSvcTimesList m_svcTimesList;

    /**
     * List of the categories this node belongs to
     */
    private List<String> m_categories;

    /**
     * Default constructor. Initializes all values
     */
    public RTCNode() {
    	this(-1, null, null);
    }
    
    public RTCNode(RTCNodeKey key) {
    	this(key.getNodeID(), key.getIP(), key.getSvcName());
    }

    /**
     * Constructor.
     * 
     * @param nodeid
     *            the node id
     * @param ip
     *            the IP address
     * @param svcName
     *            the service
     */
    public RTCNode(long nodeid, String ip, String svcName) {
        m_nodeID = nodeid;

        m_ip = ip;

        m_svcName = svcName;

        m_svcTimesList = new RTCNodeSvcTimesList();
        m_categories = new ArrayList<String>();
    }

    /**
     * Set the node ID.
     * 
     * @param id
     *            the node ID
     */
    public void setNodeID(long id) {
        m_nodeID = id;
    }

    /**
     * Set the service name.
     * 
     * @param svcName
     *            the service name
     */
    public void setSvcName(String svcName) {
        m_svcName = svcName;
    }

    /**
     * Set the IP address.
     * 
     * @param ipStr
     *            the ip address
     */
    public void setIP(String ipStr) {
        m_ip = ipStr;
    }

    /**
     * Add a new 'RTCNodeSvcTime' entry for this node.
     * 
     * @param losttime
     *            time at which service was lost
     * @param regainedtime
     *            time at which service was regained
     */
    public void addSvcTime(long losttime, long regainedtime) {
        m_svcTimesList.addSvcTime(losttime, regainedtime);
    }

    /**
     * Add to the category list for this node.
     * 
     * @param catLabel
     *            category label of the category this node has been added to
     */
    public void addCategory(String catLabel) {
        int index = m_categories.indexOf(catLabel);
        if (index == -1) {
            m_categories.add(catLabel);
        }
    }

    /**
     * Remove a category from the node's context.
     * 
     * @param catLabel
     *            category label of the category this node has been added to
     */
    public void removeCategory(String catLabel) {
        int index = m_categories.indexOf(catLabel);
        if (index != -1) {
            m_categories.remove(catLabel);

        }
    }

    /**
     * Add a node lost service time. Add a losttime entry to the service times
     * list - create a new service time entry in the list
     * 
     * @param t
     *            the time at which service was lost
     */
    public synchronized void nodeLostService(long t) {
        // check if the last element in the times list is 'open'
        // i.e. is waiting for a regained service - if yes,
        // don't add anything

        int listsize = m_svcTimesList.size();
        if (listsize > 0) {
            RTCNodeSvcTime stime = (RTCNodeSvcTime) m_svcTimesList.get(listsize - 1);

            if (stime.getRegainedTime() == -1) {
                // last event was a 'lostService'
                // ignore this event
                return;
            }
        }

        // create a new entry
        RTCNodeSvcTime newStime = new RTCNodeSvcTime(t);
        m_svcTimesList.add(newStime);
    }

    /**
     * Add a node regained service time. Add a regained time entry to the
     * service times list - set the regained time in the last serice time entry
     * in the list
     * 
     * @param t
     *            the time at which node regained service
     */
    public synchronized void nodeRegainedService(long t) {
        int listsize = m_svcTimesList.size();
        if (listsize > 0) {
            RTCNodeSvcTime stime = (RTCNodeSvcTime) m_svcTimesList.get(listsize - 1);

            if (stime.getRegainedTime() != -1) {
                // last event was a 'regainedService'
                // ignore this event
                return;
            }

            stime.setRegainedTime(t);
        }
    }

    /**
     * Return the node ID.
     * 
     * @return the node ID
     */
    public long getNodeID() {
        return m_nodeID;
    }

    /**
     * Return the service name.
     * 
     * @return the service name
     */
    public String getSvcName() {
        return m_svcName;
    }

    /**
     * Return the IP address.
     * 
     * @return the IP address
     */
    public String getIP() {
        return m_ip;
    }

    /**
     * Return the list of service times for this node.
     * 
     * @return the list of service times for this node
     */
    public List<RTCNodeSvcTime> getServiceTimes() {
        return m_svcTimesList;
    }

    /**
     * Check if this node belongs to the category.
     * 
     * @param catLabel
     *            category label
     * 
     * @return true if the node belongs to this category, false otherwise
     */
    public boolean belongsTo(String catLabel) {
        if (m_categories.contains(catLabel)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the list of categories this node belongs to.
     * 
     * @return list of categories for the node.
     */
    public List<String> getCategories() {
        return m_categories;
    }

    /**
     * Get the down time. Return the total outage time for this node in the
     * 'rollingWindow' milliseconds since 'curTime' for the category
     * 
     * @param cat
     *            the category in the context which of which downtime is needed
     * @param curTime
     *            the start time (or current time) from which we go back
     *            rollinWindow interval
     * @param rollingWindow
     *            the window for which downtime is required
     * 
     * @return the total outage time for this node
     */
    public long getDownTime(String cat, long curTime, long rollingWindow) {
        // get the down time for this node in the context of the
        // category
        // if the service is not in 'context', return a negative value
        if (!m_categories.contains(cat))
            return (long) RTCConstants.NODE_NOT_IN_CATEGORY;

        return m_svcTimesList.getDownTime(curTime, rollingWindow);
    }

    /**
     * Get the avaialability. Return the total availability for this node in the
     * last 'rollingWindow' milliseconds since 'curTime' for the category
     * 
     * @param cat
     *            the category in the context which of which availability is
     *            needed
     * @param curTime
     *            the start time (or current time) from which we go back
     *            rollinWindow interval
     * @param rollingWindow
     *            the window for which availability is required
     * 
     * @return the value for this node
     */
    public double getValue(String cat, long curTime, long rollingWindow) {
        double value = 0.0;

        // get the outages in the last 'rollingWindow'
        long outageTime = getDownTime(cat, curTime, rollingWindow);
        if (outageTime < 0)
            return outageTime;

        double dOut = outageTime * 1.0;
        double dRoll = rollingWindow * 1.0;

        value = 100 * (1 - (dOut / dRoll));

        return value;

    }

    /**
     * Return if the service is currently up/down.
     * 
     * @return if the service is currently up/down
     */
    public boolean isServiceCurrentlyDown() {
        int size = m_svcTimesList.size();
        if (size == 0) {
            return false;
        }

        // else get last entry
        RTCNodeSvcTime svctime = (RTCNodeSvcTime) m_svcTimesList.get(size - 1);
        if (svctime.getRegainedTime() != -1) {
            // node has regained service - so service not currently down
            return false;
        }

        // getting here means the service losttime is the one to be returned
        long svclosttime = svctime.getLostTime();
        if (svclosttime == -1) {
            // huh? how is it possible?
            return false;
        }

        return true;
    }

    /**
     * Compare RTCNodes. Overrides the 'equals()' method in the superclass
     * 
     * @return true if all the attributes are equal
     */
    public boolean equals(Object o) {
        if (!(o instanceof RTCNode)) {
            return false;
        }

        RTCNode obj = (RTCNode) o;

        if (m_nodeID == obj.getNodeID() && m_ip.equals(obj.getIP()) && m_svcName.equals(obj.getSvcName())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * String represenatation. Returns a string representation of this object
     * that has the nodeid/ip/servicename details
     * 
     * @return the string representation of this object
     */
    public String toString() {
        String s = "RTCNode\n[\n\t" + "nodeID       = " + m_nodeID + "\n\t" + "IP           = " + m_ip + "\n\t" + "Service      = " + m_svcName + "\n\t" + "\n]\n";
        return s;
    }

}
