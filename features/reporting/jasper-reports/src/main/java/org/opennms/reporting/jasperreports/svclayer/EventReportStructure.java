package org.opennms.reporting.jasperreports.svclayer;


public class EventReportStructure {
	
	 /** The human-readable name of the node of this event. Can be null. */
    protected String m_nodeLabel;
	
    /** Unique identifier for the event, cannot be null */
    protected int m_eventId;
	
    /** Universal Event Identifer (UEI) for this event, cannot be null */
    protected String m_eventUEI;
	
    /** Creation time of event in database, cannot be null */
    protected String m_createTime;
    
    /**Event Log */
    protected String m_eventLogMsg;
    
    /** AlarmId of the event    */
    protected int m_alarmId;
    
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
	public int getAlarmId() {
		return m_alarmId;
	}

	/**
     * <p>Setter for the field <code>eventId</code>.</p>
     *
     * @param eventId a int.
     */
	public void setAlarmId(int alarmId) {
		m_alarmId = alarmId;
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
	 * <p>Getter for the field <code>eventDescr</code>.</p>
	 * @return
	 */
	public String getEventLogMsg() {
		return m_eventLogMsg;
	}

	/**
	 * <p>Setter for the field <code>eventDescr</code>.</p>
	 * @param m_eventDescr
	 */
	public void setEventLogMsg(String eventLogMsg) {
		this.m_eventLogMsg = eventLogMsg;
	}
	
	
}
