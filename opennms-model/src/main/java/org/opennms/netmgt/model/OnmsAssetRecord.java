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

import org.springframework.core.style.ToStringCreator;


/**
 * Represents the asset information for a node.
 *
 * @hibernate.class table="assets"
 * @author ranger
 * @version $Id: $
 */
@Entity
@Table(name="assets")
public class OnmsAssetRecord implements Serializable {

    private static final long serialVersionUID = 509128305684814487L;
    
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

    /**
     * default constructor
     */
    public OnmsAssetRecord() {
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }
    
    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    protected void setId(Integer id) {
        m_id = id;
    }

    /**
     * The node this asset information belongs to.
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    public OnmsNode getNode() {
        return m_node;
    }

    /**
     * Set the node associated with the asset record
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(OnmsNode node) {
        m_node = node;
    }



    /**
     *--# category         : A broad idea of what this asset does (examples are
     *--#                    desktop, printer, server, infrastructure, etc.).
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="category", length=64)
    public String getCategory() {
        return m_category;
    }

    /**
     * <p>setCategory</p>
     *
     * @param category a {@link java.lang.String} object.
     */
    public void setCategory(String category) {
        m_category = category;
    }

    /**
     *--# manufacturer     : Name of the manufacturer of this asset.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="manufacturer", length=64)
    public String getManufacturer() {
        return m_manufacturer;
    }

    /**
     * <p>setManufacturer</p>
     *
     * @param manufacturer a {@link java.lang.String} object.
     */
    public void setManufacturer(String manufacturer) {
        m_manufacturer = manufacturer;
    }

    /**
     *--# vendor           : Vendor from whom this asset was purchased.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="vendor", length=64)
    public String getVendor() {
        return m_vendor;
    }

    /**
     * <p>setVendor</p>
     *
     * @param vendor a {@link java.lang.String} object.
     */
    public void setVendor(String vendor) {
        m_vendor = vendor;
    }

    /**
     *--# modelNumber      : The model number of this asset.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="modelNumber", length=64)
    public String getModelNumber() {
        return m_modelNumber;
    }

    /**
     * <p>setModelNumber</p>
     *
     * @param modelnumber a {@link java.lang.String} object.
     */
    public void setModelNumber(String modelnumber) {
        m_modelNumber = modelnumber;
    }

    /**
     *--# serialNumber     : The serial number of this asset.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="serialNumber", length=64)
    public String getSerialNumber() {
        return m_serialNumber;
    }

    /**
     * <p>setSerialNumber</p>
     *
     * @param serialnumber a {@link java.lang.String} object.
     */
    public void setSerialNumber(String serialnumber) {
        m_serialNumber = serialnumber;
    }

    /**
     *--# description      : A free-form description.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="description", length=128)
    public String getDescription() {
        return m_description;
    }

    /**
     * <p>setDescription</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        m_description = description;
    }

    /**
     *--# circuitId        : The electrical/network circuit this asset connects to.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="circuitId", length=64)
    public String getCircuitId() {
        return m_circuitId;
    }

    /**
     * <p>setCircuitId</p>
     *
     * @param circuitid a {@link java.lang.String} object.
     */
    public void setCircuitId(String circuitid) {
        m_circuitId = circuitid;
    }

    /**
     *--# assetNumber      : A business-specified asset number.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="assetNumber", length=64)
    public String getAssetNumber() {
        return m_assetNumber;
    }

    /**
     * <p>setAssetNumber</p>
     *
     * @param assetnumber a {@link java.lang.String} object.
     */
    public void setAssetNumber(String assetnumber) {
        m_assetNumber = assetnumber;
    }

    /**
     *--# operatingSystem  : The operating system, if any.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="operatingSystem", length=64)
    public String getOperatingSystem() {
        return m_operatingSystem;
    }

    /**
     * <p>setOperatingSystem</p>
     *
     * @param operatingsystem a {@link java.lang.String} object.
     */
    public void setOperatingSystem(String operatingsystem) {
        m_operatingSystem = operatingsystem;
    }

    /**
     *--# rack             : For servers, the rack it is installed in.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="rack", length=64)
    public String getRack() {
        return m_rack;
    }

    /**
     * <p>setRack</p>
     *
     * @param rack a {@link java.lang.String} object.
     */
    public void setRack(String rack) {
        m_rack = rack;
    }

    /**
     *--# slot             : For servers, the slot in the rack it is installed in.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="slot", length=64)
    public String getSlot() {
        return m_slot;
    }

    /**
     * <p>setSlot</p>
     *
     * @param slot a {@link java.lang.String} object.
     */
    public void setSlot(String slot) {
        m_slot = slot;
    }

    /**
     *--# port             : For servers, the port in the slot it is installed in.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="port", length=64)
    public String getPort() {
        return m_port;
    }

    /**
     * <p>setPort</p>
     *
     * @param port a {@link java.lang.String} object.
     */
    public void setPort(String port) {
        m_port = port;
    }

