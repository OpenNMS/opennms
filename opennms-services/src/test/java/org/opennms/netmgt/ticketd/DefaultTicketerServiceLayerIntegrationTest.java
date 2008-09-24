/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: May 22, 2007
 *
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
package org.opennms.netmgt.ticketd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.netmgt.dao.AbstractTransactionalDaoTestCase;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultTicketerServiceLayerIntegrationTest extends
        AbstractTransactionalDaoTestCase {

    private TicketerServiceLayer m_ticketerServiceLayer;
    private TestTicketerPlugin m_ticketerPlugin;
    
    
    @Override
    protected void setUpConfiguration() {
        
        super.setUpConfiguration();

        System.setProperty("opennms.ticketer.plugin", TestTicketerPlugin.class.getName());
        
        // TODO I hate this
        EventIpcManagerFactory.setIpcManager(new MockEventIpcManager());
        
    }

    @Override
    protected String[] getConfigLocations() {
        
        String[] configs = new String[] {
            "classpath:/META-INF/opennms/applicationContext-daemon.xml",
            "classpath:/META-INF/opennms/applicationContext-troubleTicketer.xml",
            "classpath:/org/opennms/netmgt/ticketd/applicationContext-configOverride.xml",
        };
        
        List<String> configLocation = new ArrayList<String>();
        
        configLocation.addAll(Arrays.asList(super.getConfigLocations()));
        configLocation.addAll(Arrays.asList(configs));
        
        return configLocation.toArray(new String[configLocation.size()]);
    }
    
    public void setTicketerPlugin(TestTicketerPlugin ticketerPlugin) {
        m_ticketerPlugin = ticketerPlugin;
    }
    
    public void setTicketerServiceLayer(TicketerServiceLayer ticketerServiceLayer) {
        m_ticketerServiceLayer = ticketerServiceLayer;
    }
    
    public void testWire() {
        assertNotNull(m_ticketerServiceLayer);
        assertNotNull(m_ticketerPlugin);
        
        final int alarmId = 1;
        
        OnmsAlarm alarm = getAlarmDao().get(alarmId);
        assertNull(alarm.getTTicketState());
        assertNull(alarm.getTTicketId());
        
        m_ticketerServiceLayer.createTicketForAlarm(alarmId);
        
        getAlarmDao().flush();
        
        alarm = getAlarmDao().get(alarmId);
        assertEquals(TroubleTicketState.OPEN, alarm.getTTicketState());
        assertNotNull(alarm.getTTicketId());
        assertEquals("testId", alarm.getTTicketId());
        
        m_ticketerServiceLayer.updateTicketForAlarm(alarm.getId(), alarm.getTTicketId());

        getAlarmDao().flush();

        alarm = getAlarmDao().get(alarmId);
        assertEquals(TroubleTicketState.OPEN, alarm.getTTicketState());
        
        m_ticketerServiceLayer.closeTicketForAlarm(alarmId, alarm.getTTicketId());
        
        getAlarmDao().flush();

        alarm = getAlarmDao().get(alarmId);
        assertEquals(TroubleTicketState.CLOSED, alarm.getTTicketState());
        
    }
    
    public static class TestTicketerPlugin implements Plugin {
        
        public Ticket get(String ticketId) {
            Ticket ticket = new Ticket();
            ticket.setId(ticketId);
            return ticket;
        }

        public void saveOrUpdate(Ticket ticket) {
            ticket.setId("testId");
        }
        
    }

}
