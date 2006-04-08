package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="outages"
 *     
*/
public class OnmsOutage implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3846398168228820151L;

    /** identifier field */
    private Integer outageid;

    /** persistent field */
    private String ipaddr;

    /** persistent field */
    private Date iflostservice;

    /** nullable persistent field */
    private Date ifregainedservice;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsEvent eventBySvcregainedeventid;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsEvent eventBySvclosteventid;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsNode node;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsServiceType serviceType;

    /** full constructor */
    public OnmsOutage(Integer outageid, String ipaddr, Date iflostservice, Date ifregainedservice, org.opennms.netmgt.model.OnmsEvent eventBySvcregainedeventid, org.opennms.netmgt.model.OnmsEvent eventBySvclosteventid, org.opennms.netmgt.model.OnmsNode node, org.opennms.netmgt.model.OnmsServiceType service) {
        this.outageid = outageid;
        this.ipaddr = ipaddr;
        this.iflostservice = iflostservice;
        this.ifregainedservice = ifregainedservice;
        this.eventBySvcregainedeventid = eventBySvcregainedeventid;
        this.eventBySvclosteventid = eventBySvclosteventid;
        this.node = node;
        this.serviceType = service;
    }

    /** default constructor */
    public OnmsOutage() {
    }

    /** minimal constructor */
    public OnmsOutage(Integer outageid, String ipaddr, Date iflostservice, org.opennms.netmgt.model.OnmsEvent eventBySvcregainedeventid, org.opennms.netmgt.model.OnmsEvent eventBySvclosteventid, org.opennms.netmgt.model.OnmsNode node, org.opennms.netmgt.model.OnmsServiceType service) {
        this.outageid = outageid;
        this.ipaddr = ipaddr;
        this.iflostservice = iflostservice;
        this.eventBySvcregainedeventid = eventBySvcregainedeventid;
        this.eventBySvclosteventid = eventBySvclosteventid;
        this.node = node;
        this.serviceType = service;
    }

    /** 
     * @hibernate.id generator-class="assigned" type="java.lang.Integer" column="outageid"
     * @hibernate.generator-param name="sequence" value="outageNxtId"
     */
    public Integer getOutageid() {
        return this.outageid;
    }

    public void setOutageid(Integer outageid) {
        this.outageid = outageid;
    }

    /** 
     *            @hibernate.property
     *             column="ipaddr"
     *             length="16"
     *             not-null="true"
     *         
     */
    public String getIpaddr() {
        return this.ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    /** 
     *            @hibernate.property
     *             column="iflostservice"
     *             length="8"
     *             not-null="true"
     *         
     */
    public Date getIflostservice() {
        return this.iflostservice;
    }

    public void setIflostservice(Date iflostservice) {
        this.iflostservice = iflostservice;
    }

    /** 
     *            @hibernate.property
     *             column="ifregainedservice"
     *             length="8"
     *         
     */
    public Date getIfregainedservice() {
        return this.ifregainedservice;
    }

    public void setIfregainedservice(Date ifregainedservice) {
        this.ifregainedservice = ifregainedservice;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="svcregainedeventid"         
     *         
     */
    public org.opennms.netmgt.model.OnmsEvent getEventBySvcregainedeventid() {
        return this.eventBySvcregainedeventid;
    }

    public void setEventBySvcregainedeventid(org.opennms.netmgt.model.OnmsEvent eventBySvcregainedeventid) {
        this.eventBySvcregainedeventid = eventBySvcregainedeventid;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="svclosteventid"         
     *         
     */
    public org.opennms.netmgt.model.OnmsEvent getEventBySvclosteventid() {
        return this.eventBySvclosteventid;
    }

    public void setEventBySvclosteventid(org.opennms.netmgt.model.OnmsEvent eventBySvclosteventid) {
        this.eventBySvclosteventid = eventBySvclosteventid;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="nodeid"         
     *         
     */
    public org.opennms.netmgt.model.OnmsNode getNode() {
        return this.node;
    }

    public void setNode(org.opennms.netmgt.model.OnmsNode node) {
        this.node = node;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="serviceid"         
     *         
     */
    public OnmsServiceType getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(OnmsServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("outageid", getOutageid())
            .toString();
    }

}
