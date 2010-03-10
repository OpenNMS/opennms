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
package org.opennms.netmgt.snmpinterfacepoller;


import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollContext;
import org.opennms.netmgt.utils.Updater;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a DefaultPollContext 
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class DefaultPollContext implements PollContext {
    
    private volatile EventIpcManager m_eventManager;
    private volatile String m_name;
    private volatile String m_localHostName;
    private SnmpInterfaceDao m_snmpInterfaceDao;
    private DataSource m_dataSource;

    private String m_serviceName="SNMP";

    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    public EventIpcManager getEventManager() {
        return m_eventManager;
    }
    
    public void setEventManager(EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }
    
    public void setLocalHostName(String localHostName) {
        m_localHostName = localHostName;
    }
    
    public String getLocalHostName() {
        return m_localHostName;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public DataSource getDataSource() {
        return m_dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#getCriticalServiceName()
     */
    public String getServiceName() {
        return m_serviceName;
    }
    
    public void setServiceName(String serviceName) {
        m_serviceName=serviceName;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#sendEvent(org.opennms.netmgt.xml.event.Event)
     */
    public void sendEvent(Event event) {
        getEventManager().sendNow(event);
    }

    Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#createEvent(java.lang.String, int, java.net.InetAddress, java.lang.String, java.util.Date)
     */
    public Event createEvent(String uei, int nodeId, String address, Date date, OnmsSnmpInterface snmpinterface) {
        
            log().debug("createEvent: uei = " + uei + " nodeid = " + nodeId + " date = " + date);
        
        EventBuilder bldr = new EventBuilder(uei, this.getName(), date);
        bldr.setNodeid(nodeId);
        if (address != null) {
            bldr.setInterface(address);
        }
        bldr.setService(getServiceName());

        bldr.setHost(this.getLocalHostName());
        bldr.setField("ifIndex", snmpinterface.getIfIndex().toString());

        bldr.addParam(EventConstants.PARM_SNMP_INTERFACE_IFINDEX, snmpinterface.getIfIndex().toString());
        bldr.addParam(EventConstants.PARM_SNMP_INTERFACE_IP, snmpinterface.getIpAddress());
        if (snmpinterface.getIfName() != null) bldr.addParam(EventConstants.PARM_SNMP_INTERFACE_NAME, snmpinterface.getIfName());
        if (snmpinterface.getIfDescr() != null) bldr.addParam(EventConstants.PARM_SNMP_INTERFACE_DESC, snmpinterface.getIfDescr());
        if (snmpinterface.getIfAlias() != null) bldr.addParam(EventConstants.PARM_SNMP_INTERFACE_ALIAS, snmpinterface.getIfAlias());
        if (snmpinterface.getNetMask() != null) bldr.addParam(EventConstants.PARM_SNMP_INTERFACE_MASK, snmpinterface.getNetMask());        
        
        return bldr.getEvent();
    }

    public List<OnmsSnmpInterface> get(int nodeId, String criteria) {
        final OnmsCriteria onmsCriteria = new OnmsCriteria(OnmsSnmpInterface.class);
        onmsCriteria.add(Restrictions.sqlRestriction(criteria + " and nodeid = " + nodeId));
        return getSnmpInterfaceDao().findMatching(onmsCriteria);

    }
        
    public void update(OnmsSnmpInterface snmpinterface) {
        getSnmpInterfaceDao().update(snmpinterface);
    }

    public void updatePollStatus(int nodeId, String criteria, String status) {
        String sql = "update snmpinterface set snmppoll = ? where nodeid = ? and " + criteria;
        
        Updater updater = new Updater(m_dataSource, sql);
        updater.execute(status,new Integer(nodeId));  
    }
    
    public void updatePollStatus(int nodeId, String status) {
        String sql = "update snmpinterface set snmppoll = ? where nodeid = ? ";
        
        Updater updater = new Updater(m_dataSource, sql);
        updater.execute(status,new Integer(nodeId));  
        
    }

    public void updatePollStatus(String status) {
        final String sql = "update snmpinterface set snmppoll = ? ";
        
        Updater updater = new Updater(m_dataSource, sql);
        updater.execute(status);  
    }

}
