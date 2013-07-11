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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Type;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.bind.InetAddressXmlAdapter;
import org.opennms.netmgt.model.events.AddEventVisitor;
import org.opennms.netmgt.model.events.DeleteEventVisitor;
import org.opennms.netmgt.model.events.EventForwarder;
import org.springframework.core.style.ToStringCreator;

/**
 * <p>OnmsIpInterface class.</p>
 */
@XmlRootElement(name = "ipInterface")
@Entity
@Table(name="ipInterface")
public class OnmsIpInterface extends OnmsEntity implements Serializable {
    
    private static final long serialVersionUID = 7750043250236397014L;

    private Integer m_id;

    private InetAddress m_ipAddress;

    private String m_ipHostName;

    private String m_isManaged;

    private PrimaryType m_isSnmpPrimary = PrimaryType.NOT_ELIGIBLE;

    private Date m_ipLastCapsdPoll;

    private OnmsNode m_node;

    private Set<OnmsMonitoredService> m_monitoredServices = new HashSet<OnmsMonitoredService>();

    private OnmsSnmpInterface m_snmpInterface;

    /**
     * <p>Constructor for OnmsIpInterface.</p>
     */
    public OnmsIpInterface() {
    }

    /**
     * minimal constructor
     * @deprecated Use the {@link InetAddress} version instead.
     * @param ipAddr a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public OnmsIpInterface(String ipAddr, OnmsNode node) {
        this(InetAddressUtils.getInetAddress(ipAddr), node);
    }

    /**
     * minimal constructor
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public OnmsIpInterface(InetAddress ipAddr, OnmsNode node) {
        m_ipAddress = ipAddr;
        m_node = node;
        if (node != null) {
            node.getIpInterfaces().add(this);
        }
    }

    /**
     * Unique identifier for ipInterface.
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(nullable=false)
    @XmlTransient
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }
    
    /**
     * <p>getInterfaceId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlID
    @XmlAttribute(name="id")
    @Transient
    public String getInterfaceId() {
        return getId().toString();
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
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     * @deprecated
     */
    @Transient
    public String getIpAddressAsString() {
        return InetAddressUtils.toIpAddrString(m_ipAddress);
    }

    //@Column(name="ifIndex")
    /**
     * <p>getIfIndex</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Transient
    @XmlAttribute(name="ifIndex")
    public Integer getIfIndex() {
        if (m_snmpInterface == null) {
            return null;
        }
        return m_snmpInterface.getIfIndex();
        //return m_ifIndex;
    }

    /**
     * <p>setIfIndex</p>
     *
     * @param ifindex a {@link java.lang.Integer} object.
     */
    public void setIfIndex(Integer ifindex) {
        if (m_snmpInterface == null) {
            throw new IllegalStateException("Cannot set ifIndex if snmpInterface relation isn't setup");
        }
        m_snmpInterface.setIfIndex(ifindex);
        //m_ifIndex = ifindex;
    }

    /**
     * <p>getIpHostName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="ipHostName", length=256)
    @XmlElement(name="hostName")
    public String getIpHostName() {
        return m_ipHostName;
    }

    /**
     * <p>setIpHostName</p>
     *
     * @param iphostname a {@link java.lang.String} object.
     */
    public void setIpHostName(String iphostname) {
        m_ipHostName = iphostname;
    }

    /**
     * <p>getIsManaged</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="isManaged", length=1)
    @XmlAttribute(name="isManaged")
    public String getIsManaged() {
        return m_isManaged;
    }

    /**
     * <p>setIsManaged</p>
     *
     * @param ismanaged a {@link java.lang.String} object.
     */
    public void setIsManaged(String ismanaged) {
        m_isManaged = ismanaged;
    }

    /**
     * <p>isManaged</p>
     *
     * @return a boolean.
     */
    @Transient
    public boolean isManaged() {
        return "M".equals(getIsManaged());
    }

    /**
     * <p>getIpLastCapsdPoll</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ipLastCapsdPoll")
    @XmlElement(name="lastCapsdPoll")
    public Date getIpLastCapsdPoll() {
        return m_ipLastCapsdPoll;
    }

    /**
     * <p>setIpLastCapsdPoll</p>
     *
     * @param iplastcapsdpoll a {@link java.util.Date} object.
     */
    public void setIpLastCapsdPoll(Date iplastcapsdpoll) {
        m_ipLastCapsdPoll = iplastcapsdpoll;
    }

