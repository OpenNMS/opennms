/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import javax.annotation.Resource;

import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.events.api.EventForwarder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * This factory constructs {@link TrapQueueProcessor} instances.
 */
public class TrapQueueProcessorFactory {

    /**
     * Whether or not a newSuspect event should be generated with a trap from an
     * unknown IP address
     */
    @Resource(name="newSuspectOnTrap")
    private Boolean m_newSuspectOnTrap;

    @Autowired
    private EventConfDao m_eventConfDao;

    @Autowired
    @Qualifier("eventIpcManager")
    private EventForwarder m_eventForwarder;

    @Autowired
    private InterfaceToNodeCache m_interfaceToNodeCache;

    public TrapQueueProcessor getInstance() {
        TrapQueueProcessor processor = new TrapQueueProcessor();
        processor.setEventConfDao(m_eventConfDao);
        processor.setEventForwarder(m_eventForwarder);
        processor.setNewSuspect(m_newSuspectOnTrap);
        processor.setInterfaceToNodeCache(m_interfaceToNodeCache);
        processor.afterPropertiesSet();
        return processor;
    }

    void setNewSuspect(boolean newSuspectOnTrap) {
        m_newSuspectOnTrap = newSuspectOnTrap;
    }
}
