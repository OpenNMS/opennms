//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.io.Serializable;
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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Category;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.springframework.core.style.ToStringCreator;

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
    private String m_ipAddr;

    /** identifier field */
    private String m_netMask;

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
    

    private OnmsNode m_node;

    private Set<OnmsIpInterface> m_ipInterfaces = new HashSet<OnmsIpInterface>();

    public OnmsSnmpInterface(String ipAddr, int ifIndex, OnmsNode node) {
        this(ipAddr, new Integer(ifIndex), node);
    }

    public OnmsSnmpInterface(String ipaddr, Integer ifIndex, OnmsNode node) {
        m_ipAddr = ipaddr == null ? "0.0.0.0" : ipaddr;
        m_ifIndex = ifIndex;
        m_node = node;
        if (node != null) {
            node.getSnmpInterfaces().add(this);
        }
    }

    /** default constructor */
    public OnmsSnmpInterface() {
    }

    /**
     * Unique identifier for snmpInterface.
     */
    @Id
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer id) {
        m_id = id;
    }

    /*
     * TODO this doesn't belong on SnmpInterface
     */
    @Column(name = "ipAddr", length = 16)
    public String getIpAddress() {
        return m_ipAddr;
    }

    public void setIpAddress(String ipaddr) {
        m_ipAddr = ipaddr;
    }

    /*
     * TODO this doesn't belong on SnmpInterface
     */
    @Column(name = "snmpIpAdEntNetMask", length = 16)
    public String getNetMask() {
        return m_netMask;
    }

    public void setNetMask(String snmpipadentnetmask) {
        m_netMask = snmpipadentnetmask;
    }

    @Column(name = "snmpPhysAddr", length = 16)
    public String getPhysAddr() {
        return m_physAddr;
    }

    public void setPhysAddr(String snmpphysaddr) {
        m_physAddr = snmpphysaddr;
    }

    @Column(name = "snmpIfIndex")
    public Integer getIfIndex() {
        return m_ifIndex;
    }

    public void setIfIndex(Integer snmpifindex) {
        m_ifIndex = snmpifindex;
    }

    @Column(name = "snmpIfDescr", length = 256)
    public String getIfDescr() {
        return m_ifDescr;
    }

    public void setIfDescr(String snmpifdescr) {
        m_ifDescr = snmpifdescr;
    }

    @Column(name = "snmpIfType")
    public Integer getIfType() {
        return m_ifType;
    }

    public void setIfType(Integer snmpiftype) {
        m_ifType = snmpiftype;
    }

    @Column(name = "snmpIfName", length = 32)
    public String getIfName() {
        return m_ifName;
    }

    public void setIfName(String snmpifname) {
        m_ifName = snmpifname;
    }

    @Column(name = "snmpIfSpeed")
    public Long getIfSpeed() {
        return m_ifSpeed;
    }

    public void setIfSpeed(Long snmpifspeed) {
        m_ifSpeed = snmpifspeed;
    }

    @Column(name = "snmpIfAdminStatus")
    public Integer getIfAdminStatus() {
        return m_ifAdminStatus;
    }

    public void setIfAdminStatus(Integer snmpifadminstatus) {
        m_ifAdminStatus = snmpifadminstatus;
    }

    @Column(name = "snmpIfOperStatus")
    public Integer getIfOperStatus() {
        return m_ifOperStatus;
    }

    public void setIfOperStatus(Integer snmpifoperstatus) {
        m_ifOperStatus = snmpifoperstatus;
    }

    @Column(name = "snmpIfAlias", length = 256)
    public String getIfAlias() {
        return m_ifAlias;
    }

    public void setIfAlias(String snmpifalias) {
        m_ifAlias = snmpifalias;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="snmpLastCapsdPoll")
    public Date getLastCapsdPoll() {
        return m_lastCapsdPoll;
    }
    
    public void setLastCapsdPoll(Date lastCapsdPoll) {
        m_lastCapsdPoll = lastCapsdPoll;
    }

    @XmlIDREF
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
    public OnmsNode getNode() {
        return m_node;
    }

    public void setNode(OnmsNode node) {
        m_node = node;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("ipaddr", getIpAddress())
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
            .append("lastCapsdPoll", getLastCapsdPoll())
            .toString();
    }

    public void visit(EntityVisitor visitor) {
        visitor.visitSnmpInterface(this);
        visitor.visitSnmpInterfaceComplete(this);
    }

    @XmlIDREF
    @OneToMany(mappedBy = "snmpInterface", fetch = FetchType.LAZY)
    public Set<OnmsIpInterface> getIpInterfaces() {
        return m_ipInterfaces;
    }

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

    @Transient
    public CollectionType getCollectionType() {
        CollectionType maxCollType = CollectionType.NO_COLLECT;
        for (OnmsIpInterface ipIface : getIpInterfaces()) {
            if (ipIface.getIsSnmpPrimary() != null) {
                maxCollType = maxCollType.max(ipIface.getIsSnmpPrimary());
            }
        }
        return maxCollType;
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

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

    public String computeLabelForRRD() {
        String name = computeNameForRRD();
        String physAddrForRRD = computePhysAddrForRRD();
        return (physAddrForRRD == null ? name : name + '-' + physAddrForRRD);
    }

    public void addIpInterface(OnmsIpInterface iface) {
        m_ipInterfaces.add(iface);
    }

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
        
        if (hasNewValue(scannedSnmpIface.getIpAddress(), getIpAddress())) {
            setIpAddress(scannedSnmpIface.getIpAddress());
        }
        
        if (hasNewValue(scannedSnmpIface.getNetMask(), getNetMask())) {
            setNetMask(scannedSnmpIface.getNetMask());
        }
        
        if (hasNewValue(scannedSnmpIface.getPhysAddr(), getPhysAddr())) {
            setPhysAddr(scannedSnmpIface.getPhysAddr());
        }
        
    }


}
