package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="outages"
 *     
*/
public class OnmsOutage extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3846398168228820151L;

    /** identifier field */
    private Integer m_outageId;

    /** persistent field */
    private String m_ipAddr;

    /** persistent field */
    private Date m_ifLostService;

    /** nullable persistent field */
    private Date m_ifRegainedService;

    /** persistent field */
    private OnmsEvent m_eventBySvcRegainedEventId;

    /** persistent field */
    private OnmsEvent m_eventBySvcLostEventId;

    /** persistent field */
    private OnmsNode m_node;

    /** persistent field */
    private OnmsServiceType m_serviceType;

    /** full constructor */
    public OnmsOutage(Integer outageId, String ipAddr, Date ifLostService, Date ifRegainedService, OnmsEvent eventBySvcRegainedEventId, OnmsEvent eventBySvcLostEventId, OnmsNode node, OnmsServiceType service) {
        m_outageId = outageId;
        m_ipAddr = ipAddr;
        m_ifLostService = ifLostService;
        m_ifRegainedService = ifRegainedService;
        m_eventBySvcRegainedEventId = eventBySvcRegainedEventId;
        m_eventBySvcLostEventId = eventBySvcLostEventId;
        m_node = node;
        m_serviceType = service;
    }

    /** default constructor */
    public OnmsOutage() {
    }

    /** minimal constructor */
    public OnmsOutage(Integer outageId, String ipAddr, Date ifLostService, OnmsEvent eventBySvcRegainedEventId, OnmsEvent eventBySvcLostEventId, OnmsNode node, OnmsServiceType service) {
        m_outageId = outageId;
        m_ipAddr = ipAddr;
        m_ifLostService = ifLostService;
        m_eventBySvcRegainedEventId = eventBySvcRegainedEventId;
        m_eventBySvcLostEventId = eventBySvcLostEventId;
        m_node = node;
        m_serviceType = service;
    }

    /** 
     * @hibernate.id generator-class="assigned" type="java.lang.Integer" column="outageId"
     * @hibernate.generator-param name="sequence" value="outageNxtId"
     */
    public Integer getOutageId() {
        return m_outageId;
    }

    public void setOutageId(Integer outageId) {
        m_outageId = outageId;
    }

    /** 
     *            @hibernate.property
     *             column="ipAddr"
     *             length="16"
     *             not-null="true"
     *         
     */
    public String getIpAddr() {
        return m_ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        m_ipAddr = ipAddr;
    }

    /** 
     *            @hibernate.property
     *             column="ifLostService"
     *             length="8"
     *             not-null="true"
     *         
     */
    public Date getIfLostService() {
        return m_ifLostService;
    }

    public void setIfLostService(Date ifLostService) {
        m_ifLostService = ifLostService;
    }

    /** 
     *            @hibernate.property
     *             column="ifRegainedService"
     *             length="8"
     *         
     */
    public Date getIfRegainedService() {
        return m_ifRegainedService;
    }

    public void setIfRegainedService(Date ifRegainedService) {
        m_ifRegainedService = ifRegainedService;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="svcregainedeventid"         
     *         
     */
    public OnmsEvent getEventBySvcRegainedEventId() {
        return m_eventBySvcRegainedEventId;
    }

    public void setEventBySvcRegainedEventId(OnmsEvent eventBySvcRegainedEventId) {
        m_eventBySvcRegainedEventId = eventBySvcRegainedEventId;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="svclosteventid"         
     *         
     */
    public OnmsEvent getEventBySvcLostEventId() {
        return m_eventBySvcLostEventId;
    }

    public void setEventBySvcLostEventId(OnmsEvent eventBySvcLostEventId) {
        m_eventBySvcLostEventId = eventBySvcLostEventId;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="nodeid"         
     *         
     */
    public OnmsNode getNode() {
        return m_node;
    }

    public void setNode(OnmsNode node) {
        m_node = node;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="serviceid"         
     *         
     */
    public OnmsServiceType getServiceType() {
        return m_serviceType;
    }

    public void setServiceType(OnmsServiceType serviceType) {
        m_serviceType = serviceType;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("outageId", getOutageId())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		// TODO Auto-generated method stub
		throw new RuntimeException("visitor method not implemented");
	}

}