    /**
     *--# region           : A broad geographical or organizational area.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="region", length=64)
    public String getRegion() {
        return m_region;
    }

    /**
     * <p>setRegion</p>
     *
     * @param region a {@link java.lang.String} object.
     */
    public void setRegion(String region) {
        m_region = region;
    }

    /**
     *--# division         : A broad geographical or organizational area.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="division", length=64)
    public String getDivision() {
        return m_division;
    }

    /**
     * <p>setDivision</p>
     *
     * @param division a {@link java.lang.String} object.
     */
    public void setDivision(String division) {
        m_division = division;
    }

    /**
     *--# department       : The department this asset belongs to.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="department", length=64)
    public String getDepartment() {
        return m_department;
    }

    /**
     * <p>setDepartment</p>
     *
     * @param department a {@link java.lang.String} object.
     */
    public void setDepartment(String department) {
        m_department = department;
    }

    /**
     *--# address1         : Address of geographical location of asset, line 1.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="address1", length=256)
    public String getAddress1() {
        return m_address1;
    }

    /**
     * <p>setAddress1</p>
     *
     * @param address1 a {@link java.lang.String} object.
     */
    public void setAddress1(String address1) {
        m_address1 = address1;
    }

    /**
     *--# address2         : Address of geographical location of asset, line 2.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="address2", length=256)
    public String getAddress2() {
        return m_address2;
    }

    /**
     * <p>setAddress2</p>
     *
     * @param address2 a {@link java.lang.String} object.
     */
    public void setAddress2(String address2) {
        m_address2 = address2;
    }

    /**
     *--# city             : The city where this asset resides.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="city", length=64)
    public String getCity() {
        return m_city;
    }

    /**
     * <p>setCity</p>
     *
     * @param city a {@link java.lang.String} object.
     */
    public void setCity(String city) {
        m_city = city;
    }

    /**
     *--# state            : The state where this asset resides.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="state", length=64)
    public String getState() {
        return m_state;
    }

    /**
     * <p>setState</p>
     *
     * @param state a {@link java.lang.String} object.
     */
    public void setState(String state) {
        m_state = state;
    }

    /**
     *--# zip              : The zip code where this asset resides.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="zip", length=64)
    public String getZip() {
        return m_zip;
    }

    /**
     * <p>setZip</p>
     *
     * @param zip a {@link java.lang.String} object.
     */
    public void setZip(String zip) {
        m_zip = zip;
    }

    /**
     *--# building         : The building where this asset resides.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="building", length=64)
    public String getBuilding() {
        return m_building;
    }

    /**
     * <p>setBuilding</p>
     *
     * @param building a {@link java.lang.String} object.
     */
    public void setBuilding(String building) {
        m_building = building;
    }

    /**
     *--# floor            : The floor of the building where this asset resides.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="floor", length=64)
    public String getFloor() {
        return m_floor;
    }

    /**
     * <p>setFloor</p>
     *
     * @param floor a {@link java.lang.String} object.
     */
    public void setFloor(String floor) {
        m_floor = floor;
    }

    /**
     *--# room             : The room where this asset resides.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="room", length=64)
    public String getRoom() {
        return m_room;
    }

    /**
     * <p>setRoom</p>
     *
     * @param room a {@link java.lang.String} object.
     */
    public void setRoom(String room) {
        m_room = room;
    }

    /**
     *--# vendorPhone      : A contact number for the vendor.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="vendorPhone", length=64)
    public String getVendorPhone() {
        return m_vendorPhone;
    }

    /**
     * <p>setVendorPhone</p>
     *
     * @param vendorphone a {@link java.lang.String} object.
     */
    public void setVendorPhone(String vendorphone) {
        m_vendorPhone = vendorphone;
    }

    /**
     *--# vendorFax        : A fax number for the vendor.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="vendorFax", length=64)
    public String getVendorFax() {
        return m_vendorFax;
    }

    /**
     * <p>setVendorFax</p>
     *
     * @param vendorfax a {@link java.lang.String} object.
     */
    public void setVendorFax(String vendorfax) {
        m_vendorFax = vendorfax;
    }

    /**
     * <p>getVendorAssetNumber</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="vendorAssetNumber", length=64)
    public String getVendorAssetNumber() {
        return m_vendorAssetNumber;
    }

    /**
     * <p>setVendorAssetNumber</p>
     *
     * @param vendorassetnumber a {@link java.lang.String} object.
     */
    public void setVendorAssetNumber(String vendorassetnumber) {
        m_vendorAssetNumber = vendorassetnumber;
    }

    /**
     *--# userLastModified : The last user who modified this record.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="userLastModified", length=20)
    public String getLastModifiedBy() {
        return m_lastModifiedBy;
    }

    /**
     * <p>setLastModifiedBy</p>
     *
     * @param userlastmodified a {@link java.lang.String} object.
     */
    public void setLastModifiedBy(String userlastmodified) {
        m_lastModifiedBy = userlastmodified;
    }

    /**
     *--# lastModifiedDate : The last time this record was modified.
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastModifiedDate")
    public Date getLastModifiedDate() {
        return m_lastModifiedDate;
    }

    /**
     * <p>setLastModifiedDate</p>
     *
     * @param lastmodifieddate a {@link java.util.Date} object.
     */
    public void setLastModifiedDate(Date lastmodifieddate) {
        m_lastModifiedDate = lastmodifieddate;
    }

