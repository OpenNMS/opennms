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
public class OnmsNotification extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1162549324168290004L;

    /** identifier field */
    private Integer m_notifyId;

    /** persistent field */
    private String m_textMsg;

    /** nullable persistent field */
    private String m_subject;

    /** nullable persistent field */
    private String m_numericMsg;

    /** nullable persistent field */
    private Date m_pageTime;

    /** nullable persistent field */
    private Date m_respondTime;

    /** nullable persistent field */
    private String m_answeredBy;

    /** nullable persistent field */
    private OnmsIpInterface m_interface;

    /** nullable persistent field */
    private OnmsMonitoredService m_service;

    /** nullable persistent field */
    private String m_queueId;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsEvent m_event;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsNode m_node;

    /** persistent field */
    private Set m_usersNotified;

    /** full constructor */
    public OnmsNotification(Integer notifyId, String textMsg, String subject, String numericMsg, Date pageTime, Date respondTime, String answeredBy, OnmsIpInterface ipInterface, OnmsMonitoredService service, String queueId, org.opennms.netmgt.model.OnmsEvent event, org.opennms.netmgt.model.OnmsNode node, Set usersNotified) {
        m_notifyId = notifyId;
        m_textMsg = textMsg;
        m_subject = subject;
        m_numericMsg = numericMsg;
        m_pageTime = pageTime;
        m_respondTime = respondTime;
        m_answeredBy = answeredBy;
        m_interface = ipInterface;
        m_service = service;
        m_queueId = queueId;
        m_event = event;
        m_node = node;
        m_usersNotified = usersNotified;
    }

    /** default constructor */
    public OnmsNotification() {
    }

    /** minimal constructor */
    public OnmsNotification(Integer notifyId, String textMsg, org.opennms.netmgt.model.OnmsEvent event, org.opennms.netmgt.model.OnmsNode node, Set usersNotified) {
        m_notifyId = notifyId;
        m_textMsg = textMsg;
        m_event = event;
        m_node = node;
        m_usersNotified = usersNotified;
    }

    /** 
     * @hibernate.id generator-class="native" type="java.lang.Integer" column="notifyid"
     * @hibernate.generator-param name="sequence" value="notifyNxtId"
     */
    public Integer getNotifyId() {
        return m_notifyId;
    }

    public void setNotifyId(Integer notifyid) {
        m_notifyId = notifyid;
    }

    /** 
     *            @hibernate.property
     *             column="textmsg"
     *             length="4000"
     *             not-null="true"
     *         
     */
    public String getTextMsg() {
        return m_textMsg;
    }

    public void setTextMsg(String textmsg) {
        m_textMsg = textmsg;
    }

    /** 
     *            @hibernate.property
     *             column="subject"
     *             length="256"
     *         
     */
    public String getSubject() {
        return m_subject;
    }

    public void setSubject(String subject) {
        m_subject = subject;
    }

    /** 
     *            @hibernate.property
     *             column="numericmsg"
     *             length="256"
     *         
     */
    public String getNumericMsg() {
        return m_numericMsg;
    }

    public void setNumericMsg(String numericmsg) {
        m_numericMsg = numericmsg;
    }

    /** 
     *            @hibernate.property
     *             column="pagetime"
     *             length="8"
     *         
     */
    public Date getPageTime() {
        return m_pageTime;
    }

    public void setPageTime(Date pagetime) {
        m_pageTime = pagetime;
    }

    /** 
     *            @hibernate.property
     *             column="respondtime"
     *             length="8"
     *         
     */
    public Date getRespondTime() {
        return m_respondTime;
    }

    public void setRespondTime(Date respondtime) {
        m_respondTime = respondtime;
    }

    /** 
     *            @hibernate.property
     *             column="answeredby"
     *             length="256"
     *         
     */
    public String getAnsweredBy() {
        return m_answeredBy;
    }

    public void setAnsweredBy(String answeredby) {
        m_answeredBy = answeredby;
    }

    /** 
     *            @hibernate.property
     *             column="interfaceid"
     *             length="16"
     *         
     */
    public OnmsIpInterface getInterface() {
        return m_interface;
    }

    public void setInterface(OnmsIpInterface interfaceId) {
        m_interface = interfaceId;
    }

    /** 
     *            @hibernate.property
     *             column="serviceid"
     *             length="4"
     *         
     */
    public OnmsMonitoredService getService() {
        return m_service;
    }

    public void setService(OnmsMonitoredService serviceId) {
        m_service = serviceId;
    }

    /** 
     *            @hibernate.property
     *             column="queueid"
     *             length="256"
     *         
     */
    public String getQueueId() {
        return m_queueId;
    }

    public void setQueueId(String queueid) {
        m_queueId = queueid;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="eventid"         
     *         
     */
    public org.opennms.netmgt.model.OnmsEvent getEvent() {
        return m_event;
    }

    public void setEvent(org.opennms.netmgt.model.OnmsEvent event) {
        m_event = event;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="nodeid"         
     *         
     */
    public org.opennms.netmgt.model.OnmsNode getNode() {
        return m_node;
    }

    public void setNode(org.opennms.netmgt.model.OnmsNode node) {
        m_node = node;
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
    public Set getUsersNotified() {
        return m_usersNotified;
    }

    public void setUsersNotified(Set usersnotifieds) {
        m_usersNotified = usersnotifieds;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("notifyid", getNotifyId())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		throw new RuntimeException("visitor method not implemented");
	}

}
