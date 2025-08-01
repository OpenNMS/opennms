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
package org.opennms.web.svclayer.support;

import java.util.Map;

import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.model.events.EventBuilder;
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
     * @param alarmDao a {@link org.opennms.netmgt.dao.api.AlarmDao} object.
     */
    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }
    
    /**
     * <p>setEventProxy</p>
     *
     * @param eventProxy a {@link org.opennms.netmgt.events.api.EventProxy} object.
     */
    public void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }
    
    /** {@inheritDoc} */
    @Override
    public void closeTicket(Integer alarmId) {
        changeTicket(alarmId, TroubleTicketState.CLOSE_PENDING, EventConstants.TROUBLETICKET_CLOSE_UEI,null);
    }

    /** {@inheritDoc} */
    @Override
    public void createTicket(Integer alarmId, Map<String,String> attributes) {
        changeTicket(alarmId, TroubleTicketState.CREATE_PENDING, EventConstants.TROUBLETICKET_CREATE_UEI,attributes);
    }


    /** {@inheritDoc} */
    @Override
    public void updateTicket(Integer alarmId) {
        changeTicket(alarmId, TroubleTicketState.UPDATE_PENDING, EventConstants.TROUBLETICKET_UPDATE_UEI,null);
    }

    private void changeTicket(Integer alarmId, TroubleTicketState newState, String uei,Map<String,String> attributes) {
        OnmsAlarm alarm = m_alarmDao.get(alarmId);
        alarm.setTTicketState(newState);
        m_alarmDao.saveOrUpdate(alarm);

        EventBuilder bldr = createEventBuilder(uei, alarm, attributes);
        send(bldr.getEvent());
    }

    public static EventBuilder createEventBuilder(String uei, OnmsAlarm alarm, Map<String, String> attributes) {
        EventBuilder bldr = new EventBuilder(uei, "AlarmUI");
        bldr.setNode(alarm.getNode());
        bldr.setInterface(alarm.getIpAddr());
        bldr.setService(alarm.getServiceType() == null ? null : alarm.getServiceType().getName());
        bldr.addParam(EventConstants.PARM_ALARM_UEI, alarm.getUei());
        if (attributes == null || !attributes.containsKey(EventConstants.PARM_USER))
        	bldr.addParam(EventConstants.PARM_USER, alarm.getAlarmAckUser());
        bldr.addParam(EventConstants.PARM_ALARM_ID, alarm.getId());
        if (alarm.getTTicketId() != null) {
            bldr.addParam(EventConstants.PARM_TROUBLE_TICKET, alarm.getTTicketId());
        }
        if (attributes != null) {
        	for (Map.Entry<String, String> attribute: attributes.entrySet()) {
        		bldr.addParam(attribute.getKey(), attribute.getValue());
        	}
        }
        return bldr;
    }

    private void send(Event e) {
        try {
            m_eventProxy.send(e);
        } catch (EventProxyException e1) {
            throw new DataSourceLookupFailureException("Unable to send event to eventd", e1);
        }
    }
}
