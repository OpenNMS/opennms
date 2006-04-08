package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="events"
 *     
*/
public class OnmsEvent implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7412025003474162992L;

    /** identifier field */
    private Integer eventid;

    /** persistent field */
    private String eventuei;

    /** persistent field */
    private Date eventtime;

    /** nullable persistent field */
    private String eventhost;

    /** persistent field */
    private String eventsource;

    /** nullable persistent field */
    private String ipaddr;

    /** persistent field */
    private String eventdpname;

    /** nullable persistent field */
    private String eventsnmphost;

    /** nullable persistent field */
    private Integer serviceid;

    /** nullable persistent field */
    private String eventsnmp;

    /** nullable persistent field */
    private String eventparms;

    /** persistent field */
    private Date eventcreatetime;

    /** nullable persistent field */
    private String eventdescr;

    /** nullable persistent field */
    private String eventloggroup;

    /** nullable persistent field */
    private String eventlogmsg;

    /** persistent field */
    private int eventseverity;

    /** nullable persistent field */
    private String eventpathoutage;

    /** nullable persistent field */
    private String eventcorrelation;

    /** nullable persistent field */
    private Integer eventsuppressedcount;

    /** nullable persistent field */
    private String eventoperinstruct;

    /** nullable persistent field */
    private String eventautoaction;

    /** nullable persistent field */
    private String eventoperaction;

    /** nullable persistent field */
    private String eventoperactionmenutext;

    /** nullable persistent field */
    private String eventnotification;

    /** nullable persistent field */
    private String eventtticket;

    /** nullable persistent field */
    private Integer eventtticketstate;

    /** nullable persistent field */
    private String eventforward;

    /** nullable persistent field */
    private String eventmouseovertext;

    /** persistent field */
    private String eventlog;

    /** persistent field */
    private String eventdisplay;

    /** nullable persistent field */
    private String eventackuser;

    /** nullable persistent field */
    private Date eventacktime;

    /** nullable persistent field */
    private Integer alarmid;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsNode node;

    /** persistent field */
    private Set notifications;

    /** persistent field */
    private Set outagesBySvcregainedeventid;

    /** persistent field */
    private Set outagesBySvclosteventid;

    /** persistent field */
    private Set alarms;

    /** full constructor */
    public OnmsEvent(Integer eventid, String eventuei, Date eventtime, String eventhost, String eventsource, String ipaddr, String eventdpname, String eventsnmphost, Integer serviceid, String eventsnmp, String eventparms, Date eventcreatetime, String eventdescr, String eventloggroup, String eventlogmsg, int eventseverity, String eventpathoutage, String eventcorrelation, Integer eventsuppressedcount, String eventoperinstruct, String eventautoaction, String eventoperaction, String eventoperactionmenutext, String eventnotification, String eventtticket, Integer eventtticketstate, String eventforward, String eventmouseovertext, String eventlog, String eventdisplay, String eventackuser, Date eventacktime, Integer alarmid, org.opennms.netmgt.model.OnmsNode node, Set notifications, Set outagesBySvcregainedeventid, Set outagesBySvclosteventid, Set alarms) {
        this.eventid = eventid;
        this.eventuei = eventuei;
        this.eventtime = eventtime;
        this.eventhost = eventhost;
        this.eventsource = eventsource;
        this.ipaddr = ipaddr;
        this.eventdpname = eventdpname;
        this.eventsnmphost = eventsnmphost;
        this.serviceid = serviceid;
        this.eventsnmp = eventsnmp;
        this.eventparms = eventparms;
        this.eventcreatetime = eventcreatetime;
        this.eventdescr = eventdescr;
        this.eventloggroup = eventloggroup;
        this.eventlogmsg = eventlogmsg;
        this.eventseverity = eventseverity;
        this.eventpathoutage = eventpathoutage;
        this.eventcorrelation = eventcorrelation;
        this.eventsuppressedcount = eventsuppressedcount;
        this.eventoperinstruct = eventoperinstruct;
        this.eventautoaction = eventautoaction;
        this.eventoperaction = eventoperaction;
        this.eventoperactionmenutext = eventoperactionmenutext;
        this.eventnotification = eventnotification;
        this.eventtticket = eventtticket;
        this.eventtticketstate = eventtticketstate;
        this.eventforward = eventforward;
        this.eventmouseovertext = eventmouseovertext;
        this.eventlog = eventlog;
        this.eventdisplay = eventdisplay;
        this.eventackuser = eventackuser;
        this.eventacktime = eventacktime;
        this.alarmid = alarmid;
        this.node = node;
        this.notifications = notifications;
        this.outagesBySvcregainedeventid = outagesBySvcregainedeventid;
        this.outagesBySvclosteventid = outagesBySvclosteventid;
        this.alarms = alarms;
    }

    /** default constructor */
    public OnmsEvent() {
    }

    /** minimal constructor */
    public OnmsEvent(Integer eventid, String eventuei, Date eventtime, String eventsource, String eventdpname, Date eventcreatetime, int eventseverity, String eventlog, String eventdisplay, org.opennms.netmgt.model.OnmsNode node, Set notifications, Set outagesBySvcregainedeventid, Set outagesBySvclosteventid, Set alarms) {
        this.eventid = eventid;
        this.eventuei = eventuei;
        this.eventtime = eventtime;
        this.eventsource = eventsource;
        this.eventdpname = eventdpname;
        this.eventcreatetime = eventcreatetime;
        this.eventseverity = eventseverity;
        this.eventlog = eventlog;
        this.eventdisplay = eventdisplay;
        this.node = node;
        this.notifications = notifications;
        this.outagesBySvcregainedeventid = outagesBySvcregainedeventid;
        this.outagesBySvclosteventid = outagesBySvclosteventid;
        this.alarms = alarms;
    }

    /** 
     * @hibernate.id generator-class="assigned" type="java.lang.Integer" column="eventid"
     * @hibernate.generator-param name="sequence" value="eventsNxtId"
     */
    public Integer getEventid() {
        return this.eventid;
    }

    public void setEventid(Integer eventid) {
        this.eventid = eventid;
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
     *            @hibernate.property
     *             column="eventtime"
     *             length="8"
     *             not-null="true"
     *         
     */
    public Date getEventtime() {
        return this.eventtime;
    }

    public void setEventtime(Date eventtime) {
        this.eventtime = eventtime;
    }

    /** 
     *            @hibernate.property
     *             column="eventhost"
     *             length="256"
     *         
     */
    public String getEventhost() {
        return this.eventhost;
    }

    public void setEventhost(String eventhost) {
        this.eventhost = eventhost;
    }

    /** 
     *            @hibernate.property
     *             column="eventsource"
     *             length="128"
     *             not-null="true"
     *         
     */
    public String getEventsource() {
        return this.eventsource;
    }

    public void setEventsource(String eventsource) {
        this.eventsource = eventsource;
    }

    /** 
     *            @hibernate.property
     *             column="ipaddr"
     *             length="16"
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
     *             column="eventdpname"
     *             length="12"
     *             not-null="true"
     *         
     */
    public String getEventdpname() {
        return this.eventdpname;
    }

    public void setEventdpname(String eventdpname) {
        this.eventdpname = eventdpname;
    }

    /** 
     *            @hibernate.property
     *             column="eventsnmphost"
     *             length="256"
     *         
     */
    public String getEventsnmphost() {
        return this.eventsnmphost;
    }

    public void setEventsnmphost(String eventsnmphost) {
        this.eventsnmphost = eventsnmphost;
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
     *             column="eventsnmp"
     *             length="256"
     *         
     */
    public String getEventsnmp() {
        return this.eventsnmp;
    }

    public void setEventsnmp(String eventsnmp) {
        this.eventsnmp = eventsnmp;
    }

    /** 
     *            @hibernate.property
     *             column="eventparms"
     *             length="1024"
     *         
     */
    public String getEventparms() {
        return this.eventparms;
    }

    public void setEventparms(String eventparms) {
        this.eventparms = eventparms;
    }

    /** 
     *            @hibernate.property
     *             column="eventcreatetime"
     *             length="8"
     *             not-null="true"
     *         
     */
    public Date getEventcreatetime() {
        return this.eventcreatetime;
    }

    public void setEventcreatetime(Date eventcreatetime) {
        this.eventcreatetime = eventcreatetime;
    }

    /** 
     *            @hibernate.property
     *             column="eventdescr"
     *             length="4000"
     *         
     */
    public String getEventdescr() {
        return this.eventdescr;
    }

    public void setEventdescr(String eventdescr) {
        this.eventdescr = eventdescr;
    }

    /** 
     *            @hibernate.property
     *             column="eventloggroup"
     *             length="32"
     *         
     */
    public String getEventloggroup() {
        return this.eventloggroup;
    }

    public void setEventloggroup(String eventloggroup) {
        this.eventloggroup = eventloggroup;
    }

    /** 
     *            @hibernate.property
     *             column="eventlogmsg"
     *             length="256"
     *         
     */
    public String getEventlogmsg() {
        return this.eventlogmsg;
    }

    public void setEventlogmsg(String eventlogmsg) {
        this.eventlogmsg = eventlogmsg;
    }

    /** 
     *            @hibernate.property
     *             column="eventseverity"
     *             length="4"
     *             not-null="true"
     *         
     */
    public int getEventseverity() {
        return this.eventseverity;
    }

    public void setEventseverity(int eventseverity) {
        this.eventseverity = eventseverity;
    }

    /** 
     *            @hibernate.property
     *             column="eventpathoutage"
     *             length="1024"
     *         
     */
    public String getEventpathoutage() {
        return this.eventpathoutage;
    }

    public void setEventpathoutage(String eventpathoutage) {
        this.eventpathoutage = eventpathoutage;
    }

    /** 
     *            @hibernate.property
     *             column="eventcorrelation"
     *             length="1024"
     *         
     */
    public String getEventcorrelation() {
        return this.eventcorrelation;
    }

    public void setEventcorrelation(String eventcorrelation) {
        this.eventcorrelation = eventcorrelation;
    }

    /** 
     *            @hibernate.property
     *             column="eventsuppressedcount"
     *             length="4"
     *         
     */
    public Integer getEventsuppressedcount() {
        return this.eventsuppressedcount;
    }

    public void setEventsuppressedcount(Integer eventsuppressedcount) {
        this.eventsuppressedcount = eventsuppressedcount;
    }

    /** 
     *            @hibernate.property
     *             column="eventoperinstruct"
     *             length="1024"
     *         
     */
    public String getEventoperinstruct() {
        return this.eventoperinstruct;
    }

    public void setEventoperinstruct(String eventoperinstruct) {
        this.eventoperinstruct = eventoperinstruct;
    }

    /** 
     *            @hibernate.property
     *             column="eventautoaction"
     *             length="256"
     *         
     */
    public String getEventautoaction() {
        return this.eventautoaction;
    }

    public void setEventautoaction(String eventautoaction) {
        this.eventautoaction = eventautoaction;
    }

    /** 
     *            @hibernate.property
     *             column="eventoperaction"
     *             length="256"
     *         
     */
    public String getEventoperaction() {
        return this.eventoperaction;
    }

    public void setEventoperaction(String eventoperaction) {
        this.eventoperaction = eventoperaction;
    }

    /** 
     *            @hibernate.property
     *             column="eventoperactionmenutext"
     *             length="64"
     *         
     */
    public String getEventoperactionmenutext() {
        return this.eventoperactionmenutext;
    }

    public void setEventoperactionmenutext(String eventoperactionmenutext) {
        this.eventoperactionmenutext = eventoperactionmenutext;
    }

    /** 
     *            @hibernate.property
     *             column="eventnotification"
     *             length="128"
     *         
     */
    public String getEventnotification() {
        return this.eventnotification;
    }

    public void setEventnotification(String eventnotification) {
        this.eventnotification = eventnotification;
    }

    /** 
     *            @hibernate.property
     *             column="eventtticket"
     *             length="128"
     *         
     */
    public String getEventtticket() {
        return this.eventtticket;
    }

    public void setEventtticket(String eventtticket) {
        this.eventtticket = eventtticket;
    }

    /** 
     *            @hibernate.property
     *             column="eventtticketstate"
     *             length="4"
     *         
     */
    public Integer getEventtticketstate() {
        return this.eventtticketstate;
    }

    public void setEventtticketstate(Integer eventtticketstate) {
        this.eventtticketstate = eventtticketstate;
    }

    /** 
     *            @hibernate.property
     *             column="eventforward"
     *             length="256"
     *         
     */
    public String getEventforward() {
        return this.eventforward;
    }

    public void setEventforward(String eventforward) {
        this.eventforward = eventforward;
    }

    /** 
     *            @hibernate.property
     *             column="eventmouseovertext"
     *             length="64"
     *         
     */
    public String getEventmouseovertext() {
        return this.eventmouseovertext;
    }

    public void setEventmouseovertext(String eventmouseovertext) {
        this.eventmouseovertext = eventmouseovertext;
    }

    /** 
     *            @hibernate.property
     *             column="eventlog"
     *             length="1"
     *             not-null="true"
     *         
     */
    public String getEventlog() {
        return this.eventlog;
    }

    public void setEventlog(String eventlog) {
        this.eventlog = eventlog;
    }

    /** 
     *            @hibernate.property
     *             column="eventdisplay"
     *             length="1"
     *             not-null="true"
     *         
     */
    public String getEventdisplay() {
        return this.eventdisplay;
    }

    public void setEventdisplay(String eventdisplay) {
        this.eventdisplay = eventdisplay;
    }

    /** 
     *            @hibernate.property
     *             column="eventackuser"
     *             length="256"
     *         
     */
    public String getEventackuser() {
        return this.eventackuser;
    }

    public void setEventackuser(String eventackuser) {
        this.eventackuser = eventackuser;
    }

    /** 
     *            @hibernate.property
     *             column="eventacktime"
     *             length="8"
     *         
     */
    public Date getEventacktime() {
        return this.eventacktime;
    }

    public void setEventacktime(Date eventacktime) {
        this.eventacktime = eventacktime;
    }

    /** 
     *            @hibernate.property
     *             column="alarmid"
     *             length="4"
     *         
     */
    public Integer getAlarmid() {
        return this.alarmid;
    }

    public void setAlarmid(Integer alarmid) {
        this.alarmid = alarmid;
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
     *             column="eventid"
     *            @hibernate.one-to-many
     *             class="org.opennms.netmgt.model.OnmsNotification"
     *            
     * old XDoclet1 Tags
     *            hibernate.collection-key
     *             column="eventid"
     *            hibernate.collection-one-to-many
     *             class="org.opennms.netmgt.model.OnmsNotification"
     *         
     */
    public Set getNotifications() {
        return this.notifications;
    }

    public void setNotifications(Set notifications) {
        this.notifications = notifications;
    }

    /** 
     *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.key
     *             column="svcregainedeventid"
     *            @hibernate.one-to-many
     *             class="org.opennms.netmgt.model.OnmsOutage"
     *            
     * old XDoclet1 Tags
     *            hibernate.collection-key
     *             column="svcregainedeventid"
     *            hibernate.collection-one-to-many
     *             class="org.opennms.netmgt.model.OnmsOutage"
     *         
     */
    public Set getOutagesBySvcregainedeventid() {
        return this.outagesBySvcregainedeventid;
    }

    public void setOutagesBySvcregainedeventid(Set outagesBySvcregainedeventid) {
        this.outagesBySvcregainedeventid = outagesBySvcregainedeventid;
    }

    /** 
     *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.key
     *             column="svclosteventid"
     *            @hibernate.one-to-many
     *             class="org.opennms.netmgt.model.OnmsOutage"

     * old XDoclet1 Tags
     *            hibernate.collection-key
     *             column="svclosteventid"
     *            hibernate.collection-one-to-many
     *             class="org.opennms.netmgt.model.OnmsOutage"
     *         
     */
    public Set getOutagesBySvclosteventid() {
        return this.outagesBySvclosteventid;
    }

    public void setOutagesBySvclosteventid(Set outagesBySvclosteventid) {
        this.outagesBySvclosteventid = outagesBySvclosteventid;
    }

    /** 
     *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.key
     *             column="lasteventid"
     *            @hibernate.one-to-many
     *             class="org.opennms.netmgt.model.OnmsAlarm"

     * old XDoclet1 Tags
     *            hibernate.collection-key
     *             column="lasteventid"
     *            hibernate.collection-one-to-many
     *             class="org.opennms.netmgt.model.OnmsAlarm"
     *         
     */
    public Set getAlarms() {
        return this.alarms;
    }

    public void setAlarms(Set alarms) {
        this.alarms = alarms;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("eventid", getEventid())
            .toString();
    }

}
