package org.opennms.web.svclayer.support;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.svclayer.TroubleTicketProxy;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

public class DefaultTroubleTicketProxy implements TroubleTicketProxy {

    private AlarmDao m_alarmDao;
    private EventProxy m_eventProxy;

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }
    
    public void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }
    
    public void closeTicket(Integer alarmId) {
        changeTicket(alarmId, TroubleTicketState.CLOSE_PENDING, EventConstants.TROUBLETICKET_CLOSE_UEI);
    }

    public void createTicket(Integer alarmId) {
        changeTicket(alarmId, TroubleTicketState.CREATE_PENDING, EventConstants.TROUBLETICKET_CREATE_UEI);
    }


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
