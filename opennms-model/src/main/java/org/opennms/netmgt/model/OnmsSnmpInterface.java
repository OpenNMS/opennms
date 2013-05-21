/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Type;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.RrdLabelUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.bind.InetAddressXmlAdapter;
import org.springframework.core.style.ToStringCreator;

/**
 * <p>OnmsSnmpInterface class.</p>
 */
@XmlRootElement(name = "snmpInterface")
@Entity
@Table(name = "snmpInterface")
public class OnmsSnmpInterface extends OnmsEntity implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -5396189389666285305L;

    private Integer m_id;

    /** identifier field */
    private InetAddress m_netMask;

    /** identifier field */
    private String m_physAddr;

    /** identifier field */
    private Integer m_ifIndex;

    /** identifier field */
    private String m_ifDescr;

    /** identifier field */
    private Integer m_ifType;

    /** identifier field */
    private String m_ifName;

    /** identifier field */
    private Long m_ifSpeed;

    /** identifier field */
    private Integer m_ifAdminStatus;

    /** identifier field */
    private Integer m_ifOperStatus;

    /** identifier field */
    private String m_ifAlias;
    
    private Date m_lastCapsdPoll;

    private String m_collect = "N";
    
    private String m_poll = "N";

    private Date m_lastSnmpPoll;

    private OnmsNode m_node;

    private Set<OnmsIpInterface> m_ipInterfaces = new HashSet<OnmsIpInterface>();

    /**
     * <p>Constructor for OnmsSnmpInterface.</p>
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @param ifIndex a int.
     */
    public OnmsSnmpInterface(OnmsNode node, int ifIndex) {
        this(node, new Integer(ifIndex));
    }

    /**
     * <p>Constructor for OnmsSnmpInterface.</p>
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @param ifIndex a {@link java.lang.Integer} object.
     */
    public OnmsSnmpInterface(OnmsNode node, Integer ifIndex) {
        m_ifIndex = ifIndex;
        m_node = node;
        if (node != null) {
            node.getSnmpInterfaces().add(this);
        }
    }

    /**
     * default constructor
     */
    public OnmsSnmpInterface() {
    }

    /**
     * Unique identifier for snmpInterface.
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(nullable=false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    @XmlAttribute(name="id")
    public Integer getId() {
        return m_id;
    }

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }

    /**
     * <p>getNetMask</p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "snmpIpAdEntNetMask")
    @Type(type="org.opennms.netmgt.model.InetAddressUserType")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getNetMask() {
        return m_netMask;
    }

    /**
     * <p>setNetMask</p>
     * 
     * @param snmpipadentnetmask a {@link java.lang.String} object.
     */
    public void setNetMask(InetAddress snmpipadentnetmask) {
        m_netMask = snmpipadentnetmask;
    }
    
    /**
     * <p>getPhysAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "snmpPhysAddr", length = 32)
    public String getPhysAddr() {
        return m_physAddr;
    }

    /**
     * <p>setPhysAddr</p>
     *
     * @param snmpphysaddr a {@link java.lang.String} object.
     */
    public void setPhysAddr(String snmpphysaddr) {
        m_physAddr = snmpphysaddr;
    }

    /**
     * <p>getIfIndex</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name = "snmpIfIndex")
    @XmlAttribute(name="ifIndex")
    public Integer getIfIndex() {
        return m_ifIndex;
    }

    /**
     * <p>setIfIndex</p>
     *
     * @param snmpifindex a {@link java.lang.Integer} object.
     */
    public void setIfIndex(Integer snmpifindex) {
        m_ifIndex = snmpifindex;
    }

    /**
     * <p>getIfDescr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "snmpIfDescr", length = 256)
    public String getIfDescr() {
        return m_ifDescr;
    }

    /**
     * <p>setIfDescr</p>
     *
     * @param snmpifdescr a {@link java.lang.String} object.
     */
    public void setIfDescr(String snmpifdescr) {
        m_ifDescr = snmpifdescr;
    }

    /**
     * <p>getIfType</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name = "snmpIfType")
    public Integer getIfType() {
        return m_ifType;
    }

    /**
     * <p>setIfType</p>
     *
     * @param snmpiftype a {@link java.lang.Integer} object.
     */
    public void setIfType(Integer snmpiftype) {
        m_ifType = snmpiftype;
    }

    /**
     * <p>getIfName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "snmpIfName", length = 32)
    public String getIfName() {
        return m_ifName;
    }

    /**
     * <p>setIfName</p>
     *
     * @param snmpifname a {@link java.lang.String} object.
     */
    public void setIfName(String snmpifname) {
        m_ifName = snmpifname;
    }

    /**
     * <p>getIfSpeed</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    @Column(name = "snmpIfSpeed")
    public Long getIfSpeed() {
        return m_ifSpeed;
    }

    /**
     * <p>setIfSpeed</p>
     *
     * @param snmpifspeed a {@link java.lang.Long} object.
     */
    public void setIfSpeed(Long snmpifspeed) {
        m_ifSpeed = snmpifspeed;
    }

    /**
     * <p>getIfAdminStatus</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name = "snmpIfAdminStatus")
    public Integer getIfAdminStatus() {
        return m_ifAdminStatus;
    }

    /**
     * <p>setIfAdminStatus</p>
     *
     * @param snmpifadminstatus a {@link java.lang.Integer} object.
     */
    public void setIfAdminStatus(Integer snmpifadminstatus) {
        m_ifAdminStatus = snmpifadminstatus;
    }

    /**
     * <p>getIfOperStatus</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name = "snmpIfOperStatus")
    public Integer getIfOperStatus() {
        return m_ifOperStatus;
    }

    /**
     * <p>setIfOperStatus</p>
     *
     * @param snmpifoperstatus a {@link java.lang.Integer} object.
     */
    public void setIfOperStatus(Integer snmpifoperstatus) {
        m_ifOperStatus = snmpifoperstatus;
    }

    /**
     * <p>getIfAlias</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "snmpIfAlias", length = 256)
    public String getIfAlias() {
        return m_ifAlias;
    }

    /**
     * <p>setIfAlias</p>
     *
     * @param snmpifalias a {@link java.lang.String} object.
     */
    public void setIfAlias(String snmpifalias) {
        m_ifAlias = snmpifalias;
    }
    
    /**
     * <p>getLastCapsdPoll</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="snmpLastCapsdPoll")
    public Date getLastCapsdPoll() {
        return m_lastCapsdPoll;
    }
    
    /**
     * <p>setLastCapsdPoll</p>
     *
     * @param lastCapsdPoll a {@link java.util.Date} object.
     */
    public void setLastCapsdPoll(Date lastCapsdPoll) {
        m_lastCapsdPoll = lastCapsdPoll;
    }
    
    /**
     * <p>getCollect</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="snmpCollect")
    @XmlAttribute(name="collectFlag")
    public String getCollect() {
        return m_collect;
    }
    
    /**
     * <p>setCollect</p>
     *
     * @param collect a {@link java.lang.String} object.
     */
    public void setCollect(String collect) {
        m_collect = collect;
    }

    /**
     * <p>getPoll</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="snmpPoll")
    @XmlAttribute(name="pollFlag")
    public String getPoll() {
        return m_poll;
    }
    
    /**
     * <p>setPoll</p>
     *
     * @param poll a {@link java.lang.String} object.
     */
    public void setPoll(String poll) {
        m_poll = poll;
    }

    /**
     * <p>getLastSnmpPoll</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="snmpLastSnmpPoll")
    public Date getLastSnmpPoll() {
        return m_lastSnmpPoll;
    }
    
    /**
     * <p>setLastSnmpPoll</p>
     *
     * @param lastSnmpPoll a {@link java.util.Date} object.
     */
    public void setLastSnmpPoll(Date lastSnmpPoll) {
        m_lastSnmpPoll = lastSnmpPoll;
    }

    /**
     * <p>isCollectionUserSpecified</p>
     *
     * @return a boolean.
     */
    @Transient
    public boolean isCollectionUserSpecified(){
        return m_collect.startsWith("U");
    }
    
    /**
     * <p>isCollectionEnabled</p>
     *
     * @return a boolean.
     */
    @Transient
    @XmlAttribute(name="collect")
    public boolean isCollectionEnabled() {
        return "C".equals(m_collect) || "UC".equals(m_collect);
    }
    
    /**
     * <p>setCollectionEnabled</p>
     *
     * @param shouldCollect a boolean.
     */
    public void setCollectionEnabled(boolean shouldCollect) {
        setCollectionEnabled(shouldCollect, false);
    }
    
    /**
     * <p>setCollectionEnabled</p>
     *
     * @param shouldCollect a boolean.
     * @param userSpecified a boolean.
     */
    public void setCollectionEnabled(boolean shouldCollect, boolean userSpecified){
       if(userSpecified){
           m_collect = shouldCollect ? "UC":"UN";
       }else if(!m_collect.startsWith("U")){
           m_collect = shouldCollect ? "C" : "N";
       }
    }

    /**
     * <p>isPollEnabled</p>
     *
     * @return a boolean.
     */
    @Transient
    @XmlAttribute(name="poll")
    public boolean isPollEnabled() {
        return "P".equals(m_poll);
    }

    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @XmlIDREF
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
    @XmlElement(name="nodeId")
    public OnmsNode getNode() {
        return m_node;
    }

    /**
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(OnmsNode node) {
        m_node = node;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringCreator(this)
            .append("snmpipadentnetmask", getNetMask())
            .append("snmpphysaddr", getPhysAddr())
            .append("snmpifindex", getIfIndex())
            .append("snmpifdescr", getIfDescr())
            .append("snmpiftype", getIfType())
            .append("snmpifname", getIfName())
            .append("snmpifspeed", getIfSpeed())
            .append("snmpifadminstatus", getIfAdminStatus())
            .append("snmpifoperstatus", getIfOperStatus())
            .append("snmpifalias", getIfAlias())
            .append("snmpCollect", getCollect())
            .append("snmpPoll", getPoll())
            .append("lastCapsdPoll", getLastCapsdPoll())
            .append("lastSnmpPoll", getLastSnmpPoll())
            .toString();
    }

    /** {@inheritDoc} */
    @Override
    public void visit(EntityVisitor visitor) {
        visitor.visitSnmpInterface(this);
        visitor.visitSnmpInterfaceComplete(this);
    }

    /**
     * <p>getIpInterfaces</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @XmlIDREF
    @OneToMany(mappedBy = "snmpInterface", fetch = FetchType.LAZY)
    public Set<OnmsIpInterface> getIpInterfaces() {
        return m_ipInterfaces;
    }

    /**
     * <p>setIpInterfaces</p>
     *
     * @param ipInterfaces a {@link java.util.Set} object.
     */
    public void setIpInterfaces(Set<OnmsIpInterface> ipInterfaces) {
        m_ipInterfaces = ipInterfaces;
    }

    // @Transient
    // public Set getIpInterfaces() {
    //		
    // Set ifsForSnmpIface = new LinkedHashSet();
    // for (Iterator it = getNode().getIpInterfaces().iterator();
    // it.hasNext();) {
    // OnmsIpInterface iface = (OnmsIpInterface) it.next();
    // if (getIfIndex().equals(iface.getIfIndex()))
    // ifsForSnmpIface.add(iface);
    // }
    // return ifsForSnmpIface;
    // }

    /**
     * <p>getPrimaryIpInterface</p>
     *
     * @return an {@link OnmsIpInterface} object.
     */
    @Transient
    @XmlTransient
    public OnmsIpInterface getPrimaryIpInterface() {
        return getNode().getPrimaryInterface();
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>computePhysAddrForRRD</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String computePhysAddrForRRD() {
        /*
         * In order to assure the uniqueness of the RRD file names we now
         * append the MAC/physical address to the end of label if it is
         * available.
         */
        String physAddrForRRD = null;

        if (getPhysAddr() != null) {
            String parsedPhysAddr = AlphaNumeric.parseAndTrim(getPhysAddr());
            if (parsedPhysAddr.length() == 12) {
                physAddrForRRD = parsedPhysAddr;
            } else {
                if (log().isDebugEnabled()) {
                    log().debug(
                                "physAddrForRRD: physical address len "
                                        + "is NOT 12, physAddr="
                                        + parsedPhysAddr);
                }
            }
        }
        log().debug(
                    "computed physAddr for " + this + " to be "
                            + physAddrForRRD);
        return physAddrForRRD;
    }

    /**
     * <p>computeNameForRRD</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String computeNameForRRD() {
        /*
         * Determine the label for this interface. The label will be used to
         * create the RRD file name which holds SNMP data retreived from the
         * remote agent. If available ifName is used to generate the label
         * since it is guaranteed to be unique. Otherwise ifDescr is used. In
         * either case, all non alpha numeric characters are converted to
         * underscores to ensure that the resuling string will make a decent
         * file name and that RRD won't have any problems using it
         */
        String label = null;
        if (getIfName() != null) {
            label = AlphaNumeric.parseAndReplace(getIfName(), '_');
        } else if (getIfDescr() != null) {
            label = AlphaNumeric.parseAndReplace(getIfDescr(), '_');
        } else {
            log().info(
                       "Interface ("
                               + this
                               + ") has no ifName and no ifDescr...setting to label to 'no_ifLabel'.");
            label = "no_ifLabel";
        }
        return label;
    }

    /**
     * <p>computeLabelForRRD</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String computeLabelForRRD() {
        return RrdLabelUtils.computeLabelForRRD(getIfName(), getIfDescr(), getPhysAddr());
    }

    /**
     * <p>addIpInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    public void addIpInterface(OnmsIpInterface iface) {
        m_ipInterfaces.add(iface);
    }

    /**
     * <p>mergeSnmpInterfaceAttributes</p>
     *
     * @param scannedSnmpIface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    public void mergeSnmpInterfaceAttributes(OnmsSnmpInterface scannedSnmpIface) {
        
        if (hasNewValue(scannedSnmpIface.getIfAdminStatus(), getIfAdminStatus())) {
            setIfAdminStatus(scannedSnmpIface.getIfAdminStatus());
        }
        
        if (hasNewValue(scannedSnmpIface.getIfAlias(), getIfAlias())) {
            setIfAlias(scannedSnmpIface.getIfAlias());
        }
        
        if (hasNewValue(scannedSnmpIface.getIfDescr(), getIfDescr())) {
            setIfDescr(scannedSnmpIface.getIfDescr());
        }
            
        if (hasNewValue(scannedSnmpIface.getIfName(), getIfName())) {
            setIfName(scannedSnmpIface.getIfName());
        }
        
        if (hasNewValue(scannedSnmpIface.getIfOperStatus(), getIfOperStatus())) {
            setIfOperStatus(scannedSnmpIface.getIfOperStatus());
        }
        
        if (hasNewValue(scannedSnmpIface.getIfSpeed(), getIfSpeed())) {
            setIfSpeed(scannedSnmpIface.getIfSpeed());
        }
        
        if (hasNewValue(scannedSnmpIface.getIfType(), getIfType())) {
            setIfType(scannedSnmpIface.getIfType());
        }
        
        if (hasNewValue(scannedSnmpIface.getNetMask(), getNetMask())) {
            setNetMask(scannedSnmpIface.getNetMask());
        }
        
        if (hasNewValue(scannedSnmpIface.getPhysAddr(), getPhysAddr())) {
            setPhysAddr(scannedSnmpIface.getPhysAddr());
        }
        
        if (hasNewValue(scannedSnmpIface.getLastCapsdPoll(), getLastCapsdPoll())) {
            setLastCapsdPoll(scannedSnmpIface.getLastCapsdPoll());
        }
        
        if (hasNewValue(scannedSnmpIface.getPoll(), getPoll())) {
            setPoll(scannedSnmpIface.getPoll());
        }

        if (hasNewValue(scannedSnmpIface.getLastSnmpPoll(), getLastSnmpPoll())) {
            setLastSnmpPoll(scannedSnmpIface.getLastSnmpPoll());
        }
        
        if(scannedSnmpIface.isCollectionUserSpecified() || !isCollectionUserSpecified()){
            setCollectionEnabled(scannedSnmpIface.isCollectionEnabled(), scannedSnmpIface.isCollectionUserSpecified());
        }
        
    }

}