    /**
     *--# dateInstalled    : The date the asset was installed.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="dateInstalled", length=64)
    public String getDateInstalled() {
        return m_dateInstalled;
    }

    /**
     * <p>setDateInstalled</p>
     *
     * @param dateinstalled a {@link java.lang.String} object.
     */
    public void setDateInstalled(String dateinstalled) {
        m_dateInstalled = dateinstalled;
    }

    /**
     *--# lease            : The lease number of this asset.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="lease", length=64)
    public String getLease() {
        return m_lease;
    }

    /**
     * <p>setLease</p>
     *
     * @param lease a {@link java.lang.String} object.
     */
    public void setLease(String lease) {
        m_lease = lease;
    }

    /**
     *--# leaseExpires     : The date the lease expires for this asset.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="leaseExpires", length=64)
    public String getLeaseExpires() {
        return m_leaseExpires;
    }

    /**
     * <p>setLeaseExpires</p>
     *
     * @param leaseexpires a {@link java.lang.String} object.
     */
    public void setLeaseExpires(String leaseexpires) {
        m_leaseExpires = leaseexpires;
    }

    /**
     *--# supportPhone     : A support phone number for this asset.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="supportPhone", length=64)
    public String getSupportPhone() {
        return m_supportPhone;
    }

    /**
     * <p>setSupportPhone</p>
     *
     * @param supportphone a {@link java.lang.String} object.
     */
    public void setSupportPhone(String supportphone) {
        m_supportPhone = supportphone;
    }

    /**
     *--# maintContract    : The maintenance contract number for this asset.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="maintContract", length=64)
    public String getMaintContractNumber() {
        return m_maintContractNumber;
    }

    /**
     * <p>setMaintContractNumber</p>
     *
     * @param maintcontract a {@link java.lang.String} object.
     */
    public void setMaintContractNumber(String maintcontract) {
        m_maintContractNumber = maintcontract;
    }

    /**
     * <p>getMaintContractExpiration</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="maintContractExpires", length=64)
    public String getMaintContractExpiration() {
        return m_maintContractExpiration;
    }

    /**
     * <p>setMaintContractExpiration</p>
     *
     * @param maintcontractexpires a {@link java.lang.String} object.
     */
    public void setMaintContractExpiration(String maintcontractexpires) {
        m_maintContractExpiration = maintcontractexpires;
    }

    /**
     * <p>getDisplayCategory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="displayCategory", length=64)
    public String getDisplayCategory() {
        return m_displayCategory;
    }

    /**
     * <p>setDisplayCategory</p>
     *
     * @param displaycategory a {@link java.lang.String} object.
     */
    public void setDisplayCategory(String displaycategory) {
        m_displayCategory = displaycategory;
    }

    /**
     * <p>getNotifyCategory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="notifyCategory", length=64)
    public String getNotifyCategory() {
        return m_notifyCategory;
    }

    /**
     * <p>setNotifyCategory</p>
     *
     * @param notifycategory a {@link java.lang.String} object.
     */
    public void setNotifyCategory(String notifycategory) {
        m_notifyCategory = notifycategory;
    }

    /**
     * <p>getPollerCategory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="pollerCategory", length=64)
    public String getPollerCategory() {
        return m_pollerCategory;
    }

    /**
     * <p>setPollerCategory</p>
     *
     * @param pollercategory a {@link java.lang.String} object.
     */
    public void setPollerCategory(String pollercategory) {
        m_pollerCategory = pollercategory;
    }

    /**
     * <p>getThresholdCategory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="thresholdCategory", length=64)
    public String getThresholdCategory() {
        return m_thresholdCategory;
    }

    /**
     * <p>setThresholdCategory</p>
     *
     * @param thresholdcategory a {@link java.lang.String} object.
     */
    public void setThresholdCategory(String thresholdcategory) {
        m_thresholdCategory = thresholdcategory;
    }

    /**
     * <p>getComment</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="comment", length=1024)
    public String getComment() {
        return m_comment;
    }

    /**
     * <p>setComment</p>
     *
     * @param comment a {@link java.lang.String} object.
     */
    public void setComment(String comment) {
        m_comment = comment;
    }
    
    /**
     * <p>getManagedObjectType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="managedObjectType", length=512)
    public String getManagedObjectType() {
        return m_managedObjectType;
    }
    
    /**
     * <p>setManagedObjectType</p>
     *
     * @param mot a {@link java.lang.String} object.
     */
    public void setManagedObjectType(String mot) {
        m_managedObjectType = mot;
    }
    
    /**
     * <p>getManagedObjectInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="managedObjectInstance", length=512)
    public String getManagedObjectInstance() {
        return m_managedObjectInstance;
    }
    
    /**
     * <p>setManagedObjectInstance</p>
     *
     * @param moi a {@link java.lang.String} object.
     */
    public void setManagedObjectInstance(String moi) {
        m_managedObjectInstance = moi;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
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

}
