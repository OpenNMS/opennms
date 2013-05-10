package org.opennms.reporting.jasperreports.svclayer;

import java.util.Date;

import org.opennms.netmgt.model.AckAction;

public class AlarmReportStructure {
	
	 /** The human-readable name of the node of this alarm. Can be null. */
    protected String m_nodeLabel;
	
    /** Unique identifier for the event, cannot be null */
    protected int m_eventId;
	
    /** The alarmId if reduced. Can be null. */
    protected Integer m_alarmId;
	
    /** Universal Event Identifer (UEI) for this event, cannot be null */
    protected String m_eventUEI;
	
    /** Creation time of event in database, cannot be null */
    protected String m_createTime;
	
    /** Creation time of ack in database, cannot be null */
    protected String m_ackTime;
    
    /** User name of ack in database, cannot be null */
    protected String m_ackUser;
    
    /** Action type of ack in database, cannot be null */
    protected String m_ackAction;

    
    /**
     * <p>Getter for the field <code>nodeLabel</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
	public String getNodeLabel() {
		return m_nodeLabel;
	}

	/**
     * <p>Setter for the field <code>nodeLabel</code>.</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     */
	public void setNodeLabel(String nodeLabel) {
		m_nodeLabel = nodeLabel;
	}

	/**
     * <p>Getter for the field <code>eventId</code>.</p>
     *
     * @return a int.
     */
	public int getEventId() {
		return m_eventId;
	}

	/**
     * <p>Setter for the field <code>eventId</code>.</p>
     *
     * @param eventId a int.
     */
	public void setEventId(int eventId) {
		m_eventId = eventId;
	}

	 /**
     * <p>Getter for the field <code>alarmId</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
	public Integer getAlarmId() {
		return m_alarmId;
	}

	/**
     * <p>Setter for the field <code>alarmId</code>.</p>
     *
     * @param alarmId a {@link java.lang.Integer} object.
     */
	public void setAlarmId(Integer alarmId) {
		m_alarmId = alarmId;
	}

	 /**
     * <p>Getter for the field <code>eventUEI</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
	public String getEventUEI() {
		return m_eventUEI;
	}

	/**
     * <p>Setter for the field <code>eventUEI</code>.</p>
     *
     * @param eventUEI a {@link java.lang.String} object.
     */
	public void setEventUEI(String eventUEI) {
		m_eventUEI = eventUEI;
	}

	 /**
     * <p>Getter for the field <code>createTime</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
	public String getCreateTime() {
		return m_createTime;
	}

	/**
     * <p>Setter for the field <code>createTime</code>.</p>
     *
     * @param createTime a {@link java.lang.String} object.
     */
	public void setCreateTime(String createTime) {
		m_createTime = createTime;
	}
	
	 /**
     * <p>Getter for the field <code>ackTime</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
	public String getAckTime() {
		return m_ackTime;
	}

	/**
     * <p>Setter for the field <code>ackTime</code>.</p>
     *
     * @param ackTime a {@link java.lang.String} object.
     */
	public void setAckTime(String ackTime) {
		m_ackTime = ackTime;
	}

	/**
     * <p>Getter for the field <code>ackUser</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
	public String getAckUser() {
		return m_ackUser;
	}

	/**
     * <p>Setter for the field <code>ackUser</code>.</p>
     *
     * @param ackUser a {@link java.lang.String} object.
     */
	public void setAckUser(String ackUser) {
		m_ackUser = ackUser;
	}

	 /**
     * <p>Getter for the field <code>ackAction</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
	public String getAckAction() {
		return m_ackAction;
	}

	/**
     * <p>Setter for the field <code>ackAction</code>.</p>
     *
     * @param ackUser a {@link java.lang.String} object.
     */
	public void setAckAction(String ackAction) {
		m_ackAction = ackAction;
	}
	
}
