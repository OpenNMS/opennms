package org.opennms.netmgt.alarmd.api.support;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.netmgt.alarmd.Resolver;
import org.opennms.netmgt.alarmd.api.Alarm;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;

public class NorthboundAlarm implements Alarm {

    private String m_uei;
    private Date m_ackTime;
    private String m_ackUser;
    private Integer m_alarmType;
    private String m_appDn;
    private String m_clearKey;
    private Integer m_count;
    private String m_desc;
    private OnmsDistPoller m_poller;
    private Date m_firstOccurrence;
    private Integer m_id;
    private InetAddress m_ipAddr;
    private Date m_lastOccurrence;
    private String m_logMsg;
    private String m_objectInstance;
    private String m_objectType;
    private String m_operInst;
    private String m_ossKey;
    private String m_ossState;
    private String m_alarmKey;
    private OnmsServiceType m_service;
    private OnmsSeverity m_severity;
    private Date m_suppressed;
    private Date m_suppressedUntil;
    private String m_suppressedBy;
    private String m_ticketId;
    private OnmsAlarm m_ticketalarm;
    private TroubleTicketState m_ticketState;
    private String m_x733Type;
    private int m_x733Cause;

    public NorthboundAlarm(OnmsAlarm alarm) {
        //alarm.getAckId();
        //alarm.getAckTime();
        //alarm.getAckUser();
        
        m_ackTime = alarm.getAlarmAckTime();
        m_ackUser = alarm.getAlarmAckUser();
        m_alarmType = alarm.getAlarmType();
        m_appDn = alarm.getApplicationDN();
        m_clearKey = alarm.getClearKey();
        m_count = alarm.getCounter();
        m_desc = alarm.getDescription();
        m_poller = alarm.getDistPoller();
        //alarm.getEventParms();
        //alarm.getFirstAutomationTime();
        m_firstOccurrence = alarm.getFirstEventTime();
        m_id = alarm.getId();
        //alarm.getIfIndex();
        m_ipAddr = alarm.getIpAddr();
        //alarm.getLastAutomationTime();
        //alarm.getLastEvent();
        m_lastOccurrence = alarm.getLastEventTime();
        m_logMsg = alarm.getLogMsg();
        m_objectInstance = alarm.getManagedObjectInstance();
        m_objectType = alarm.getManagedObjectType();
        //alarm.getNode();
        m_operInst = alarm.getOperInstruct();
        m_ossKey = alarm.getOssPrimaryKey();
        m_ossState = alarm.getQosAlarmState();
        m_alarmKey = alarm.getReductionKey();
        m_service = alarm.getServiceType();
        m_severity = alarm.getSeverity();
        //alarm.getSeverityId();
        //alarm.getSeverityLabel();
        m_suppressed = alarm.getSuppressedTime();
        m_suppressedUntil = alarm.getSuppressedUntil();
        m_suppressedBy = alarm.getSuppressedUser();
        m_ticketId = alarm.getTTicketId();
        m_ticketState = alarm.getTTicketState();
        //alarm.getType();
        m_alarmKey = alarm.getUei();
        m_x733Type = alarm.getX733AlarmType();
        m_x733Cause = alarm.getX733ProbableCause();
    }
    
    @Override
    public String getUei() {
        return m_uei;
    }

    @Override
    public Date getTimeStamp() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isPreserved() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setPreserved(boolean preserved) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String resolveNodeLabel(Resolver r) {
        // TODO Auto-generated method stub
        return null;
    }

}
