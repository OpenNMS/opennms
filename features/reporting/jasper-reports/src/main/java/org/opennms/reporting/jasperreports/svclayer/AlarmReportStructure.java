package org.opennms.reporting.jasperreports.svclayer;

public class AlarmReportStructure {
	
	/** The alarmId if reduced. Can be null. */
	private Integer m_alarmId;
    
	 /** The human-readable name of the node of this alarm. Can be null. */
    private String m_nodeLabel;
	
    /** Unique identifier for the event, cannot be null */
    private Integer m_eventId;
	
    /** Universal Event Identifer (UEI) for this event, cannot be null */
    private String m_eventUEI;
	
    /** nullable persistent field */
    private String m_dpName;
    
    /** nullable persistent field */
    private Integer m_nodeId;
    
    /** nullable persistent field */
    private String m_ipAddr;
    
    /** nullable persistent field */
    private String m_eventIpAddr;
    
    /** nullable persistent field */
    private Integer m_serviceId;
    
    /** nullable persistent field */
    private String m_reductionKey;

    /** nullable persistent field */
    private Integer m_alarmType;

    /** persistent field */
    private Integer m_counter;
    
    private String m_severity;
    
    /** persistent field */
    private Integer m_lastEventId;
    
    /** persistent field */
    private String m_firstEventTime;
    
    /** persistent field */
    private String m_lastEventTime;
    
    /** persistent field */
    private String m_firstAutomationTime;
    
    /** persistent field */
    private String m_lastAutomationTime;
    
    /** nullable persistent field */
    private String m_description;

    /** nullable persistent field */
    private String m_logMsg;

    /** nullable persistent field */
    private String m_operInstruct;

    /** nullable persistent field */
    private String m_tTicketId;
    
    /** nullable persistent field */
    private String m_tTicketState;

    /** nullable persistent field */
    private String m_mouseOverText;

    /** nullable persistent field */
    private String m_suppressedUntil;

    /** nullable persistent field */
    private String m_suppressedUser;

    /** nullable persistent field */
    private String m_suppressedTime;

    /** nullable persistent field */
    private String m_alarmAckUser;

    /** nullable persistent field */
    private String m_alarmAckTime;
    
    /** persistent field */
    private String m_managedObjectInstance;
    
    /** persistent field */
    private String m_managedObjectType;
    
    /** persistent field */
    private String m_applicationDN;

    /** nullable persistent field */
    private String m_ossPrimaryKey;

    /** nullable persistent field */
    private String m_x733AlarmType;

    /** nullable persistent field */
    private String m_qosAlarmState;

    /** nullable persistent field */
    private Integer m_x733ProbableCause;
    
    /** nullable persistent field */
    private String m_clearKey;

    /** nullable persistent field */
    private Integer m_ifIndex;
    
    /** persistent field */
    private String m_eventParms;
    
    /** nullable persistent field */
    private String m_stickyMemo;
    
    /** persistent field */
	private String m_eventTime;

	/** nullable persistent field */
	private String m_eventHost;
	
	/** nullable persistent field */
	private String m_eventSnmpHost;
	
	/** persistent field */
	private String m_eventSource;
	
	/** persistent field */
	private String m_eventDbName;
    
	/** nullable persistent field */
	private Integer m_eventServiceId;
	
	/** nullable persistent field */
	private String m_specificEventParms;

	/** persistent field */
	private String m_eventCreateTime;

	/** nullable persistent field */
	private String m_eventDescr;

	/** nullable persistent field */
	private String m_eventLogGroup;

	/** nullable persistent field */
	private String m_eventLogMsg;

	/** persistent field */
	private String m_eventSeverity;
	
	/** nullable persistent field */
	private String m_eventPathOutage;

	/** nullable persistent field */
	private String m_eventCorrelation;

	/** nullable persistent field */
	private Integer m_eventSuppressedCount;

	/** nullable persistent field */
	private String m_eventOperInstruct;
	
	/** nullable persistent field */
	private String m_eventAutoAction;

