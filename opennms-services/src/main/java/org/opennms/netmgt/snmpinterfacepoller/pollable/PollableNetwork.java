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
    public PollableInterface create(int nodeid, String ipaddress, String netMask, String packageName) {
        PollableInterface nodeGroup = new PollableInterface(this);
        nodeGroup.setNodeid(nodeid);
        nodeGroup.setIpaddress(ipaddress);
        nodeGroup.setNetMask(netMask);
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
    public void refresh(final int nodeid) {
        String ipaddress = getIp(nodeid);
        if (ipaddress != null) {
            final var iface = getInterface(ipaddress);
            if (iface != null) {
                iface.refresh();
            }
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
