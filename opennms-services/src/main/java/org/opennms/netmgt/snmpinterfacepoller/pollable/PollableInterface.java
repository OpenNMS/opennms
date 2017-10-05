/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * Represents an SNMP PollableInterface
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class PollableInterface {

    private int m_nodeid;
    
    private String m_ipaddress;
    
    private String m_netMask;

    private PollableNetwork m_parent;
    
    private Map<String, PollableSnmpInterface> m_pollablesnmpinterface;

    private String m_packageName;
    
    private boolean polling = true;
    
    /**
     * <P>
     * Initialize the service monitor.
     * </P>
     *
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     */
    protected void initialize() {
        // Initialize the SnmpPeerFactory
        //
        try {
            SnmpPeerFactory.init();
        } catch (UnknownHostException ex) {
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }
    
    /**
     * <p>getNodeid</p>
     *
     * @return a int.
     */
    public int getNodeid() {
        return m_nodeid;
    }

    /**
     * <p>setNodeid</p>
     *
     * @param nodeid a int.
     */
    public void setNodeid(int nodeid) {
        m_nodeid = nodeid;
    }

    /**
     * <p>getIpaddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpaddress() {
        return m_ipaddress;
    }

    /**
     * <p>setIpaddress</p>
     *
     * @param ipaddress a {@link java.lang.String} object.
     */
    public void setIpaddress(String ipaddress) {
        m_ipaddress = ipaddress;
    }

    public String getNetMask() {
        return m_netMask;
    }

    public void setNetMask(final String netMask) {
        m_netMask = netMask;
    }

    /**
     * <p>Constructor for PollableInterface.</p>
     *
     * @param parent a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollableNetwork} object.
     */
    public PollableInterface(PollableNetwork parent) {
        m_parent = parent;
        m_pollablesnmpinterface = new HashMap<String,PollableSnmpInterface>();
    }

  /**
   * <p>createPollableSnmpInterface</p>
   *
   * @param name a {@link java.lang.String} object.
   * @param criteria a {@link java.lang.String} object.
   * @param hasPort a boolean.
   * @param port a int.
   * @param hasTimeout a boolean.
   * @param timeout a int.
   * @param hasRetries a boolean.
   * @param retries a int.
   * @param hasMaxVarsPerPdu a boolean.
   * @param maxVarsPerPdu a int.
   * @return a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterface} object.
   */
  public PollableSnmpInterface createPollableSnmpInterface(String name, String criteria, boolean hasPort, 
          int port, boolean hasTimeout, int timeout, boolean hasRetries, int retries, 
          boolean hasMaxVarsPerPdu,int maxVarsPerPdu) {

        PollableSnmpInterface iface = new PollableSnmpInterface(this);
        iface.setName(name);
        iface.setCriteria(criteria);
        InetAddress ipAddr = null;
        ipAddr = InetAddressUtils.addr(getIpaddress());
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipAddr);
        if (hasPort) agentConfig.setPort(port);
        if (hasTimeout) agentConfig.setTimeout(timeout);
        if (hasRetries) agentConfig.setRetries(retries);
        if (hasMaxVarsPerPdu) agentConfig.setMaxVarsPerPdu(maxVarsPerPdu);

        iface.setAgentConfig(agentConfig);
               
        m_pollablesnmpinterface.put(name,iface);
        return iface;
    }
    
    /**
     * <p>refresh</p>
     */
    protected void refresh() {
        for ( PollableSnmpInterface pi: getSnmpinterfacepollableNodes().values()){
            pi.setSnmpinterfaces(getContext().get(getNodeid(), pi.getCriteria()));
        }
    }
    
    /**
     * <p>suspend</p>
     */
    protected void suspend() {
          polling = false;  
    }

    /**
     * <p>activate</p>
     */
    protected void activate() {
        polling = true;
    }

    /**
     * <p>getSnmpinterfacepollableNodes</p>
     *
     * @return a {@link java.util.HashMap} object.
     */
    public Map<String,PollableSnmpInterface> getSnmpinterfacepollableNodes() {
        return Collections.unmodifiableMap(m_pollablesnmpinterface);
    }


    /**
     * <p>polling</p>
     *
     * @return a boolean.
     */
    public boolean polling() {
        return polling;
    }
    

    /**
     * <p>delete</p>
     */
    protected void delete() {
        for ( PollableSnmpInterface node: getSnmpinterfacepollableNodes().values()){
            node.delete();
        }
    }

    /**
     * <p>getParent</p>
     *
     * @return a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollableNetwork} object.
     */
    public PollableNetwork getParent() {
        return m_parent;
    }

    /**
     * <p>setParent</p>
     *
     * @param parent a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollableNetwork} object.
     */
    public void setParent(PollableNetwork parent) {
        m_parent = parent;
    }
    
    /**
     * <p>getContext</p>
     *
     * @return a {@link org.opennms.netmgt.snmpinterfacepoller.pollable.PollContext} object.
     */
    public PollContext getContext() {
        return getParent().getContext();
    }

    /**
     * <p>getPackageName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPackageName() {
        return m_packageName;
    }

    /**
     * <p>setPackageName</p>
     *
     * @param packageName a {@link java.lang.String} object.
     */
    public void setPackageName(String packageName) {
        m_packageName = packageName;
    }

}
