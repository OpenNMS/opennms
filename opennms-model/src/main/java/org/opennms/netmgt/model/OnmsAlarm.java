package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="alarms"
 *     
*/
public class OnmsAlarm implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1342362989494090682L;

    /** identifier field */
    private Integer alarmid;

    /** persistent field */
    private String eventuei;

    /** persistent field */
    private String dpname;

    /** nullable persistent field */
    private Integer nodeid;

    /** nullable persistent field */
    private String ipaddr;

    /** nullable persistent field */
    private Integer serviceid;

    /** nullable persistent field */
    private String reductionkey;

    /** nullable persistent field */
    private Integer alarmtype;

    /** persistent field */
    private int counter;

    /** persistent field */
    private int severity;

    /** persistent field */
    private Date firsteventtime;

    /** persistent field */
    private Date lasteventtime;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private String logmsg;

    /** nullable persistent field */
    private String operinstruct;

    /** nullable persistent field */
    private String tticketid;

    /** nullable persistent field */
    private Integer tticketstate;

    /** nullable persistent field */
    private String mouseovertext;

    /** nullable persistent field */
    private Date suppresseduntil;

    /** nullable persistent field */
    private String suppresseduser;

    /** nullable persistent field */
    private Date suppressedtime;

    /** nullable persistent field */
    private String alarmackuser;

    /** nullable persistent field */
    private Date alarmacktime;

    /** nullable persistent field */
    private String clearuei;

    /** persistent field */
    private org.opennms.netmgt.model.OnmsEvent event;

    /** full constructor */
    public OnmsAlarm(Integer alarmid, String eventuei, String dpname, Integer nodeid, String ipaddr, Integer serviceid, String reductionkey, Integer alarmtype, int counter, int severity, Date firsteventtime, Date lasteventtime, String description, String logmsg, String operinstruct, String tticketid, Integer tticketstate, String mouseovertext, Date suppresseduntil, String suppresseduser, Date suppressedtime, String alarmackuser, Date alarmacktime, String clearuei, org.opennms.netmgt.model.OnmsEvent event) {
        this.alarmid = alarmid;
        this.eventuei = eventuei;
        this.dpname = dpname;
        this.nodeid = nodeid;
        this.ipaddr = ipaddr;
        this.serviceid = serviceid;
        this.reductionkey = reductionkey;
        this.alarmtype = alarmtype;
        this.counter = counter;
        this.severity = severity;
        this.firsteventtime = firsteventtime;
        this.lasteventtime = lasteventtime;
        this.description = description;
        this.logmsg = logmsg;
        this.operinstruct = operinstruct;
        this.tticketid = tticketid;
        this.tticketstate = tticketstate;
        this.mouseovertext = mouseovertext;
        this.suppresseduntil = suppresseduntil;
        this.suppresseduser = suppresseduser;
        this.suppressedtime = suppressedtime;
        this.alarmackuser = alarmackuser;
        this.alarmacktime = alarmacktime;
        this.clearuei = clearuei;
        this.event = event;
    }

    /** default constructor */
    public OnmsAlarm() {
    }

    /** minimal constructor */
    public OnmsAlarm(Integer alarmid, String eventuei, String dpname, int counter, int severity, Date firsteventtime, Date lasteventtime, org.opennms.netmgt.model.OnmsEvent event) {
        this.alarmid = alarmid;
        this.eventuei = eventuei;
        this.dpname = dpname;
        this.counter = counter;
        this.severity = severity;
        this.firsteventtime = firsteventtime;
        this.lasteventtime = lasteventtime;
        this.event = event;
    }

    /** 
     *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="alarmid"
     *         
     */
    public Integer getAlarmid() {
        return this.alarmid;
    }

    public void setAlarmid(Integer alarmid) {
        this.alarmid = alarmid;
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
     *             column="dpname"
     *             length="12"
     *             not-null="true"
     *         
     */
    public String getDpname() {
        return this.dpname;
    }

    public void setDpname(String dpname) {
        this.dpname = dpname;
    }

    /** 
     *            @hibernate.property
     *             column="nodeid"
     *             length="4"
     *         
     */
    public Integer getNodeid() {
        return this.nodeid;
    }

    public void setNodeid(Integer nodeid) {
        this.nodeid = nodeid;
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
     *             column="reductionkey"
     *             unique="true"
     *             length="256"
     *         
     */
    public String getReductionkey() {
        return this.reductionkey;
    }

    public void setReductionkey(String reductionkey) {
        this.reductionkey = reductionkey;
    }

    /** 
     *            @hibernate.property
     *             column="alarmtype"
     *             length="4"
     *         
     */
    public Integer getAlarmtype() {
        return this.alarmtype;
    }

    public void setAlarmtype(Integer alarmtype) {
        this.alarmtype = alarmtype;
    }

    /** 
     *            @hibernate.property
     *             column="counter"
     *             length="4"
     *             not-null="true"
     *         
     */
    public int getCounter() {
        return this.counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    /** 
     *            @hibernate.property
     *             column="severity"
     *             length="4"
     *             not-null="true"
     *         
     */
    public int getSeverity() {
        return this.severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    /** 
     *            @hibernate.property
     *             column="firsteventtime"
     *             length="8"
     *             not-null="true"
     *         
     */
    public Date getFirsteventtime() {
        return this.firsteventtime;
    }

    public void setFirsteventtime(Date firsteventtime) {
        this.firsteventtime = firsteventtime;
    }

    /** 
     *            @hibernate.property
     *             column="lasteventtime"
     *             length="8"
     *             not-null="true"
     *         
     */
    public Date getLasteventtime() {
        return this.lasteventtime;
    }

    public void setLasteventtime(Date lasteventtime) {
        this.lasteventtime = lasteventtime;
    }

    /** 
     *            @hibernate.property
     *             column="description"
     *             length="4000"
     *         
     */
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** 
     *            @hibernate.property
     *             column="logmsg"
     *             length="256"
     *         
     */
    public String getLogmsg() {
        return this.logmsg;
    }

    public void setLogmsg(String logmsg) {
        this.logmsg = logmsg;
    }

    /** 
     *            @hibernate.property
     *             column="operinstruct"
     *             length="1024"
     *         
     */
    public String getOperinstruct() {
        return this.operinstruct;
    }

    public void setOperinstruct(String operinstruct) {
        this.operinstruct = operinstruct;
    }

    /** 
     *            @hibernate.property
     *             column="tticketid"
     *             length="128"
     *         
     */
    public String getTticketid() {
        return this.tticketid;
    }

    public void setTticketid(String tticketid) {
        this.tticketid = tticketid;
    }

    /** 
     *            @hibernate.property
     *             column="tticketstate"
     *             length="4"
     *         
     */
    public Integer getTticketstate() {
        return this.tticketstate;
    }

    public void setTticketstate(Integer tticketstate) {
        this.tticketstate = tticketstate;
    }

    /** 
     *            @hibernate.property
     *             column="mouseovertext"
     *             length="64"
     *         
     */
    public String getMouseovertext() {
        return this.mouseovertext;
    }

    public void setMouseovertext(String mouseovertext) {
        this.mouseovertext = mouseovertext;
    }

    /** 
     *            @hibernate.property
     *             column="suppresseduntil"
     *             length="8"
     *         
     */
    public Date getSuppresseduntil() {
        return this.suppresseduntil;
    }

    public void setSuppresseduntil(Date suppresseduntil) {
        this.suppresseduntil = suppresseduntil;
    }

    /** 
     *            @hibernate.property
     *             column="suppresseduser"
     *             length="256"
     *         
     */
    public String getSuppresseduser() {
        return this.suppresseduser;
    }

    public void setSuppresseduser(String suppresseduser) {
        this.suppresseduser = suppresseduser;
    }

    /** 
     *            @hibernate.property
     *             column="suppressedtime"
     *             length="8"
     *         
     */
    public Date getSuppressedtime() {
        return this.suppressedtime;
    }

    public void setSuppressedtime(Date suppressedtime) {
        this.suppressedtime = suppressedtime;
    }

    /** 
     *            @hibernate.property
     *             column="alarmackuser"
     *             length="256"
     *         
     */
    public String getAlarmackuser() {
        return this.alarmackuser;
    }

    public void setAlarmackuser(String alarmackuser) {
        this.alarmackuser = alarmackuser;
    }

    /** 
     *            @hibernate.property
     *             column="alarmacktime"
     *             length="8"
     *         
     */
    public Date getAlarmacktime() {
        return this.alarmacktime;
    }

    public void setAlarmacktime(Date alarmacktime) {
        this.alarmacktime = alarmacktime;
    }

    /** 
     *            @hibernate.property
     *             column="clearuei"
     *             length="256"
     *         
     */
    public String getClearuei() {
        return this.clearuei;
    }

    public void setClearuei(String clearuei) {
        this.clearuei = clearuei;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="lasteventid"         
     *         
     */
    public org.opennms.netmgt.model.OnmsEvent getEvent() {
        return this.event;
    }

    public void setEvent(org.opennms.netmgt.model.OnmsEvent event) {
        this.event = event;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("alarmid", getAlarmid())
            .toString();
    }

}