    /**
     * <p>getPrimaryString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    @XmlAttribute(name="snmpPrimary")
    public String getPrimaryString() {
        return m_isSnmpPrimary == null? null : m_isSnmpPrimary.toString();
    }
    /**
     * <p>setPrimaryString</p>
     *
     * @param primaryType a {@link java.lang.String} object.
     */
    public void setPrimaryString(String primaryType) {
        m_isSnmpPrimary = new PrimaryType(primaryType.charAt(0));
    }
    
    /**
     * <p>getIsSnmpPrimary</p>
     *
     * @return a {@link org.opennms.netmgt.model.PrimaryType} object.
     */
    @Column(name="isSnmpPrimary", length=1)
    @XmlTransient
    public PrimaryType getIsSnmpPrimary() {
        return m_isSnmpPrimary;
    }

    /**
     * <p>setIsSnmpPrimary</p>
     *
     * @param issnmpprimary a {@link org.opennms.netmgt.model.PrimaryType} object.
     */
    public void setIsSnmpPrimary(PrimaryType issnmpprimary) {
        m_isSnmpPrimary = issnmpprimary;
    }
    
    /**
     * <p>isPrimary</p>
     *
     * @return a boolean.
     */
    @Transient
    public boolean isPrimary(){
        return m_isSnmpPrimary.equals(PrimaryType.PRIMARY);
    }

    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    @XmlElement(name="nodeId")
    @XmlIDREF
    public OnmsNode getNode() {
        return m_node;
    }

    /**
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(org.opennms.netmgt.model.OnmsNode node) {
        m_node = node;
    }

    /**
     * The services on this node
     *
     * @return a {@link java.util.Set} object.
     */
    @XmlTransient
    @OneToMany(mappedBy="ipInterface",orphanRemoval=true)
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
    public Set<OnmsMonitoredService> getMonitoredServices() {
        return m_monitoredServices ;
    }

    /**
     * <p>setMonitoredServices</p>
     *
     * @param ifServices a {@link java.util.Set} object.
     */
    public void setMonitoredServices(Set<OnmsMonitoredService> ifServices) {
        m_monitoredServices = ifServices;
    }


    /**
     * The SnmpInterface associated with this interface if any
     *
     * @return a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    @XmlElement(name = "snmpInterface")
    @ManyToOne(optional=true, fetch=FetchType.LAZY)
    @JoinColumn(name="snmpInterfaceId")
    public OnmsSnmpInterface getSnmpInterface() {
        return m_snmpInterface;
    }


    /**
     * <p>setSnmpInterface</p>
     *
     * @param snmpInterface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    public void setSnmpInterface(OnmsSnmpInterface snmpInterface) {
        m_snmpInterface = snmpInterface;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringCreator(this)
        .append("id", m_id)
        .append("ipAddr", InetAddressUtils.str(m_ipAddress))
        .append("ipHostName", m_ipHostName)
        .append("isManaged", m_isManaged)
        .append("isSnmpPrimary", m_isSnmpPrimary)
        .append("ipLastCapsdPoll", m_ipLastCapsdPoll)
        .toString();
    }

    /** {@inheritDoc} */
    @Override
    public void visit(EntityVisitor visitor) {
        visitor.visitIpInterface(this);

        for (OnmsMonitoredService monSvc : getMonitoredServices()) {
            monSvc.visit(visitor);
        }

        visitor.visitIpInterfaceComplete(this);
    }

    /**
     * <p>getInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    @Column(name="ipAddr")
    @XmlElement(name="ipAddress")
    @Type(type="org.opennms.netmgt.model.InetAddressUserType")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getIpAddress() {
        return m_ipAddress;
    }

    /**
     * <p>setInetAddress</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     */
    public void setIpAddress(InetAddress ipaddr) {
        m_ipAddress = ipaddr;
    }

    /**
     * <p>isDown</p>
     *
     * @return a boolean.
     */
    @Transient
    @XmlAttribute(name="isDown")
    public boolean isDown() {
        boolean down = true;
        for (OnmsMonitoredService svc : m_monitoredServices) {
            if (!svc.isDown()) {
                return !down;
            }
        }
        return down;
    }

    
    @Transient 
    @XmlAttribute
    public int getMonitoredServiceCount () {
    	return m_monitoredServices.size();
    }
    
