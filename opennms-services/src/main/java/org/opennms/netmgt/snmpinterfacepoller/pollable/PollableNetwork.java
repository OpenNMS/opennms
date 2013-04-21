/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.scheduler.Schedule;

/**
 * Represents an SNMP PollableNetwork
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class PollableNetwork {
    
    private final Map<String, PollableInterface>m_members = new HashMap<String, PollableInterface>();
    private final Map<Integer,String> m_node = new HashMap<Integer, String>();

    private PollContext m_context;
    
    /**
     * <p>Constructor for PollableNetwork.</p>
     *
     * @param context a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollContext} object.
     */
    public PollableNetwork(PollContext context) {
        m_context = context;
    }
    
    /**
     * <p>create</p>
     *
     * @param nodeid a int.
     * @param ipaddress a {@link java.lang.String} object.
     * @param packageName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollableInterface} object.
     */
    public PollableInterface create(int nodeid, String ipaddress, String packageName) {
        PollableInterface nodeGroup = new PollableInterface(this);
        nodeGroup.setNodeid(nodeid);
        nodeGroup.setIpaddress(ipaddress);
        nodeGroup.setPackageName(packageName);
        nodeGroup.initialize();
        m_members.put(nodeGroup.getIpaddress(), nodeGroup);
        m_node.put(Integer.valueOf(nodeGroup.getNodeid()), nodeGroup.getIpaddress());
        return nodeGroup;
    }
    
    /**
     * <p>schedule</p>
     *
     * @param node a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterface} object.
     * @param criteria a {@link java.lang.String} object.
     * @param interval a long.
     * @param scheduler a {@link org.opennms.netmgt.scheduler.Scheduler} object.
     */
    public void schedule(PollableSnmpInterface node, long interval, org.opennms.netmgt.scheduler.Scheduler scheduler) {

        

        PollableSnmpInterfaceConfig nodeconfig = new PollableSnmpInterfaceConfig(scheduler,interval);

        node.setSnmppollableconfig(nodeconfig);

        synchronized(node) {
            if (node.getSchedule() == null) {
                Schedule schedule = new Schedule(node, nodeconfig, scheduler);
                node.setSchedule(schedule);
            }
        }
        
            node.schedule();
    }

    /**
     * <p>deleteAll</p>
     */
    public void deleteAll() {
        for (PollableInterface pi: m_members.values()) {
            pi.delete();
        }
        m_members.clear();
        m_node.clear();
    }
    
    /**
     * <p>delete</p>
     *
     * @param ipaddress a {@link java.lang.String} object.
     */
    public void delete(String ipaddress) {
        PollableInterface pi = getInterface(ipaddress);
        if (pi != null) {
            m_members.remove(ipaddress);
            m_node.remove(Integer.valueOf(pi.getNodeid()));
            pi.delete();
        }
    }
    
    /**
     * <p>delete</p>
     *
     * @param nodeid a int.
     */
    public void delete(int nodeid) {
        delete(getIp(nodeid));
    }
    
    /**
     * <p>refresh</p>
     *
     * @param nodeid a int.
     */
    public void refresh(int nodeid) {
        String ipaddress = getIp(nodeid);
        if (ipaddress != null ) {
            getInterface(ipaddress).refresh();
        }
    }
    
    /**
     * <p>suspend</p>
     *
     * @param ipaddress a {@link java.lang.String} object.
     */
    public void suspend(String ipaddress) {
        PollableInterface pi = getInterface(ipaddress);
        if (pi != null) pi.suspend();
    }
    
    /**
     * <p>activate</p>
     *
     * @param ipaddress a {@link java.lang.String} object.
     */
    public void activate(String ipaddress) {
        PollableInterface pi = getInterface(ipaddress);
        if (pi != null) pi.activate();
    }
    /**
     * <p>suspend</p>
     *
     * @param nodeid a int.
     */
    public void suspend(int nodeid) {
        String ipprimary = getIp(nodeid);
        if (ipprimary != null) suspend(ipprimary);
    }
    
    /**
     * <p>activate</p>
     *
     * @param nodeid a int.
     */
    public void activate(int nodeid) {
        String ipprimary = getIp(nodeid);
        if (ipprimary != null) activate(ipprimary);
    }
    
    /**
     * <p>getIp</p>
     *
     * @param nodeid a int.
     * @return a {@link java.lang.String} object.
     */
    public String getIp(int nodeid) {
        return m_node.get(Integer.valueOf(nodeid));
    }
    
    private PollableInterface getInterface(String ipaddress) {
        if ( m_members.containsKey(ipaddress)) return m_members.get(ipaddress);
        return null;
    }
    
    /**
     * <p>hasPollableInterface</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasPollableInterface(String ipaddr) {
        return (m_members.containsKey(ipaddr));
    }

    /**
     * <p>getContext</p>
     *
     * @return a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollContext} object.
     */
    public PollContext getContext() {
        return m_context;
    }

    /**
     * <p>setContext</p>
     *
     * @param context a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollContext} object.
     */
    public void setContext(PollContext context) {
        m_context = context;
    }
}
