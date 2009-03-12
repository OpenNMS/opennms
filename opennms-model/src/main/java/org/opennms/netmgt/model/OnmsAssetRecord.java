//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.style.ToStringCreator;

/** 
 * Represents the asset information for a node.
 * 
 * @hibernate.class table="assets"
 *     
 */
@XmlRootElement(name = "assetRecord")
@Entity
@Table(name="assets")
public class OnmsAssetRecord implements Serializable {

    private static final long serialVersionUID = 509128305684814487L;
    
    public static final String AUTOENABLED = "A";

    public static final String SSH_CONNECTION = "SSH";

    public static final String TELNET_CONNECTION = "Telnet";

    public static final String RSH_CONNECTION = "RSH";

    private Integer m_id;
    
    /** identifier field */
    private String m_category = "Unspecified";

    /** identifier field */
    private String m_manufacturer;

    /** identifier field */
    private String m_vendor;

    /** identifier field */
    private String m_modelNumber;

    /** identifier field */
    private String m_serialNumber;

    /** identifier field */
    private String m_description;

    /** identifier field */
    private String m_circuitId;

    /** identifier field */
    private String m_assetNumber;

    /** identifier field */
    private String m_operatingSystem;

    /** identifier field */
    private String m_rack;

    /** identifier field */
    private String m_slot;

    /** identifier field */
    private String m_port;

    /** identifier field */
    private String m_region;

    /** identifier field */
    private String m_division;

    /** identifier field */
    private String m_department;

    /** identifier field */
    private String m_address1;

    /** identifier field */
    private String m_address2;

    /** identifier field */
    private String m_city;

    /** identifier field */
    private String m_state;

    /** identifier field */
    private String m_zip;

    /** identifier field */
    private String m_building;

    /** identifier field */
    private String m_floor;

    /** identifier field */
    private String m_room;

    /** identifier field */
    private String m_vendorPhone;

    /** identifier field */
    private String m_vendorFax;

    /** identifier field */
    private String m_vendorAssetNumber;

    /** identifier field */
    private String m_username;

    /** identifier field */
    private String m_password;

    /** identifier field */
    private String m_enable;

    /** identifier field */
    private String m_connection;

    /** identifier field */
    private String m_autoenable;

    /** identifier field */
    private String m_lastModifiedBy = "";

    /** identifier field */
    private Date m_lastModifiedDate = new Date();

    /** identifier field */
    private String m_dateInstalled;

    /** identifier field */
    private String m_lease;

    /** identifier field */
    private String m_leaseExpires;

    /** identifier field */
    private String m_supportPhone;

    /** identifier field */
    private String m_maintContractNumber;

    /** identifier field */
    private String m_maintContractExpiration;

    /** identifier field */
    private String m_displayCategory;

    /** identifier field */
    private String m_notifyCategory;

    /** identifier field */
    private String m_pollerCategory;

    /** identifier field */
    private String m_thresholdCategory;

    /** identifier field */
    private String m_comment;

    /** persistent field */
    private OnmsNode m_node;

    private String m_managedObjectType;

    private String m_managedObjectInstance;

    /** default constructor */
    public OnmsAssetRecord() {
    }