    /**
     * <p>getMonitoredServiceByServiceType</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    public OnmsMonitoredService getMonitoredServiceByServiceType(String svcName) {
        for (OnmsMonitoredService monSvc : getMonitoredServices()) {
            if (monSvc.getServiceType().getName().equals(svcName)) {
                return monSvc;
            }
        }
        return null;
    }

    /**
     * <p>mergeInterfaceAttributes</p>
     *
     * @param scannedIface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    public void mergeInterfaceAttributes(OnmsIpInterface scannedIface) {
        
        if (hasNewValue(scannedIface.getIsManaged(), getIsManaged())) {
            setIsManaged(scannedIface.getIsManaged());
        }
    
        if (hasNewCollectionTypeValue(scannedIface.getIsSnmpPrimary(), getIsSnmpPrimary())) {
            setIsSnmpPrimary(scannedIface.getIsSnmpPrimary());
        }
    
        if (hasNewValue(scannedIface.getIpHostName(), getIpHostName())) {
            setIpHostName(scannedIface.getIpHostName());
        }
        
        if (hasNewValue(scannedIface.getIpLastCapsdPoll(), getIpLastCapsdPoll())) {
            setIpLastCapsdPoll(scannedIface.getIpLastCapsdPoll());
        }
        
    }
    
    /**
     * <p>hasNewCollectionTypeValue</p>
     *
     * @param newVal a {@link org.opennms.netmgt.model.PrimaryType} object.
     * @param existingVal a {@link org.opennms.netmgt.model.PrimaryType} object.
     * @return a boolean.
     */
    protected static boolean hasNewCollectionTypeValue(PrimaryType newVal, PrimaryType existingVal) {
        return newVal != null && !newVal.equals(existingVal) && newVal != PrimaryType.NOT_ELIGIBLE;
    }


    /**
     * <p>mergeMonitoredServices</p>
     *
     * @param scannedIface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     * @param deleteMissing a boolean.
     */
    public void mergeMonitoredServices(OnmsIpInterface scannedIface, EventForwarder eventForwarder, boolean deleteMissing) {
    
        // create map of services to serviceType
        Map<OnmsServiceType, OnmsMonitoredService> serviceTypeMap = new HashMap<OnmsServiceType, OnmsMonitoredService>();
        for (OnmsMonitoredService svc : scannedIface.getMonitoredServices()) {
            serviceTypeMap.put(svc.getServiceType(), svc);
        }
    
        // for each service in the database
        for (Iterator<OnmsMonitoredService> it = getMonitoredServices().iterator(); it.hasNext();) {
            OnmsMonitoredService svc = it.next();
            
            // find the corresponding scanned service
            OnmsMonitoredService imported = serviceTypeMap.get(svc.getServiceType());
            if (imported == null) {
                if (deleteMissing) {
                    // there is no scanned service... delete it from the database 
                    it.remove();
                    svc.visit(new DeleteEventVisitor(eventForwarder));
                }
            }
            else {
                // otherwice update the service attributes
                svc.mergeServiceAttributes(imported);
            }
            
            // mark the service is updated
            serviceTypeMap.remove(svc.getServiceType());
        }
        
        // for any services not found in the database, add them
        Collection<OnmsMonitoredService> newServices = serviceTypeMap.values();
        for (OnmsMonitoredService svc : newServices) {
            svc.setIpInterface(this);
            getMonitoredServices().add(svc);
            svc.visit(new AddEventVisitor(eventForwarder));
        }
    }

    /**
     * <p>updateSnmpInterface</p>
     *
     * @param scannedIface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    public void updateSnmpInterface(OnmsIpInterface scannedIface) {
        
        if (!hasNewValue(scannedIface.getIfIndex(), getIfIndex())) {
            /* no ifIndex in currently scanned interface so don't bother
             * we must have failed to collect data
             */ 
            return;
        }
        
        if (scannedIface.getSnmpInterface() == null) {
            // there is no longer an snmpInterface associated with the ipInterface
            setSnmpInterface(null);
        } else {
            // locate the snmpInterface on this node that has the new ifIndex and set it
            // into the interface
            OnmsSnmpInterface snmpIface = getNode().getSnmpInterfaceWithIfIndex(scannedIface.getIfIndex());
            setSnmpInterface(snmpIface);
        }
        
        
        
    }

    /**
     * <p>mergeInterface</p>
     *
     * @param scannedIface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     * @param deleteMissing a boolean.
     */
    public void mergeInterface(OnmsIpInterface scannedIface, EventForwarder eventForwarder, boolean deleteMissing) {
        mergeInterfaceAttributes(scannedIface);
        updateSnmpInterface(scannedIface);
        mergeMonitoredServices(scannedIface, eventForwarder, deleteMissing);
    }

}