	/** nullable persistent field */
	private String m_eventOperAction;

	/** nullable persistent field */
	private String m_eventOperActionMenuText;

	/** nullable persistent field */
	private String m_eventNotification;

	/** nullable persistent field */
	private String m_eventTTicket;

	/** nullable persistent field */
	private Integer m_eventTTicketState;

	/** nullable persistent field */
	private String m_eventForward;

	/** nullable persistent field */
	private String m_eventMouseOverText;

	/** persistent field */
	private String m_eventLog;

	/** persistent field */
	private String m_eventDisplay;

	/** nullable persistent field */
	private String m_eventAckUser;

	/** nullable persistent field */
	private String m_eventAckTime;
	
	/** nullable persistent field */
    private Integer m_eventIfIndex;
    
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
     * @return a {@link java.lang.Integer} object.
     */
	public Integer getEventId() {
		return m_eventId;
	}

	/**
     * <p>Setter for the field <code>eventId</code>.</p>
     *
     * @param eventId an Integer.
     */
	public void setEventId(Integer eventId) {
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
    * <p>Getter for the field <code>dpName</code>.</p>
    *
    * @return a {@link java.lang.String} object.
    */
	public String getDpName() {
		return m_dpName;
	}

	/**
    * <p>Setter for the field <code>dpName</code>.</p>
    *
    * @param dpName a {@link java.lang.String} object.
    */
	public void setDpName(String dpName) {
		this.m_dpName = dpName;
	}
	
    /**
     * <p>getNodeId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getNodeId() {
    	return m_nodeId;
    }
    
    /**
     * <p>Setter for the field <code>nodeId</code>.</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     */
    public void setNodeId(Integer nodeId) {
    	m_nodeId = nodeId;
	}
    
    /**
     * <p>getEventServiceId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getEventServiceId() {
		return m_eventServiceId;
	}

	/**
	 * <p>setEventServiceID</p>
	 *
	 * * @return a {@link java.lang.Integer} object.
	 */
	public void setEventServiceID(Integer serviceId) {
		this.m_eventServiceId = serviceId;
	}
	
	 /**
     * <p>getServiceId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getServiceId() {
		return m_serviceId;
	}

	/**
	 * <p>setServiceID</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public void setServiceID(Integer serviceId) {
		this.m_serviceId = serviceId;
	}
	
	/**
     * <p>getReductionKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReductionKey() {
        return this.m_reductionKey;
    }
    
    /**
	 * <p>getEventIpAddr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
    public String getEventIpAddr() {
		return m_eventIpAddr;
	}

	/**
	 * <p>setEventIpAddr</p>
	 *
	 * @param eventIpAddr a {@link java.lang.String} object.
	 */
	public void setEventIpAddr(String eventIpAddr) {
		m_eventIpAddr = eventIpAddr;
	}

    /**
     * <p>setReductionKey</p>
     *
     * @param reductionkey a {@link java.lang.String} object.
     */
    public void setReductionKey(String reductionkey) {
        this.m_reductionKey = reductionkey;
    }

    /**
     * <p>getAlarmType</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getAlarmType() {
        return this.m_alarmType;
    }

    /**
     * <p>setAlarmType</p>
     *
     * @param alarmtype a {@link java.lang.Integer} object.
     */
    public void setAlarmType(Integer alarmtype) {
        this.m_alarmType = alarmtype;
    }

    /**
     * <p>getCounter</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getCounter() {
        return this.m_counter;
    }

    /**
     * <p>setCounter</p>
     *
     * @param counter a {@link java.lang.Integer} object.
     */
    public void setCounter(Integer counter) {
        this.m_counter = counter;
    }
    
    /**
     * <p>getSeverity</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSeverity() {
        return this.m_severity;
    }

    /**
     * <p>setSeverity</p>
     *
     * @param severity a {@link java.lang.String} object.
     */
    public void setSeverity(String severity) {
        m_severity = severity;
    }
    
    /**
     * <p>getLastEvent</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getLastEventId() {
        return this.m_lastEventId;
    }

    /**
     * <p>setLastEvent</p>
     *
     * @param lastEventId a {@link java.lang.Integer} object.
     */
    public void setLastEventId(Integer lastEventId) {
    	this.m_lastEventId = lastEventId;
    }
    
    /**
     * <p>getFirstEventTime</p>
     *
     * @return a {@link java.util.String} object.
     */
    public String getFirstEventTime() {
        return this.m_firstEventTime;
    }

    /**
     * <p>setFirstEventTime</p>
     *
     * @param firsteventtime a {@link java.util.String} object.
     */
    public void setFirstEventTime(String firsteventtime) {
        this.m_firstEventTime = firsteventtime;
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return this.m_description;
    }

    /**
     * <p>setDescription</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        this.m_description = description;
    }

    /**
     * <p>getLogMsg</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLogMsg() {
        return this.m_logMsg;
    }

    /**
     * <p>setLogMsg</p>
     *
     * @param logmsg a {@link java.lang.String} object.
     */
    public void setLogMsg(String logmsg) {
        this.m_logMsg = logmsg;
    }

    /**
     * <p>getOperInstruct</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOperInstruct() {
        return this.m_operInstruct;
    }

    /**
     * <p>setOperInstruct</p>
     *
     * @param operinstruct a {@link java.lang.String} object.
     */
    public void setOperInstruct(String operinstruct) {
        this.m_operInstruct = operinstruct;
    }

    /**
     * <p>getTTicketId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String gettTicketId() {
        return this.m_tTicketId;
    }

    /**
     * <p>setTTicketId</p>
     *
     * @param tticketid a {@link java.lang.String} object.
     */
    public void settTicketId(String tticketid) {
        this.m_tTicketId = tticketid;
    }

    /**
     * <p>getTTicketState</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String gettTicketState() {
        return this.m_tTicketState;
    }

    /**
     * <p>setTTicketState</p>
     *
     * @param tticketstate a {@link java.lang.String} object.
     */
    public void settTicketState(String tticketstate) {
        this.m_tTicketState = tticketstate;
    }

    /**
     * <p>getMouseOverText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMouseOverText() {
        return this.m_mouseOverText;
    }

    /**
     * <p>setMouseOverText</p>
     *
     * @param mouseovertext a {@link java.lang.String} object.
     */
    public void setMouseOverText(String mouseovertext) {
        this.m_mouseOverText = mouseovertext;
    }

    /**
     * <p>getSuppressedUntil</p>
     *
     * @return a {@link java.util.String} object.
     */
    public String getSuppressedUntil() {
        return this.m_suppressedUntil;
    }

    /**
     * <p>setSuppressedUntil</p>
     *
     * @param suppresseduntil a {@link java.util.String} object.
     */
    public void setSuppressedUntil(String suppresseduntil) {
        this.m_suppressedUntil = suppresseduntil;
    }

    /**
     * <p>getSuppressedUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSuppressedUser() {
        return this.m_suppressedUser;
    }

    /**
     * <p>setSuppressedUser</p>
     *
     * @param suppresseduser a {@link java.lang.String} object.
     */
    public void setSuppressedUser(String suppresseduser) {
        this.m_suppressedUser = suppresseduser;
    }

    /**
     * <p>getSuppressedTime</p>
     *
     * @return a {@link java.util.String} object.
     */
    public String getSuppressedTime() {
        return this.m_suppressedTime;
    }

    /**
     * <p>setSuppressedTime</p>
     *
     * @param suppressedtime a {@link java.util.String} object.
     */
    public void setSuppressedTime(String suppressedtime) {
        this.m_suppressedTime = suppressedtime;
    }

    /**
     * <p>getAlarmAckUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAlarmAckUser() {
        return this.m_alarmAckUser;
    }

    /**
     * <p>setAlarmAckUser</p>
     *
     * @param alarmackuser a {@link java.lang.String} object.
     */
    public void setAlarmAckUser(String alarmackuser) {
        this.m_alarmAckUser = alarmackuser;
    }

    /**
     * <p>getAlarmAckTime</p>
     *
     * @return a {@link java.util.String} object.
     */
    public String getAlarmAckTime() {
        return this.m_alarmAckTime;
    }

    /**
     * <p>setAlarmAckTime</p>
     *
     * @param alarmacktime a {@link java.util.String} object.
     */
    public void setAlarmAckTime(String alarmacktime) {
        this.m_alarmAckTime = alarmacktime;
    }

    /**
     * <p>getClearKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClearKey() {
        return this.m_clearKey;
    }

    /**
     * <p>setClearKey</p>
     *
     * @param clearKey a {@link java.lang.String} object.
     */
    public void setClearKey(String clearKey) {
        this.m_clearKey = clearKey;
    }

    /**
     * <p>getEventParms</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSpecificEventParms() {
        return this.m_specificEventParms;
    }

    /**
     * <p>setEventParms</p>
     *
     * @param specificEventParms a {@link java.lang.String} object.
     */
    public void setSpecificEventParms(String specificEventParms) {
        this.m_specificEventParms = specificEventParms;
    }
    

    /**
     * <p>getLastEventTime</p>
     *
     * @return a {@link java.util.String} object.
     */
    public String getLastEventTime() {
        return m_lastEventTime;
    }

    /**
     * <p>setLastEventTime</p>
     *
     * @param lastEventTime a {@link java.util.String} object.
     */
    public void setLastEventTime(String lastEventTime) {
        m_lastEventTime = lastEventTime;
    }
    
    /**
     * <p>getApplicationDN</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getApplicationDN() {
        return m_applicationDN;
    }

    /**
     * <p>setApplicationDN</p>
     *
     * @param applicationDN a {@link java.lang.String} object.
     */
    public void setApplicationDN(String applicationDN) {
        m_applicationDN = applicationDN;
    }

    /**
     * <p>getManagedObjectInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getManagedObjectInstance() {
        return m_managedObjectInstance;
    }

    /**
     * <p>setManagedObjectInstance</p>
     *
     * @param managedObjectInstance a {@link java.lang.String} object.
     */
    public void setManagedObjectInstance(String managedObjectInstance) {
        m_managedObjectInstance = managedObjectInstance;
    }

    /**
     * <p>getManagedObjectType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getManagedObjectType() {
        return m_managedObjectType;
    }

    /**
     * <p>setManagedObjectType</p>
     *
     * @param managedObjectType a {@link java.lang.String} object.
     */
    public void setManagedObjectType(String managedObjectType) {
        m_managedObjectType = managedObjectType;
    }

    /**
     * <p>getOssPrimaryKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOssPrimaryKey() {
        return m_ossPrimaryKey;
    }
    
    /**
     * <p>setOssPrimaryKey</p>
     *
     * @param key a {@link java.lang.String} object.
     */
    public void setOssPrimaryKey(String key) {
        m_ossPrimaryKey = key;
    }
    
    /**
     * <p>getX733AlarmType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getX733AlarmType() {
        return m_x733AlarmType;
    }
    
    /**
     * <p>setX733AlarmType</p>
     *
     * @param alarmType a {@link java.lang.String} object.
     */
    public void setX733AlarmType(String alarmType) {
        m_x733AlarmType = alarmType;
    }
    
    /**
     * <p>getX733ProbableCause</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getX733ProbableCause() {
        return m_x733ProbableCause;
    }
    
    /**
     * <p>setX733ProbableCause</p>
     *
     * @param alarmType a {@link java.lang.Integer} object.
     */
    public void setX733ProbableCause(Integer cause) {
        m_x733ProbableCause = cause;
    }
    
    /**
     * <p>getQosAlarmState</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getQosAlarmState() {
        return m_qosAlarmState;
        
    }
    /**
     * <p>setQosAlarmState</p>
     *
     * @param alarmState a {@link java.lang.String} object.
     */
    public void setQosAlarmState(String alarmState) {
        m_qosAlarmState = alarmState;
    }

    /**
     * <p>getFirstAutomationTime</p>
     *
     * @return a {@link java.util.String} object.
     */
    public String getFirstAutomationTime() {
        return m_firstAutomationTime;
    }

    /**
     * <p>setFirstAutomationTime</p>
     *
     * @param firstAutomationTime a {@link java.util.String} object.
     */
    public void setFirstAutomationTime(String firstAutomationTime) {
        m_firstAutomationTime = firstAutomationTime;
    }

    /**
     * <p>getLastAutomationTime</p>
     *
     * @return a {@link java.util.String} object.
     */
    public String getLastAutomationTime() {
        return m_lastAutomationTime;
    }

    /**
     * <p>setLastAutomationTime</p>
     *
     * @param lastAutomationTime a {@link java.util.String} object.
     */
    public void setLastAutomationTime(String lastAutomationTime) {
        m_lastAutomationTime = lastAutomationTime;
    }
    

    /**
     * <p>getIfIndex</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getIfIndex() {
        return m_ifIndex;
    }

    /**
     * <p>setIfIndex</p>
     *
     * @param ifIndex a {@link java.lang.Integer} object.
     */
    public void setIfIndex(Integer ifIndex) {
        m_ifIndex = ifIndex;
    }
    
    /**
     * <p>getStickyMemo</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStickyMemo() {
        return m_stickyMemo;
    }

    /**
     * <p>setStickyMemo</p>
     *
     * @param stickyMemo a {@link java.lang.String} object.
     */
    public void setStickyMemo(String stickyMemo) {
        this.m_stickyMemo = stickyMemo;
    }
    
    /**
	 * <p>getEventTime</p>
	 *
	 * @return a {@link java.util.String} object.
	 */
	public String getEventTime() {
		return m_eventTime;
	}

	/**
	 * <p>setEventTime</p>
	 *
	 * @param eventtime a {@link java.util.String} object.
	 */
	public void setEventTime(String eventtime) {
		m_eventTime = eventtime;
	}

	/**
	 * <p>getEventHost</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventHost() {
		return m_eventHost;
	}

	/**
	 * <p>setEventHost</p>
	 *
	 * @param eventhost a {@link java.lang.String} object.
	 */
	public void setEventHost(String eventhost) {
		m_eventHost = eventhost;
	}

	/**
	 * <p>getEventSource</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventSource() {
		return m_eventSource;
	}

	/**
	 * <p>setEventSource</p>
	 *
	 * @param eventsource a {@link java.lang.String} object.
	 */
	public void setEventSource(String eventsource) {
		m_eventSource = eventsource;
	}

	/**
	 * <p>getIpAddr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getIpAddr() {
		return m_ipAddr;
	}

	/**
	 * <p>setIpAddr</p>
	 *
	 * @param ipaddr a {@link java.lang.String} object.
	 */
	public void setIpAddr(String ipaddr) {
		m_ipAddr = ipaddr;
	}

	/**
	 * <p>getEventDbName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventDbName() {
		return m_eventDbName;
	}

	/**
	 * <p>setEventUei</p>
	 *
	 * @param eventDbName a {@link java.lang.String} object.
	 */
	public void setEventDbName(String eventDbName) {
		m_eventDbName = eventDbName;
	}

	/**
	 * <p>getEventSnmpHost</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventSnmpHost() {
		return m_eventSnmpHost;
	}

	/**
	 * <p>setEventSnmpHost</p>
	 *
	 * @param eventsnmphost a {@link java.lang.String} object.
	 */
	public void setEventSnmpHost(String eventsnmphost) {
		m_eventSnmpHost = eventsnmphost;
	}

	/**
	 * <p>getEventParms</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventParms() {
		return m_eventParms;
	}

	/**
	 * <p>setEventParms</p>
	 *
	 * @param eventparms a {@link java.lang.String} object.
	 */
	public void setEventParms(String eventparms) {
		m_eventParms = eventparms;
	}

	/**
	 * <p>getEventCreateTime</p>
	 *
	 * @return a {@link java.util.String} object.
	 */
	public String getEventCreateTime() {
		return m_eventCreateTime;
	}

	/**
	 * <p>setEventCreateTime</p>
	 *
	 * @param eventcreatetime a {@link java.util.String} object.
	 */
	public void setEventCreateTime(String eventcreatetime) {
		m_eventCreateTime = eventcreatetime;
	}

	/**
	 * <p>getEventDescr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventDescr() {
		return m_eventDescr;
	}

	/**
	 * <p>setEventDescr</p>
	 *
	 * @param eventdescr a {@link java.lang.String} object.
	 */
	public void setEventDescr(String eventdescr) {
		m_eventDescr = eventdescr;
	}

	/**
	 * <p>getEventLogGroup</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventLogGroup() {
		return m_eventLogGroup;
	}

	/**
	 * <p>setEventLogGroup</p>
	 *
	 * @param eventloggroup a {@link java.lang.String} object.
	 */
	public void setEventLogGroup(String eventloggroup) {
		m_eventLogGroup = eventloggroup;
	}

	/**
	 * <p>getEventLogMsg</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventLogMsg() {
		return m_eventLogMsg;
	}

	/**
	 * <p>setEventLogMsg</p>
	 *
	 * @param eventlogmsg a {@link java.lang.String} object.
	 */
	public void setEventLogMsg(String eventlogmsg) {
		m_eventLogMsg = eventlogmsg;
	}

	/**
	 * <p>getEventSeverity</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public String getEventSeverity() {
		return m_eventSeverity;
	}

	/**
	 * <p>setEventSeverity</p>
	 *
	 * @param severity a {@link java.lang.Integer} object.
	 */
	public void setEventSeverity(String severity) {
		m_eventSeverity = severity;
	}

	/**
	 * <p>getEventPathOutage</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventPathOutage() {
		return m_eventPathOutage;
	}

	/**
	 * <p>setEventPathOutage</p>
	 *
	 * @param eventpathoutage a {@link java.lang.String} object.
	 */
	public void setEventPathOutage(String eventpathoutage) {
		m_eventPathOutage = eventpathoutage;
	}

	/**
	 * <p>getEventCorrelation</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventCorrelation() {
		return m_eventCorrelation;
	}

	/**
	 * <p>setEventCorrelation</p>
	 *
	 * @param eventcorrelation a {@link java.lang.String} object.
	 */
	public void setEventCorrelation(String eventcorrelation) {
		m_eventCorrelation = eventcorrelation;
	}

	/**
	 * <p>getEventSuppressedCount</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getEventSuppressedCount() {
		return m_eventSuppressedCount;
	}

	/**
	 * <p>setEventSuppressedCount</p>
	 *
	 * @param eventsuppressedcount a {@link java.lang.Integer} object.
	 */
	public void setEventSuppressedCount(Integer eventsuppressedcount) {
		m_eventSuppressedCount = eventsuppressedcount;
	}

	/**
	 * <p>getEventOperInstruct</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventOperInstruct() {
		return m_eventOperInstruct;
	}

	/**
	 * <p>setEventOperInstruct</p>
	 *
	 * @param eventoperinstruct a {@link java.lang.String} object.
	 */
	public void setEventOperInstruct(String eventoperinstruct) {
		m_eventOperInstruct = eventoperinstruct;
	}

	/**
	 * <p>getEventAutoAction</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventAutoAction() {
		return m_eventAutoAction;
	}

	/**
	 * <p>setEventAutoAction</p>
	 *
	 * @param eventautoaction a {@link java.lang.String} object.
	 */
	public void setEventAutoAction(String eventautoaction) {
		m_eventAutoAction = eventautoaction;
	}

	/**
	 * <p>getEventOperAction</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventOperAction() {
		return m_eventOperAction;
	}

	/**
	 * <p>setEventOperAction</p>
	 *
	 * @param eventoperaction a {@link java.lang.String} object.
	 */
	public void setEventOperAction(String eventoperaction) {
		m_eventOperAction = eventoperaction;
	}

	/**
	 * <p>getEventOperActionMenuText</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventOperActionMenuText() {
		return m_eventOperActionMenuText;
	}

	/**
	 * <p>setEventOperActionMenuText</p>
	 *
	 * @param eventOperActionMenuText a {@link java.lang.String} object.
	 */
	public void setEventOperActionMenuText(String eventOperActionMenuText) {
		m_eventOperActionMenuText = eventOperActionMenuText;
	}

	/**
	 * <p>getEventNotification</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventNotification() {
		return m_eventNotification;
	}

	/**
	 * <p>setEventNotification</p>
	 *
	 * @param eventnotification a {@link java.lang.String} object.
	 */
	public void setEventNotification(String eventnotification) {
		m_eventNotification = eventnotification;
	}

	/**
	 * <p>getEventTTicket</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventTTicket() {
		return m_eventTTicket;
	}

	/**
	 * <p>setEventTTicket</p>
	 *
	 * @param eventtticket a {@link java.lang.String} object.
	 */
	public void setEventTTicket(String eventtticket) {
		m_eventTTicket = eventtticket;
	}

	/**
	 * <p>getEventTTicketState</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getEventTTicketState() {
		return m_eventTTicketState;
	}

	/**
	 * <p>setEventTTicketState</p>
	 *
	 * @param eventtticketstate a {@link java.lang.Integer} object.
	 */
	public void setEventTTicketState(Integer eventtticketstate) {
		m_eventTTicketState = eventtticketstate;
	}

	/**
	 * <p>getEventForward</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventForward() {
		return m_eventForward;
	}

	/**
	 * <p>setEventForward</p>
	 *
	 * @param eventforward a {@link java.lang.String} object.
	 */
	public void setEventForward(String eventforward) {
		m_eventForward = eventforward;
	}

	/**
	 * <p>getEventMouseOverText</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventMouseOverText() {
		return m_eventMouseOverText;
	}

	/**
	 * <p>setEventMouseOverText</p>
	 *
	 * @param eventmouseovertext a {@link java.lang.String} object.
	 */
	public void setEventMouseOverText(String eventmouseovertext) {
		m_eventMouseOverText = eventmouseovertext;
	}

	/**
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventLog() {
		return m_eventLog;
	}

	/**
	 * <p>setEventLog</p>
	 *
	 * @param eventlog a {@link java.lang.String} object.
	 */
	public void setEventLog(String eventlog) {
		m_eventLog = eventlog;
	}

	/**
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventDisplay() {
		return m_eventDisplay;
	}

	/**
	 * <p>setEventDisplay</p>
	 *
	 * @param eventdisplay a {@link java.lang.String} object.
	 */
	public void setEventDisplay(String eventdisplay) {
		m_eventDisplay = eventdisplay;
	}

	/**
	 * <p>getEventAckUser</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getEventAckUser() {
		return m_eventAckUser;
	}

	/**
	 * <p>setEventAckUser</p>
	 *
	 * @param eventackuser a {@link java.lang.String} object.
	 */
	public void setEventAckUser(String eventackuser) {
		m_eventAckUser = eventackuser;
	}
	
	/**
	 * <p>getEventAckTime</p>
	 *
	 * @return a {@link java.util.String} object.
	 */
	public String getEventAckTime() {
		return m_eventAckTime;
	}

	/**
	 * <p>setEventAckTime</p>
	 *
	 * @param eventacktime a {@link java.util.String} object.
	 */
	public void setEventAckTime(String eventacktime) {
		m_eventAckTime = eventacktime;
	}

	/**
    * <p>getEventIfIndex</p>
    *
    * @return a {@link java.lang.Integer} object.
    */
    public Integer getEventIfIndex() {
        return m_eventIfIndex;
    }

    /**
     * <p>setIfIndex</p>
     *
     * @param ifIndex a {@link java.lang.Integer} object.
     */
    public void setEventIfIndex(Integer ifIndex) {
    	m_eventIfIndex = ifIndex;
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
