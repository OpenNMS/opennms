/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import javax.annotation.Resource;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.snmp.TrapNotification;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * This factory constructs {@link TrapQueueProcessor} instances.
 */
public class TrapQueueProcessorFactory implements InitializingBean {

    /**
     * Whether or not a newSuspect event should be generated with a trap from an
     * unknown IP address
     */
    @Resource(name="newSuspectOnTrap")
    private Boolean m_newSuspectOnTrap;

    /**
     * @return the newSuspectOnTrap
     */
    public Boolean getNewSuspect() {
        return m_newSuspectOnTrap;
    }

    /**
     * @param newSuspectOnTrap the newSuspectOnTrap to set
     */
    public void setNewSuspect(Boolean newSuspectOnTrap) {
        m_newSuspectOnTrap = newSuspectOnTrap;
    }

    /**
     * The event IPC manager to which we send events created from traps.
     */
    private EventIpcManager m_eventManager;

    /**
     * @return the eventMgr
     */
    public EventIpcManager getEventManager() {
        return m_eventManager;
    }

    /**
     * @param eventManager the eventMgr to set
     */
    public void setEventManager(EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }

    /**
     * The event configuration DAO that we use to convert from traps to events.
     */
    @Autowired
    private EventConfDao m_eventConfDao;

    /**
     * The constructor
     */
    public TrapQueueProcessorFactory() {
    }

    public TrapQueueProcessor getInstance(TrapNotification info) {
        TrapQueueProcessor retval = new TrapQueueProcessor();
        retval.setEventConfDao(m_eventConfDao);
        retval.setEventManager(m_eventManager);
        retval.setNewSuspect(m_newSuspectOnTrap);
        retval.setTrapNotification(info);
        retval.afterPropertiesSet();
        return retval;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
        Assert.state(m_eventManager != null, "eventManager must be set");
    }
}
