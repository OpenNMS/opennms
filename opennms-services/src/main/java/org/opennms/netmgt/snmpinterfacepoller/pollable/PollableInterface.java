/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * Represents an Snmp PollableInterface
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class PollableInterface {

    private int m_nodeid;
    
    private String m_ipaddress;
    
    private PollableNetwork m_parent;
    
    private HashMap<String, PollableSnmpInterface> m_pollablesnmpinterface;

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
        } catch (MarshalException ex) {
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
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
		try {
			ipAddr = InetAddress.getByName(getIpaddress());
		} catch (final UnknownHostException e) {
		    LogUtils.debugf(this, e, "unable to get host for IP address %s", getIpaddress());
		}
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
            getContext().updatePollStatus(getNodeid(), pi.getCriteria(), "P");
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
    public HashMap<String,PollableSnmpInterface> getSnmpinterfacepollableNodes() {
        return m_pollablesnmpinterface;
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
