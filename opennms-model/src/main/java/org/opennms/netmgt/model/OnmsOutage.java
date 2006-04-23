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
    private Integer m_id;

    /** persistent field */
    private OnmsIpInterface m_ipInteface;

    /** persistent field */
    private Date m_ifLostService;

    /** nullable persistent field */
    private Date m_ifRegainedService;

    /** persistent field */
    private OnmsEvent m_eventBySvcRegainedEvent;

    /** persistent field */
    private OnmsEvent m_eventBySvcLostEvent;

    /** persistent field */
    private OnmsMonitoredService m_monitoredService;

    /** full constructor */
    public OnmsOutage(Integer outageId, OnmsIpInterface ipInterface, Date ifLostService, Date ifRegainedService, OnmsEvent eventBySvcRegainedEvent, OnmsEvent eventBySvcLostEvent, OnmsMonitoredService monitoredService) {
        m_id = outageId;
        m_ipInteface = ipInterface;
        m_ifLostService = ifLostService;
        m_ifRegainedService = ifRegainedService;
        m_eventBySvcRegainedEvent = eventBySvcRegainedEvent;
        m_eventBySvcLostEvent = eventBySvcLostEvent;
        m_monitoredService = monitoredService;
    }

    /** default constructor */
    public OnmsOutage() {
    }

    /** minimal constructor */
    public OnmsOutage(Integer outageId, OnmsIpInterface ipInterface, Date ifLostService, OnmsEvent eventBySvcRegainedEvent, OnmsEvent eventBySvcLostEvent, OnmsMonitoredService monitoredService) {
        m_id = outageId;
        m_ipInteface = ipInterface;
        m_ifLostService = ifLostService;
        m_eventBySvcRegainedEvent = eventBySvcRegainedEvent;
        m_eventBySvcLostEvent = eventBySvcLostEvent;
        m_monitoredService = monitoredService;
    }

    /** 
     * @hibernate.id generator-class="assigned" type="java.lang.Integer" column="outageId"
     * @hibernate.generator-param name="sequence" value="outageNxtId"
     */
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer outageId) {
        m_id = outageId;
    }

    /** 
     *            @hibernate.property
     *             column="ipInterface"
     *             length="16"
     *             not-null="true"
     *         
     */
    public OnmsIpInterface getIpInteface() {
        return m_ipInteface;
    }

    public void setIpInteface(OnmsIpInterface ipInterface) {
        m_ipInteface = ipInterface;
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
    public OnmsEvent getEventBySvcRegainedEvent() {
        return m_eventBySvcRegainedEvent;
    }

    public void setEventBySvcRegainedEvent(OnmsEvent eventBySvcRegainedEvent) {
        m_eventBySvcRegainedEvent = eventBySvcRegainedEvent;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="svclosteventid"         
     *         
     */
    public OnmsEvent getEventBySvcLostEvent() {
        return m_eventBySvcLostEvent;
    }

    public void setEventBySvcLostEvent(OnmsEvent eventBySvcLostEvent) {
        m_eventBySvcLostEvent = eventBySvcLostEvent;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="monitoredServiceid"         
     *         
     */
    public OnmsMonitoredService getMonitoredService() {
        return m_monitoredService;
    }

    public void setMonitoredService(OnmsMonitoredService monitoredService) {
        m_monitoredService = monitoredService;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("outageId", getId())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		// TODO Auto-generated method stub
		throw new RuntimeException("visitor method not implemented");
	}

}
