/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class PollableNetwork {
    
    private final Map<String, PollableInterface>m_members = new HashMap<String, PollableInterface>();
    private final Map<Integer,String> m_node = new HashMap<Integer, String>();

    private PollContext m_context;
    
    public PollableNetwork(PollContext context) {
        m_context = context;
    }
    
    public PollableInterface create(int nodeid, String ipaddress, String packageName) {
        PollableInterface nodeGroup = new PollableInterface(this);
        nodeGroup.setNodeid(nodeid);
        nodeGroup.setIpaddress(ipaddress);
        nodeGroup.setPackageName(packageName);
        nodeGroup.initialize();
        m_members.put(nodeGroup.getIpaddress(), nodeGroup);
        m_node.put(new Integer(nodeGroup.getNodeid()), nodeGroup.getIpaddress());
        return nodeGroup;
    }
    
    public void deleteAll() {
        Iterator<PollableInterface> ite = m_members.values().iterator();
        while (ite.hasNext()) {
            ite.next().delete();
        }
        m_members.clear();
        m_node.clear();
    }
    
    public void delete(String ipaddress) {
        PollableInterface pi = getInterface(ipaddress);
        if (pi != null) {
            m_members.remove(ipaddress);
            m_node.remove(new Integer(pi.getNodeid()));
            pi.delete();
        }
    }
    
    public void refresh(int nodeid) {
        String ipaddress = getIp(nodeid);
        if (ipaddress != null ) getInterface(ipaddress).refresh();
    }
    
    public void suspend(String ipaddress) {
        PollableInterface pi = getInterface(ipaddress);
        if (pi != null) pi.suspend();
    }
    
    public void activate(String ipaddress) {
        PollableInterface pi = getInterface(ipaddress);
        if (pi != null) pi.activate();
    }
    
    public String getIp(int nodeid) {
        return m_node.get(new Integer(nodeid));
    }
    
    private PollableInterface getInterface(String ipaddress) {
        if ( m_members.containsKey(ipaddress)) return m_members.get(ipaddress);
        return null;
    }

    public int getNodeid(String ipaddress) {
        Iterator<PollableInterface> ite = m_members.values().iterator();
        while (ite.hasNext()) {
            PollableInterface pi = ite.next();
            if (pi.getIpaddress().equals(ipaddress)) return pi.getNodeid();
        }
        return 0;
    }

    public PollContext getContext() {
        return m_context;
    }

    public void setContext(PollContext context) {
        m_context = context;
    }
}
