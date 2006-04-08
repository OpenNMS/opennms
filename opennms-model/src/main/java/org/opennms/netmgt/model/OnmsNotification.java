package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="notifications"
 *     
*/
public class OnmsNotification implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1162549324168290004L;

    /** identifier field */
    private Integer notifyid;

    /** persistent field */
    private String textmsg;

    /** nullable persistent field */
    private String subject;

    /** nullable persistent field */
    private String numericmsg;

    /** nullable persistent field */
    private Date pagetime;

    /** nullable persistent field */
    private Date respondtime;

    /** nullable persistent field */
    private String answeredby;

    /** nullable persistent field */
    private String interfaceid;

    /** nullable persistent field */
    private Integer serviceid;

    /** nullable persistent field */
    private String queueid;

    /** persistent field */
    private String eventuei;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsEvent event;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsNode node;

    /** persistent field */
    private Set usersnotifieds;

    /** full constructor */
    public OnmsNotification(Integer notifyid, String textmsg, String subject, String numericmsg, Date pagetime, Date respondtime, String answeredby, String interfaceid, Integer serviceid, String queueid, String eventuei, org.opennms.netmgt.model.OnmsEvent event, org.opennms.netmgt.model.OnmsNode node, Set usersnotifieds) {
        this.notifyid = notifyid;
        this.textmsg = textmsg;
        this.subject = subject;
        this.numericmsg = numericmsg;
        this.pagetime = pagetime;
        this.respondtime = respondtime;
        this.answeredby = answeredby;
        this.interfaceid = interfaceid;
        this.serviceid = serviceid;
        this.queueid = queueid;
        this.eventuei = eventuei;
        this.event = event;
        this.node = node;
        this.usersnotifieds = usersnotifieds;
    }

    /** default constructor */
    public OnmsNotification() {
    }

    /** minimal constructor */
    public OnmsNotification(Integer notifyid, String textmsg, String eventuei, org.opennms.netmgt.model.OnmsEvent event, org.opennms.netmgt.model.OnmsNode node, Set usersnotifieds) {
        this.notifyid = notifyid;
        this.textmsg = textmsg;
        this.eventuei = eventuei;
        this.event = event;
        this.node = node;
        this.usersnotifieds = usersnotifieds;
    }

    /** 
     * @hibernate.id generator-class="native" type="java.lang.Integer" column="notifyid"
     * @hibernate.generator-param name="sequence" value="notifyNxtId"
     */
    public Integer getNotifyid() {
        return this.notifyid;
    }

    public void setNotifyid(Integer notifyid) {
        this.notifyid = notifyid;
    }

    /** 
     *            @hibernate.property
     *             column="textmsg"
     *             length="4000"
     *             not-null="true"
     *         
     */
    public String getTextmsg() {
        return this.textmsg;
    }

    public void setTextmsg(String textmsg) {
        this.textmsg = textmsg;
    }

    /** 
     *            @hibernate.property
     *             column="subject"
     *             length="256"
     *         
     */
    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    /** 
     *            @hibernate.property
     *             column="numericmsg"
     *             length="256"
     *         
     */
    public String getNumericmsg() {
        return this.numericmsg;
    }

    public void setNumericmsg(String numericmsg) {
        this.numericmsg = numericmsg;
    }

    /** 
     *            @hibernate.property
     *             column="pagetime"
     *             length="8"
     *         
     */
    public Date getPagetime() {
        return this.pagetime;
    }

    public void setPagetime(Date pagetime) {
        this.pagetime = pagetime;
    }

    /** 
     *            @hibernate.property
     *             column="respondtime"
     *             length="8"
     *         
     */
    public Date getRespondtime() {
        return this.respondtime;
    }

    public void setRespondtime(Date respondtime) {
        this.respondtime = respondtime;
    }

    /** 
     *            @hibernate.property
     *             column="answeredby"
     *             length="256"
     *         
     */
    public String getAnsweredby() {
        return this.answeredby;
    }

    public void setAnsweredby(String answeredby) {
        this.answeredby = answeredby;
    }

    /** 
     *            @hibernate.property
     *             column="interfaceid"
     *             length="16"
     *         
     */
    public String getInterfaceid() {
        return this.interfaceid;
    }

    public void setInterfaceid(String interfaceid) {
        this.interfaceid = interfaceid;
    }

    /** 
     *            @hibernate.property
     *             column="serviceid"
     *             length="4"
     *         
     */
    public Integer getServiceid() {
        return this.serviceid;
    }

    public void setServiceid(Integer serviceid) {
        this.serviceid = serviceid;
    }

    /** 
     *            @hibernate.property
     *             column="queueid"
     *             length="256"
     *         
     */
    public String getQueueid() {
        return this.queueid;
    }

    public void setQueueid(String queueid) {
        this.queueid = queueid;
    }

    /** 
     *            @hibernate.property
     *             column="eventuei"
     *             length="256"
     *             not-null="true"
     *         
     */
    public String getEventuei() {
        return this.eventuei;
    }

    public void setEventuei(String eventuei) {
        this.eventuei = eventuei;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="eventid"         
     *         
     */
    public org.opennms.netmgt.model.OnmsEvent getEvent() {
        return this.event;
    }

    public void setEvent(org.opennms.netmgt.model.OnmsEvent event) {
        this.event = event;
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
     *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.key
     *             column="notifyid"
     *            @hibernate.one-to-many
     *             class="org.opennms.netmgt.model.OnmsUserNotification"
     *             
     * old XDoclet1 Tags
     *            hibernate.collection-key
     *             column="notifyid"
     *            hibernate.collection-one-to-many
     *             class="org.opennms.netmgt.model.OnmsUserNotification"
     *         
     */
    public Set getUsersnotifieds() {
        return this.usersnotifieds;
    }

    public void setUsersnotifieds(Set usersnotifieds) {
        this.usersnotifieds = usersnotifieds;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("notifyid", getNotifyid())
            .toString();
    }

}
