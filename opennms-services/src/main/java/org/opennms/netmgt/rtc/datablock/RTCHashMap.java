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
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Orignal code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The RTCHashMap has either a nodeid or a nodeid/ip as key and provides
 * convenience methods to add and remove 'RTCNodes' with these values - each key
 * points to a list of 'RTCNode's
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @version $Id: $
 */
public class RTCHashMap {
	
	Map m_map;
	
    /**
     * Default constructor
     */
    public RTCHashMap() {
    	m_map = new HashMap();
    }

    /**
     * constructor
     *
     * @param initialCapacity a int.
     */
    public RTCHashMap(int initialCapacity) {
        m_map = new HashMap(initialCapacity);
    }

    /**
     * constructor
     *
     * @param initialCapacity a int.
     * @param loadFactor a float.
     */
    public RTCHashMap(int initialCapacity, float loadFactor) {
    	m_map = new HashMap(initialCapacity, loadFactor);
    }
    
    private void put(Object key, Object value) {
    	m_map.put(key, value);
    }
    
    private Object get(Object key) {
    	return m_map.get(key);
    }
    
    private Object remove(Object key) {
    	return m_map.remove(key);
    }
    
    private List getNodeIDs() {
    	List nodes = new LinkedList();
    	for (Iterator it = m_map.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			if (key instanceof Long)
				nodes.add(key);
		}
    	return nodes;
    }
    
    /**
     * Add the node with nodeid as key
     * 
     * @param nodeid
     *            the nodeid
     * @param rtcN
     *            the RTCNode to add
     */
    private void add(long nodeid, RTCNode rtcN) {
        Long key = new Long(nodeid);

        List nodesList = (List) get(key);
        if (nodesList != null) {
            nodesList.add(rtcN);
        } else {
            // add current node to list
            nodesList = new ArrayList();
            nodesList.add(rtcN);

            // add list to map
            put(key, nodesList);
        }
    }

    /**
     * Add the rtc node with nodeid and ip as key
     * 
     * @param nodeid
     *            the nodeid
     * @param ip
     *            the ip
     * @param rtcN
     *            the RTCNode to add
     */
    private void add(long nodeid, String ip, RTCNode rtcN) {
        String key = Long.toString(nodeid) + ip;

        List nodesList = (List) get(key);
        if (nodesList != null) {
            nodesList.add(rtcN);
        } else {
            // add current node to list
            nodesList = new ArrayList();
            nodesList.add(rtcN);

            // add list to map
            put(key, nodesList);
        }
    }
    
    private void add(long nodeid, String ip, String svcName, RTCNode rtcN) {
    	put(new RTCNodeKey(nodeid, ip, svcName), rtcN);
    }
    
    /**
     * Add an rtc node
     *
     * @param rtcN the rtcNode to add
     */
    public void add(RTCNode rtcN) {
    	add(rtcN.getNodeID(), rtcN);
    	add(rtcN.getNodeID(), rtcN.getIP(), rtcN);
    	add(rtcN.getNodeID(), rtcN.getIP(), rtcN.getSvcName(), rtcN);
    }
    
    /**
     * <p>delete</p>
     *
     * @param rtcN a {@link org.opennms.netmgt.rtc.datablock.RTCNode} object.
     */
    public void delete(RTCNode rtcN) {
    	delete(rtcN.getNodeID(), rtcN);
    	delete(rtcN.getNodeID(), rtcN.getIP(), rtcN);
    	delete(rtcN.getNodeID(), rtcN.getIP(), rtcN.getSvcName(), rtcN);
    }
    
    
    /**
     * Delete the node from list with nodeid as key
     * 
     * @param nodeid
     *            the nodeid
     * @param rtcN
     *            the RTCNode to delete
     */
    private void delete(long nodeid, RTCNode rtcN) {
        Long key = new Long(nodeid);

        List nodesList = (List) get(key);
        if (nodesList != null) {
            nodesList.remove(rtcN);
        }
    }

