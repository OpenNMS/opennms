/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: May 1, 2007
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
package org.opennms.web.svclayer.support;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.svclayer.TroubleTicketProxy;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

/**
 * <p>DefaultTroubleTicketProxy class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultTroubleTicketProxy implements TroubleTicketProxy {

    private AlarmDao m_alarmDao;
    private EventProxy m_eventProxy;

    /**
     * <p>setAlarmDao</p>
     *
     * @param alarmDao a {@link org.opennms.netmgt.dao.AlarmDao} object.
     */
    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }
    
    /**
     * <p>setEventProxy</p>
     *
     * @param eventProxy a {@link org.opennms.netmgt.model.events.EventProxy} object.
     */
    public void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }
    
    /** {@inheritDoc} */
    public void closeTicket(Integer alarmId) {
        changeTicket(alarmId, TroubleTicketState.CLOSE_PENDING, EventConstants.TROUBLETICKET_CLOSE_UEI);
    }

    /** {@inheritDoc} */
    public void createTicket(Integer alarmId) {
        changeTicket(alarmId, TroubleTicketState.CREATE_PENDING, EventConstants.TROUBLETICKET_CREATE_UEI);
    }


    /** {@inheritDoc} */
    public void updateTicket(Integer alarmId) {
        changeTicket(alarmId, TroubleTicketState.UPDATE_PENDING, EventConstants.TROUBLETICKET_UPDATE_UEI);
    }

    private void changeTicket(Integer alarmId, TroubleTicketState newState, String uei) {
        OnmsAlarm alarm = m_alarmDao.get(alarmId);
        alarm.setTTicketState(newState);
        m_alarmDao.saveOrUpdate(alarm);
        
        EventBuilder bldr = new EventBuilder(uei, "AlarmUI");
        bldr.setNode(alarm.getNode());
        bldr.setInterface(alarm.getIpAddr());
        bldr.setService(alarm.getServiceType() == null ? null : alarm.getServiceType().getName());
        bldr.addParam(EventConstants.PARM_ALARM_UEI, alarm.getUei());
        bldr.addParam(EventConstants.PARM_USER, alarm.getAlarmAckUser());
        bldr.addParam(EventConstants.PARM_ALARM_ID, alarm.getId());
        if (alarm.getTTicketId() != null) {
            bldr.addParam(EventConstants.PARM_TROUBLE_TICKET, alarm.getTTicketId());
        }
        send(bldr.getEvent());
    }

    private void send(Event e) {
        try {
            m_eventProxy.send(e);
        } catch (EventProxyException e1) {
            throw new DataSourceLookupFailureException("Unable to send event to eventd", e1);
        }
    }
}
