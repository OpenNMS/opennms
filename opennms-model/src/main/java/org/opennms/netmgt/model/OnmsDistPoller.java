package org.opennms.netmgt.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.springframework.core.style.ToStringCreator;


/** 
 * Represents an OpenNMS Distributed Poller
 * 
 * @hibernate.class table="distpoller"
 *     
*/
public class OnmsDistPoller implements Serializable {

    private static final long serialVersionUID = -1094353783612066524L;

    /** identifier field */
    private String m_name;

    /** persistent field */
    private String m_ipAddress;

    /** nullable persistent field */
    private String m_comment;

    /** nullable persistent field */
    private BigDecimal m_discoveryLimit;

    /** nullable persistent field */
    private Date m_lastNodePull;

    /** nullable persistent field */
    private Date m_lastEventPull;

    /** nullable persistent field */
    private Date m_lastPackagePush;

    /** nullable persistent field */
    private Integer m_adminState;

    /** nullable persistent field */
    private Integer m_runState;

    /** default constructor */
    public OnmsDistPoller() {}
    
    /** minimal constructor */
    public OnmsDistPoller(String name, String ipAddress) {
        m_name = name;
        m_ipAddress = ipAddress;
    }

    /** 
     * A human-readable name for each system.
     * Typically, the system's hostname (not fully qualified).
     * 
     * @hibernate.id generator-class="assigned" column="dpname" length="12"
     *         
     */
    public String getName() {
        return m_name;
    }

    public void setName(String dpname) {
        m_name = dpname;
    }

    /**
     * IP address of the distributed poller.
     *  
     * @hibernate.property column="dpip" length="16" not-null="true"
     *         
     */
    public String getIpAddress() {
        return m_ipAddress;
    }

    public void setIpAddress(String dpip) {
        m_ipAddress = dpip;
    }

    /** 
     * A free form text field providing a desciption of the distrubted poller
     * 
     * @hibernate.property column="dpcomment" length="256"
     *         
     */
    public String getComment() {
        return m_comment;
    }

    public void setComment(String dpcomment) {
        m_comment = dpcomment;
    }

    /** 
     * Numeric representation of percentage of interface speed available to discovery
     * process.  See documentation for "bandwidth troll"
     * 
     * @hibernate.property column="dpdisclimit" length="5"
     *         
     */
    public BigDecimal getDiscoveryLimit() {
        return m_discoveryLimit;
    }

    public void setDiscoveryLimit(BigDecimal dpdisclimit) {
        m_discoveryLimit = dpdisclimit;
    }

    /**
     * Time of last pull of new nodes from the DP
     * 
     * @hibernate.property column="dplastnodepull" length="8"
     *         
     */
    public Date getLastNodePull() {
        return m_lastNodePull;
    }

    public void setLastNodePull(Date dplastnodepull) {
        m_lastNodePull = dplastnodepull;
    }

    /**
     * Time of last pull of events from the DP
     * 
     * @hibernate.property column="dplasteventpull" length="8"
     *         
     */
    public Date getLastEventPull() {
        return m_lastEventPull;
    }

    public void setLastEventPull(Date dplasteventpull) {
        m_lastEventPull = dplasteventpull;
    }

    /** 
     * Time of last push of Package (config) to the DP
     *
     * @hibernate.property column="dplastpackagepush" length="8"
     *         
     */
    public Date getLastPackagePush() {
        return m_lastPackagePush;
    }

    public void setLastPackagePush(Date dplastpackagepush) {
        m_lastPackagePush = dplastpackagepush;
    }

    /** 
     * Reflects desired state for this distributed poller. 1 = Up, 0 = Down
     * 
     * @hibernate.property column="dpadminstate" length="4"
     *         
     */
    public Integer getAdminState() {
        return m_adminState;
    }

    public void setAdminState(Integer dpadminstate) {
        m_adminState = dpadminstate;
    }

    /**
     * Reflects the current perceived state of the distributed 
     * poller.  1 = Up, 0 = Down
     * 
     * @hibernate.property column="dprunstate" length="4"
     *         
     */
    public Integer getRunState() {
        return m_runState;
    }

    public void setRunState(Integer dprunstate) {
        m_runState = dprunstate;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("name", getName())
            .toString();
    }

}