    /**
     * Delete the rtc node from list with nodeid and ip as key
     * 
     * @param nodeid
     *            the nodeid
     * @param ip
     *            the ip
     * @param rtcN
     *            the RTCNode to add
     */
    private void delete(long nodeid, String ip, RTCNode rtcN) {
        String key = Long.toString(nodeid) + ip;

        List nodesList = (List) get(key);
        if (nodesList != null) {
            nodesList.remove(rtcN);
        }
    }
    
    private void delete(long nodeid, String ip, String svcName, RTCNode rtcN) {
    	RTCNodeKey key = new RTCNodeKey(nodeid, ip, svcName);
    	remove(key);
    }

    /**
     * Check if this IP has already been validated for this category
     *
     * @param nodeid
     *            the node id whose interface is to be validated
     * @param ip
     *            the ip to be validated
     * @param catLabel
     *            the category whose rule this ip is to pass
     * @return true if ip has already been validated, false otherwise
     */
    public boolean isIpValidated(long nodeid, String ip, String catLabel) {
        List nodesList = getRTCNodes(nodeid, ip);
        Iterator iter = nodesList.iterator();
        while (iter.hasNext()) {
            RTCNode node = (RTCNode) iter.next();
            if (node.belongsTo(catLabel)) {
                return true;
            }
        }

        return false;

    }

    /**
     * Get the value (uptime) for a category in the last 'rollingWindow'
     * starting at current time
     *
     * @param catLabel
     *            the category to which the node should belong to
     * @param curTime
     *            the current time
     * @param rollingWindow
     *            the window for which value is to be calculated
     * @return the value(uptime) for the node
     */
    public double getValue(String catLabel, long curTime, long rollingWindow) {
        // the value (uptime)
        double value = 0.0;

        // total outage time
        long outageTime = 0;

        // number of entries for this node
        int count = 0;

        // downtime for a node
        long downTime = 0;

        // get all nodes in the hashtable
        List nodes = getNodeIDs();
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            // get only values of nodeids
            Long key = (Long)it.next();
            List valList = getRTCNodes(key.longValue());
            if (valList == null || valList.size() == 0)
                continue;

            Iterator valIter = valList.iterator();
            while (valIter.hasNext()) {
                RTCNode node = (RTCNode) valIter.next();
                downTime = node.getDownTime(catLabel, curTime, rollingWindow);
                if (downTime < 0)
                // node does not belong to category
                // or RTCConstants.SERVICE_NOT_FOUND_VALUE
                // or node / interface / service unmanaged
                {
                    continue;
                }

                outageTime += downTime;

                count++;

            }

        }

        double dOut = outageTime * 1.0;
        double dRoll = rollingWindow * 1.0;

        if (count > 0) {
            value = 100 * (1 - (dOut / (dRoll * count)));
        } else {
            value = 100.0;
        }