    @Id
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }
    
    protected void setId(Integer id) {
        m_id = id;
    }

    /** 
     * The node this asset information belongs to.
     */
    @XmlIDREF
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    public OnmsNode getNode() {
        return m_node;
    }

    /**
     * Set the node associated with the asset record
     * 
     */
    public void setNode(OnmsNode node) {
        m_node = node;
    }



    /** 
--# category         : A broad idea of what this asset does (examples are
--#                    desktop, printer, server, infrastructure, etc.).
     *             
     */
    @Column(name="category", length=64)
    public String getCategory() {
        return m_category;
    }

    public void setCategory(String category) {
        m_category = category;
    }

    /** 
--# manufacturer     : Name of the manufacturer of this asset.
     */
    @Column(name="manufacturer", length=64)
    public String getManufacturer() {
        return m_manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        m_manufacturer = manufacturer;
    }

    /** 
--# vendor           : Vendor from whom this asset was purchased.
     */
    @Column(name="vendor", length=64)
    public String getVendor() {
        return m_vendor;
    }

    public void setVendor(String vendor) {
        m_vendor = vendor;
    }

    /** 
--# modelNumber      : The model number of this asset.
     */
    @Column(name="modelNumber", length=64)
    public String getModelNumber() {
        return m_modelNumber;
    }

    public void setModelNumber(String modelnumber) {
        m_modelNumber = modelnumber;
    }

    /** 
--# serialNumber     : The serial number of this asset.
     *             
     */
    @Column(name="serialNumber", length=64)
    public String getSerialNumber() {
        return m_serialNumber;
    }

    public void setSerialNumber(String serialnumber) {
        m_serialNumber = serialnumber;
    }

    /** 
--# description      : A free-form description.
     */
    @Column(name="description", length=128)
    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    /** 
--# circuitId        : The electrical/network circuit this asset connects to.
     */
    @Column(name="circuitId", length=64)
    public String getCircuitId() {
        return m_circuitId;
    }

    public void setCircuitId(String circuitid) {
        m_circuitId = circuitid;
    }

    /** 
--# assetNumber      : A business-specified asset number.
     *             
     */
    @Column(name="assetNumber", length=64)
    public String getAssetNumber() {
        return m_assetNumber;
    }

    public void setAssetNumber(String assetnumber) {
        m_assetNumber = assetnumber;
    }

    /** 
--# operatingSystem  : The operating system, if any.
     *             
     */
    @Column(name="operatingSystem", length=64)
    public String getOperatingSystem() {
        return m_operatingSystem;
    }

    public void setOperatingSystem(String operatingsystem) {
        m_operatingSystem = operatingsystem;
    }

    /** 
--# rack             : For servers, the rack it is installed in.
     *             
     */
    @Column(name="rack", length=64)
    public String getRack() {
        return m_rack;
    }

    public void setRack(String rack) {
        m_rack = rack;
    }

    /** 
--# slot             : For servers, the slot in the rack it is installed in.
     *             
     */
    @Column(name="slot", length=64)
    public String getSlot() {
        return m_slot;
    }

    public void setSlot(String slot) {
        m_slot = slot;
    }

    /** 
--# port             : For servers, the port in the slot it is installed in.
     *             
     */
    @Column(name="port", length=64)
    public String getPort() {
        return m_port;
    }

    public void setPort(String port) {
        m_port = port;
    }

    /** 
--# region           : A broad geographical or organizational area.
     *             
     */
    @Column(name="region", length=64)
    public String getRegion() {
        return m_region;
    }

    public void setRegion(String region) {
        m_region = region;
    }

    /** 
--# division         : A broad geographical or organizational area.
     *             
     */
    @Column(name="division", length=64)
    public String getDivision() {
        return m_division;
    }

    public void setDivision(String division) {
        m_division = division;
    }

    /** 
--# department       : The department this asset belongs to.
     *             
     */
    @Column(name="department", length=64)
    public String getDepartment() {
        return m_department;
    }

    public void setDepartment(String department) {
        m_department = department;
    }

    /** 
--# address1         : Address of geographical location of asset, line 1.
     *             
     */
    @Column(name="address1", length=256)
    public String getAddress1() {
        return m_address1;
    }

    public void setAddress1(String address1) {
        m_address1 = address1;
    }

    /** 
--# address2         : Address of geographical location of asset, line 2.
     *             
     */
    @Column(name="address2", length=256)
    public String getAddress2() {
        return m_address2;
    }

    public void setAddress2(String address2) {
        m_address2 = address2;
    }

    /** 
--# city             : The city where this asset resides.
     *             
     */
    @Column(name="city", length=64)
    public String getCity() {
        return m_city;
    }

    public void setCity(String city) {
        m_city = city;
    }

    /** 
--# state            : The state where this asset resides.
     *             
     */
    @Column(name="state", length=64)
    public String getState() {
        return m_state;
    }

    public void setState(String state) {
        m_state = state;
    }

    /** 
--# zip              : The zip code where this asset resides.
     *             
     */
    @Column(name="zip", length=64)
    public String getZip() {
        return m_zip;
    }

    public void setZip(String zip) {
        m_zip = zip;
    }

    /** 
--# building         : The building where this asset resides.
     *             
     */
    @Column(name="building", length=64)
    public String getBuilding() {
        return m_building;
    }

    public void setBuilding(String building) {
        m_building = building;
    }

    /** 
--# floor            : The floor of the building where this asset resides.
     *             
     */
    @Column(name="floor", length=64)
    public String getFloor() {
        return m_floor;
    }

    public void setFloor(String floor) {
        m_floor = floor;
    }

    /** 
--# room             : The room where this asset resides.
     *             
     */
    @Column(name="room", length=64)
    public String getRoom() {
        return m_room;
    }

    public void setRoom(String room) {
        m_room = room;
    }

    /** 
--# vendorPhone      : A contact number for the vendor.
     *             
     */
    @Column(name="vendorPhone", length=64)
    public String getVendorPhone() {
        return m_vendorPhone;
    }

    public void setVendorPhone(String vendorphone) {
        m_vendorPhone = vendorphone;
    }

    /** 
--# vendorFax        : A fax number for the vendor.
     *             
     */
    @Column(name="vendorFax", length=64)
    public String getVendorFax() {
        return m_vendorFax;
    }

    public void setVendorFax(String vendorfax) {
        m_vendorFax = vendorfax;
    }

    @Column(name="vendorAssetNumber", length=64)
    public String getVendorAssetNumber() {
        return m_vendorAssetNumber;
    }

    public void setVendorAssetNumber(String vendorassetnumber) {
        m_vendorAssetNumber = vendorassetnumber;
    }

    /** 
--# userLastModified : The last user who modified this record.
     *             
     */
    @Column(name="userLastModified", length=20)
    public String getLastModifiedBy() {
        return m_lastModifiedBy;
    }

    public void setLastModifiedBy(String userlastmodified) {
        m_lastModifiedBy = userlastmodified;
    }

    /** 
--# lastModifiedDate : The last time this record was modified.
     *             
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastModifiedDate")
    public Date getLastModifiedDate() {
        return m_lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastmodifieddate) {
        m_lastModifiedDate = lastmodifieddate;
    }

    /** 
--# dateInstalled    : The date the asset was installed.
     *             
     */
    @Column(name="dateInstalled", length=64)
    public String getDateInstalled() {
        return m_dateInstalled;
    }

    public void setDateInstalled(String dateinstalled) {
        m_dateInstalled = dateinstalled;
    }

    /** 
--# lease            : The lease number of this asset.
     *             
     */
    @Column(name="lease", length=64)
    public String getLease() {
        return m_lease;
    }

    public void setLease(String lease) {
        m_lease = lease;
    }

    /** 
--# leaseExpires     : The date the lease expires for this asset.
     *             
     */
    @Column(name="leaseExpires", length=64)
    public String getLeaseExpires() {
        return m_leaseExpires;
    }

    public void setLeaseExpires(String leaseexpires) {
        m_leaseExpires = leaseexpires;
    }

    /** 
--# supportPhone     : A support phone number for this asset.
     *             
     */
    @Column(name="supportPhone", length=64)
    public String getSupportPhone() {
        return m_supportPhone;
    }

    public void setSupportPhone(String supportphone) {
        m_supportPhone = supportphone;
    }

    /** 
--# maintContract    : The maintenance contract number for this asset.
     *             
     */
    @Column(name="maintContract", length=64)
    public String getMaintContractNumber() {
        return m_maintContractNumber;
    }

    public void setMaintContractNumber(String maintcontract) {
        m_maintContractNumber = maintcontract;
    }

    @Column(name="maintContractExpires", length=64)
    public String getMaintContractExpiration() {
        return m_maintContractExpiration;
    }

    public void setMaintContractExpiration(String maintcontractexpires) {
        m_maintContractExpiration = maintcontractexpires;
    }

    @Column(name="displayCategory", length=64)
    public String getDisplayCategory() {
        return m_displayCategory;
    }

    public void setDisplayCategory(String displaycategory) {
        m_displayCategory = displaycategory;
    }

    @Column(name="notifyCategory", length=64)
    public String getNotifyCategory() {
        return m_notifyCategory;
    }

    public void setNotifyCategory(String notifycategory) {
        m_notifyCategory = notifycategory;
    }

    @Column(name="pollerCategory", length=64)
    public String getPollerCategory() {
        return m_pollerCategory;
    }

    public void setPollerCategory(String pollercategory) {
        m_pollerCategory = pollercategory;
    }

    @Column(name="thresholdCategory", length=64)
    public String getThresholdCategory() {
        return m_thresholdCategory;
    }

    public void setThresholdCategory(String thresholdcategory) {
        m_thresholdCategory = thresholdcategory;
    }

    @Column(name="comment", length=1024)
    public String getComment() {
        return m_comment;
    }

    public void setComment(String comment) {
        m_comment = comment;
    }
    
    @Column(name="managedObjectType", length=512)
    public String getManagedObjectType() {
        return m_managedObjectType;
    }
    
    public void setManagedObjectType(String mot) {
        m_managedObjectType = mot;
    }
    
    @Column(name="managedObjectInstance", length=512)
    public String getManagedObjectInstance() {
        return m_managedObjectInstance;
    }
    
    public void setManagedObjectInstance(String moi) {
        m_managedObjectInstance = moi;
    }
    

    @Column(name="username", length=32)
    public String getUsername() {
        return m_username;
    }

    public void setUsername(String username) {
        m_username = username;
    }

    @Column(name="password", length=32)
    public String getPassword() {
        return m_password;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    @Column(name="enable", length=32)
    public String getEnable() {
        return m_enable;
    }

    public void setEnable(String enable) {
        m_enable = enable;
    }

    @Column(name="connection", length=32)
    public String getConnection() {
        return m_connection;
    }

    public void setConnection(String connection) {
        m_connection = connection;
    }

    @Column(name="autoenable", length=1)
    public String getAutoenable() {
        return m_autoenable;
    }

    public void setAutoenable(String autoenable) {
        m_autoenable = autoenable;
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
            .append("category", getCategory())
            .append("manufacturer", getManufacturer())
            .append("vendor", getVendor())
            .append("modelnumber", getModelNumber())
            .append("serialnumber", getSerialNumber())
            .append("description", getDescription())
            .append("circuitid", getCircuitId())
            .append("assetnumber", getAssetNumber())
            .append("operatingsystem", getOperatingSystem())
            .append("rack", getRack())
            .append("slot", getSlot())
            .append("port", getPort())
            .append("region", getRegion())
            .append("division", getDivision())
            .append("department", getDepartment())
            .append("address1", getAddress1())
            .append("address2", getAddress2())
            .append("city", getCity())
            .append("state", getState())
            .append("zip", getZip())
            .append("building", getBuilding())
            .append("floor", getFloor())
            .append("room", getRoom())
            .append("vendorphone", getVendorPhone())
            .append("vendorfax", getVendorFax())
            .append("vendorassetnumber", getVendorAssetNumber())
            .append("userlastmodified", getLastModifiedBy())
            .append("lastmodifieddate", getLastModifiedDate())
            .append("dateinstalled", getDateInstalled())
            .append("lease", getLease())
            .append("leaseexpires", getLeaseExpires())
            .append("supportphone", getSupportPhone())
            .append("maintcontract", getMaintContractNumber())
            .append("maintcontractexpires", getMaintContractExpiration())
            .append("displaycategory", getDisplayCategory())
            .append("notifycategory", getNotifyCategory())
            .append("pollercategory", getPollerCategory())
            .append("thresholdcategory", getThresholdCategory())
            .append("comment", getComment())
            .toString();
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = false;
        
        if (this == obj) {
            return true;
        }
        
        if (obj == null || obj.getClass() != this.getClass()) {
            throw new IllegalArgumentException("the Operation Object passed is either null or of the wrong class");
        }

        OnmsAssetRecord cmpAsset = (OnmsAssetRecord)obj;
        
        Integer currentNodeId = m_node.getId();
        Integer newNodeId = cmpAsset.getNode().getId();
        
        if (newNodeId == null) {
            return false;
        }
        
        if (m_node.getId().equals(cmpAsset.getNode().getId())) {
            equals = true;
        }
        
        return equals;
        
    }
    
    /**
     * Used to merge the contents of one asset record to another.  If equals implementation
     * returns false, the merge is aborted.
     * @param newRecord
     */
    public void mergeRecord(OnmsAssetRecord newRecord) {
        
        if (!this.equals(newRecord)) {
            return;
        }
        
        //this works because all asset properties are strings
        //if the model dependencies ever change to not include spring, this will break
        BeanWrapper currentBean = new BeanWrapperImpl(this);
        BeanWrapper newBean = new BeanWrapperImpl(newRecord);
        PropertyDescriptor[] pds = newBean.getPropertyDescriptors();
        
        for (PropertyDescriptor pd : pds) {
            String propertyName = pd.getName();
            
            if (propertyName.equals("class")) {
                continue;
            }
            
            if (newBean.getPropertyValue(propertyName) != null) {
                currentBean.setPropertyValue(propertyName, newBean.getPropertyValue(propertyName));
            }
        }
    }
}