        return value;
    }

    /**
     * Get the value (uptime) for the a node that belongs to the category in the
     * last 'rollingWindow' starting at current time
     *
     * @param nodeid
     *            the node for which value is to be calculated
     * @param catLabel
     *            the category to which the node should belong to
     * @param curTime
     *            the current time
     * @param rollingWindow
     *            the window for which value is to be calculated
     * @return the value(uptime) for the node
     */
    public double getValue(long nodeid, String catLabel, long curTime, long rollingWindow) {
        // the value (uptime)
        double value = 0.0;

        // total outage time
        long outageTime = 0;

        // number of entries for this node
        int count = 0;

        // downtime for a node
        long downTime = 0;

        // get nodeslist
        List nodesList = getRTCNodes(nodeid);
        Iterator iter = nodesList.iterator();
        while (iter.hasNext()) {
            RTCNode node = (RTCNode) iter.next();

            if (node.getNodeID() == nodeid) {
                downTime = node.getDownTime(catLabel, curTime, rollingWindow);
                if (downTime < 0)
                // node does not belong to category
                // or RTCConstants.SERVICE_NOT_FOUND_VALUE
                // or node / interface / service unmanaged
                {
                    continue;
                }

                outageTime += downTime;

                count++;

            }
        }

        double dOut = outageTime * 1.0;
        double dRoll = rollingWindow * 1.0;

        if (count > 0) {
            value = 100 * (1 - (dOut / (dRoll * count)));
        } else {
            value = 100.0;
        }

        return value;
    }

    /**
     * Get the count of services for a node in the context of the the specified
     * category
     *
     * @param nodeid
     *            the node for which servicecount is needed
     * @param catLabel
     *            the category to which the node should belong to
     * @return the service count for the nodeid in the context of the specfied
     *         category
     */
    public int getServiceCount(long nodeid, String catLabel) {
        // the count
        int count = 0;

        // get nodeslist
        List nodesList = getRTCNodes(nodeid);
        Iterator iter = nodesList.iterator();
        while (iter.hasNext()) {
            RTCNode node = (RTCNode) iter.next();

            if (node.belongsTo(catLabel))
                count++;

        }

        return count;
    }

    /**
     * Get the count of services currently down for a node in the context of the
     * the specified category
     *
     * @param nodeid
     *            the node for which servicecount is needed
     * @param catLabel
     *            the category to which the node should belong to
     * @return the service down count for the nodeid in the context of the
     *         specfied category
     */
    public int getServiceDownCount(long nodeid, String catLabel) {
        // the count
        int count = 0;

        // get nodeslist
        List nodesList = getRTCNodes(nodeid);
        Iterator iter = nodesList.iterator();
        while (iter.hasNext()) {
            RTCNode node = (RTCNode) iter.next();

            if (node.belongsTo(catLabel) && node.isServiceCurrentlyDown()) {
                count++;
            }
        }

        return count;
    }

	/**
	 * <p>getRTCNode</p>
	 *
	 * @param key a {@link org.opennms.netmgt.rtc.datablock.RTCNodeKey} object.
	 * @return a {@link org.opennms.netmgt.rtc.datablock.RTCNode} object.
	 */
	public RTCNode getRTCNode(RTCNodeKey key) {
		return (RTCNode)get(key);
	}
	
	/**
	 * <p>getRTCNode</p>
	 *
	 * @param nodeid a long.
	 * @param ipaddr a {@link java.lang.String} object.
	 * @param svcname a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.rtc.datablock.RTCNode} object.
	 */
	public RTCNode getRTCNode(long nodeid, String ipaddr, String svcname) {
		return getRTCNode(new RTCNodeKey(nodeid, ipaddr, svcname));
	}
	
	/**
	 * <p>getRTCNodes</p>
	 *
	 * @param nodeid a long.
	 * @return a {@link java.util.List} object.
	 */
	public List getRTCNodes(long nodeid) {
		Long key = new Long(nodeid);
		List nodes = (List)get(key);
		if (nodes == null) return Collections.EMPTY_LIST;
		return Collections.unmodifiableList(nodes); 
	}
	
	/**
	 * <p>getRTCNodes</p>
	 *
	 * @param nodeid a long.
	 * @param ip a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	public List getRTCNodes(long nodeid, String ip) {
		String key = Long.toString(nodeid)+ip;
		List nodes = (List)get(key);
		if (nodes == null) return Collections.EMPTY_LIST;
		return Collections.unmodifiableList(nodes);
	}

	/**
	 * <p>deleteNode</p>
	 *
	 * @param nodeid a long.
	 */
	public void deleteNode(long nodeid) {
		List nodeList = new ArrayList(getRTCNodes(nodeid));
		for (Iterator it = nodeList.iterator(); it.hasNext();) {
			RTCNode rtcN = (RTCNode) it.next();
			delete(rtcN);
		}
		
	}

}
