/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.gwt.web.ui.asset.shared;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a> </br>
 *         Command object for asset pages. Contains fields from OnmsAsset and
 *         additional values needed to show complete ui.
 */
public class AssetCommand implements IsSerializable {

    private String m_additionalhardware;

    private String m_address1;

    private String m_address2;

    private String m_admin;

    private boolean m_allowModify;

    private String m_assetNumber;

    private String m_autoenable;

    private ArrayList<String> m_autoenableOptions;

    private String m_building;

    private String m_category;

    private String m_circuitId;

    private String m_city;

    private String m_comment;

    private String m_connection;

    private ArrayList<String> m_connectionOptions;

    private Float m_longitude;
    
    private Float m_latitude;

    private String m_country;

    private String m_cpu;

    private String m_dateInstalled;

    private String m_department;

    private String m_description;

    private String m_displayCategory;

    private String m_division;

    private String m_enable;

    private String m_floor;

    private String m_hdd1;

    private String m_hdd2;

    private String m_hdd3;

    private String m_hdd4;

    private String m_hdd5;

    private String m_hdd6;

    private Integer m_id;

    private String m_inputpower;

    private String m_lastModifiedBy;

    private Date m_lastModifiedDate;

    private String m_lease;

    private String m_leaseExpires;

    private String m_loggedInUser;

    private String m_maintcontract;

    private String m_maintContractExpiration;

    private String m_manufacturer;

    private String m_modelNumber;

    private Integer m_nextNodeId;

    private String m_nodeId;

    private String m_nodeLabel;

    private String m_notifyCategory;

    private String m_numpowersupplies;

    private String m_operatingSystem;

    private String m_password;

    private String m_pollerCategory;

    private String m_port;

    private Integer m_previousNodeId;

    private String m_rack;

    private String m_rackunitheight;

    private String m_ram;

    private String m_region;

    private String m_room;

    private String m_serialNumber;

    private String m_slot;

    private String m_snmpcommunity;

    private String m_snmpSysContact;

    private String m_snmpSysDescription;

    private String m_snmpSysLocation;

    private String m_snmpSysName;

    private String m_snmpSysObjectId;

    private String m_state;

    private String m_storagectrl;

    private String m_supportPhone;

    private String m_thresholdCategory;

    private String m_username;

    private String m_vendor;

    private String m_vendorAssetNumber;

    private String m_vendorFax;

    private String m_vendorPhone;

    private String m_zip;

    private String m_vmwareManagedObjectId;

    private String m_vmwareManagedEntityType;

    private String m_vmwareManagementServer;

    private String m_vmwareTopologyInfo;

    private String m_vmwareState;

    public AssetCommand() {
        m_autoenableOptions = new ArrayList<String>();
        m_connectionOptions = new ArrayList<String>();
        m_lastModifiedDate = new Date();
    }

    // --- Getter ---

    public String getAdditionalhardware() {
        return m_additionalhardware;
    }

    public String getAddress1() {
        return m_address1;
    }

    public String getAddress2() {
        return m_address2;
    }

    public String getAdmin() {
        return m_admin;
    }

    public boolean getAllowModify() {
        return m_allowModify;
    }

    public String getAssetNumber() {
        return m_assetNumber;
    }

    public String getAutoenable() {
        return m_autoenable;
    }

    public ArrayList<String> getAutoenableOptions() {
        return m_autoenableOptions;
    }

    public String getBuilding() {
        return m_building;
    }

    public String getCategory() {
        return m_category;
    }

    public String getCircuitId() {
        return m_circuitId;
    }

    public String getCity() {
        return m_city;
    }

    public String getComment() {
        return m_comment;
    }

    public String getConnection() {
        return m_connection;
    }

    public ArrayList<String> getConnectionOptions() {
        return m_connectionOptions;
    }

    public Float getLongitude() {
        return m_longitude;
    }

    public Float getLatitude() {
        return m_latitude;
    }

    public String getCountry() {
        return m_country;
    }

    public String getCpu() {
        return m_cpu;
    }

    public String getDateInstalled() {
        return m_dateInstalled;
    }

    public String getDepartment() {
        return m_department;
    }

    public String getDescription() {
        return m_description;
    }

    public String getDisplayCategory() {
        return m_displayCategory;
    }

    public String getDivision() {
        return m_division;
    }

    public String getEnable() {
        return m_enable;
    }

    public String getFloor() {
        return m_floor;
    }

    public String getHdd1() {
        return m_hdd1;
    }

    public String getHdd2() {
        return m_hdd2;
    }

    public String getHdd3() {
        return m_hdd3;
    }

    public String getHdd4() {
        return m_hdd4;
    }

    public String getHdd5() {
        return m_hdd5;
    }

    public String getHdd6() {
        return m_hdd6;
    }

    public Integer getId() {
        return m_id;
    }

    public String getInputpower() {
        return m_inputpower;
    }

    public String getLastModifiedBy() {
        return m_lastModifiedBy;
    }

    public Date getLastModifiedDate() {
        return m_lastModifiedDate;
    }

    public String getLease() {
        return m_lease;
    }

    public String getLeaseExpires() {
        return m_leaseExpires;
    }

    public String getLoggedInUser() {
        return m_loggedInUser;
    }

    public String getMaintcontract() {
        return m_maintcontract;
    }

    public String getMaintContractExpiration() {
        return m_maintContractExpiration;
    }

    public String getManufacturer() {
        return m_manufacturer;
    }

    public String getModelNumber() {
        return m_modelNumber;
    }

    public Integer getNextNodeId() {
        return m_nextNodeId;
    }

    public String getNodeId() {
        return m_nodeId;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public String getNotifyCategory() {
        return m_notifyCategory;
    }

    public String getNumpowersupplies() {
        return m_numpowersupplies;
    }

    public String getOperatingSystem() {
        return m_operatingSystem;
    }

    public String getPassword() {
        return m_password;
    }

    public String getPollerCategory() {
        return m_pollerCategory;
    }

    public String getPort() {
        return m_port;
    }

    public Integer getPreviousNodeId() {
        return m_previousNodeId;
    }

    public String getRack() {
        return m_rack;
    }

    public String getRackunitheight() {
        return m_rackunitheight;
    }

    public String getRam() {
        return m_ram;
    }

    public String getRegion() {
        return m_region;
    }

    public String getRoom() {
        return m_room;
    }

    public String getSerialNumber() {
        return m_serialNumber;
    }

    public String getSlot() {
        return m_slot;
    }

    public String getSnmpcommunity() {
        return m_snmpcommunity;
    }

    public String getSnmpSysContact() {
        return m_snmpSysContact;
    }

    public String getSnmpSysDescription() {
        return m_snmpSysDescription;
    }

    public String getSnmpSysLocation() {
        return m_snmpSysLocation;
    }

    public String getSnmpSysName() {
        return m_snmpSysName;
    }

    public String getSnmpSysObjectId() {
        return m_snmpSysObjectId;
    }

    public String getState() {
        return m_state;
    }

    public String getStoragectrl() {
        return m_storagectrl;
    }

    public String getSupportPhone() {
        return m_supportPhone;
    }

    public String getThresholdCategory() {
        return m_thresholdCategory;
    }

    public String getUsername() {
        return m_username;
    }

    public String getVendor() {
        return m_vendor;
    }

    public String getVendorAssetNumber() {
        return m_vendorAssetNumber;
    }

    public String getVendorFax() {
        return m_vendorFax;
    }

    public String getVendorPhone() {
        return m_vendorPhone;
    }

    public String getZip() {
        return m_zip;
    }

    public String getVmwareManagedObjectId() {
        return m_vmwareManagedObjectId;
    }

    public String getVmwareManagedEntityType() {
        return m_vmwareManagedEntityType;
    }

    public String getVmwareManagementServer() {
        return m_vmwareManagementServer;
    }

    public String getVmwareTopologyInfo() {
        return m_vmwareTopologyInfo;
    }

    public String getVmwareState() {
        return m_vmwareState;
    }

    // --- Setter ---

    public void setAdditionalhardware(String additionalhardware) {
        m_additionalhardware = additionalhardware;
    }

    public void setAddress1(String address1) {
        m_address1 = address1;
    }

    public void setAddress2(String address2) {
        m_address2 = address2;
    }

    public void setAdmin(String admin) {
        m_admin = admin;
    }

    public void setAllowModify(boolean m_allowModify) {
        this.m_allowModify = m_allowModify;
    }

    public void setAssetNumber(String assetNumber) {
        m_assetNumber = assetNumber;
    }

    public void setAutoenable(String autoenable) {
        m_autoenable = autoenable;
    }

    public void setAutoenableOptions(ArrayList<String> autoenableOptions) {
        m_autoenableOptions = autoenableOptions;
    }

    public void setBuilding(String building) {
        m_building = building;
    }

    public void setCategory(String category) {
        m_category = category;
    }

    public void setCircuitId(String circuitId) {
        m_circuitId = circuitId;
    }

    public void setCity(String city) {
        m_city = city;
    }

    public void setComment(String comment) {
        m_comment = comment;
    }

    public void setConnection(String connection) {
        m_connection = connection;
    }

    public void setConnectionOptions(ArrayList<String> connectionOptions) {
        m_connectionOptions = connectionOptions;
    }

    public void setLongitude(Float longitude) {
        m_longitude = longitude;
    }

    public void setLatitude(Float latitude) {
        m_latitude = latitude;
    }

    public void setCountry(String country) {
        m_country = country;
    }

    public void setCpu(String cpu) {
        m_cpu = cpu;
    }

    public void setDateInstalled(String dateInstalled) {
        m_dateInstalled = dateInstalled;
    }

    public void setDepartment(String department) {
        m_department = department;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public void setDisplayCategory(String displayCategory) {
        m_displayCategory = displayCategory;
    }

    public void setDivision(String division) {
        m_division = division;
    }

    public void setEnable(String enable) {
        m_enable = enable;
    }

    public void setFloor(String floor) {
        m_floor = floor;
    }

    public void setHdd1(String hdd1) {
        m_hdd1 = hdd1;
    }

    public void setHdd2(String hdd2) {
        m_hdd2 = hdd2;
    }

    public void setHdd3(String hdd3) {
        m_hdd3 = hdd3;
    }

    public void setHdd4(String hdd4) {
        m_hdd4 = hdd4;
    }

    public void setHdd5(String hdd5) {
        m_hdd5 = hdd5;
    }

    public void setHdd6(String hdd6) {
        m_hdd6 = hdd6;
    }

    public void setId(Integer id) {
        m_id = id;
    }

    public void setInputpower(String inputpower) {
        m_inputpower = inputpower;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        m_lastModifiedBy = lastModifiedBy;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        m_lastModifiedDate = lastModifiedDate;
    }

    public void setLease(String lease) {
        m_lease = lease;
    }

    public void setLeaseExpires(String leaseExpires) {
        m_leaseExpires = leaseExpires;
    }

    public void setLoggedInUser(String m_loggedInUser) {
        this.m_loggedInUser = m_loggedInUser;
    }

    public void setMaintcontract(String maintcontract) {
        m_maintcontract = maintcontract;
    }

    public void setMaintContractExpiration(String maintContractExpiration) {
        m_maintContractExpiration = maintContractExpiration;
    }

    public void setManufacturer(String manufacturer) {
        m_manufacturer = manufacturer;
    }

    public void setModelNumber(String modelNumber) {
        m_modelNumber = modelNumber;
    }

    public void setNextNodeId(Integer m_nextNodeId) {
        this.m_nextNodeId = m_nextNodeId;
    }

    public void setNodeId(String m_nodeId) {
        this.m_nodeId = m_nodeId;
    }

    public void setNodeLabel(String m_nodeLabel) {
        this.m_nodeLabel = m_nodeLabel;
    }

    public void setNotifyCategory(String notifyCategory) {
        m_notifyCategory = notifyCategory;
    }

    public void setNumpowersupplies(String numpowersupplies) {
        m_numpowersupplies = numpowersupplies;
    }

    public void setOperatingSystem(String operatingSystem) {
        m_operatingSystem = operatingSystem;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    public void setPollerCategory(String pollerCategory) {
        m_pollerCategory = pollerCategory;
    }

    public void setPort(String port) {
        m_port = port;
    }

    public void setPreviousNodeId(Integer m_previousNodeId) {
        this.m_previousNodeId = m_previousNodeId;
    }

    public void setRack(String rack) {
        m_rack = rack;
    }

    public void setRackunitheight(String m_rackunitheight) {
        this.m_rackunitheight = m_rackunitheight;
    }

    public void setRam(String ram) {
        m_ram = ram;
    }

    public void setRegion(String region) {
        m_region = region;
    }

    public void setRoom(String room) {
        m_room = room;
    }

    public void setSerialNumber(String serialNumber) {
        m_serialNumber = serialNumber;
    }

    public void setSlot(String slot) {
        m_slot = slot;
    }

    public void setSnmpcommunity(String snmpcommunity) {
        m_snmpcommunity = snmpcommunity;
    }

    public void setSnmpSysContact(String snmpSysContact) {
        m_snmpSysContact = snmpSysContact;
    }

    public void setSnmpSysDescription(String snmpSysDescription) {
        m_snmpSysDescription = snmpSysDescription;
    }

    public void setSnmpSysLocation(String snmpSysLocation) {
        m_snmpSysLocation = snmpSysLocation;
    }

    public void setSnmpSysName(String snmpSysName) {
        m_snmpSysName = snmpSysName;
    }

    public void setSnmpSysObjectId(String snmpSysObjectId) {
        m_snmpSysObjectId = snmpSysObjectId;
    }

    public void setState(String state) {
        m_state = state;
    }

    public void setStoragectrl(String storagectrl) {
        m_storagectrl = storagectrl;
    }

    public void setSupportPhone(String supportPhone) {
        m_supportPhone = supportPhone;
    }

    public void setThresholdCategory(String thresholdCategory) {
        m_thresholdCategory = thresholdCategory;
    }

    public void setUsername(String username) {
        m_username = username;
    }

    public void setVendor(String vendor) {
        m_vendor = vendor;
    }

    public void setVendorAssetNumber(String vendorAssetNumber) {
        m_vendorAssetNumber = vendorAssetNumber;
    }

    public void setVendorFax(String vendorFax) {
        m_vendorFax = vendorFax;
    }

    public void setVendorPhone(String vendorPhone) {
        m_vendorPhone = vendorPhone;
    }

    public void setZip(String zip) {
        m_zip = zip;
    }

    public void setVmwareManagedObjectId(String vmwareManagedObjectId) {
        m_vmwareManagedObjectId = vmwareManagedObjectId;
    }

    public void setVmwareManagedEntityType(String vmwareManagedEntityType) {
        m_vmwareManagedEntityType = vmwareManagedEntityType;
    }

    public void setVmwareManagementServer(String vmwareManagementServer) {
        m_vmwareManagementServer = vmwareManagementServer;
    }

    public void setVmwareTopologyInfo(String vmwareTopologyInfo) {
        m_vmwareTopologyInfo = vmwareTopologyInfo;
    }

    public void setVmwareState(String vmwareState) {
        m_vmwareState = vmwareState;
    }

    // --- nice toString() ---
    @Override
    public String toString() {
        return "AssetCommand [m_additionalhardware=" + m_additionalhardware + ", m_address1=" + m_address1
                + ", m_address2=" + m_address2 + ", m_admin=" + m_admin + ", m_allowModify=" + m_allowModify
                + ", m_assetNumber=" + m_assetNumber + ", m_autoenable=" + m_autoenable + ", m_autoenableOptions="
                + m_autoenableOptions + ", m_building=" + m_building + ", m_category=" + m_category + ", m_circuitId="
                + m_circuitId + ", m_city=" + m_city + ", m_longitude=" + m_longitude + ", m_latitude=" + m_latitude
                + ", m_country=" + m_country + ", m_comment=" + m_comment + ", m_connection=" + m_connection
                + ", m_connectionOptions=" + m_connectionOptions + ", m_cpu=" + m_cpu + ", m_dateInstalled="
                + m_dateInstalled + ", m_department=" + m_department + ", m_description=" + m_description
                + ", m_displayCategory=" + m_displayCategory + ", m_division=" + m_division + ", m_enable=" + m_enable
                + ", m_floor=" + m_floor + ", m_country=" + m_country
                + ", m_hdd1=" + m_hdd1 + ", m_hdd2=" + m_hdd2 + ", m_hdd3=" + m_hdd3
                + ", m_hdd4=" + m_hdd4 + ", m_hdd5=" + m_hdd5 + ", m_hdd6=" + m_hdd6 + ", m_id=" + m_id
                + ", m_inputpower=" + m_inputpower + ", m_lastModifiedBy=" + m_lastModifiedBy + ", m_lastModifiedDate="
                + m_lastModifiedDate + ", m_lease=" + m_lease + ", m_leaseExpires=" + m_leaseExpires
                + ", m_loggedInUser=" + m_loggedInUser + ", m_maintcontract=" + m_maintcontract
                + ", m_maintContractExpiration=" + m_maintContractExpiration + ", m_manufacturer=" + m_manufacturer
                + ", m_modelNumber=" + m_modelNumber + ", m_nextNodeId=" + m_nextNodeId + ", m_nodeId=" + m_nodeId
                + ", m_nodeLabel=" + m_nodeLabel + ", m_notifyCategory=" + m_notifyCategory + ", m_numpowersupplies="
                + m_numpowersupplies + ", m_operatingSystem=" + m_operatingSystem + ", m_password=" + m_password
                + ", m_pollerCategory=" + m_pollerCategory + ", m_port=" + m_port + ", m_previousNodeId="
                + m_previousNodeId + ", m_rack=" + m_rack + ", m_rackunitheight=" + m_rackunitheight + ", m_ram="
                + m_ram + ", m_region=" + m_region + ", m_room=" + m_room + ", m_serialNumber=" + m_serialNumber
                + ", m_slot=" + m_slot + ", m_snmpcommunity=" + m_snmpcommunity + ", m_snmpSysContact="
                + m_snmpSysContact + ", m_snmpSysDescription=" + m_snmpSysDescription + ", m_snmpSysLocation="
                + m_snmpSysLocation + ", m_snmpSysName=" + m_snmpSysName + ", m_snmpSysObjectId=" + m_snmpSysObjectId
                + ", m_state=" + m_state + ", m_storagectrl=" + m_storagectrl + ", m_supportPhone=" + m_supportPhone
                + ", m_thresholdCategory=" + m_thresholdCategory + ", m_username=" + m_username + ", m_vendor="
                + m_vendor + ", m_vendorAssetNumber=" + m_vendorAssetNumber + ", m_vendorFax=" + m_vendorFax
                + ", m_vendorPhone=" + m_vendorPhone + ", m_zip=" + m_zip + ", m_vmwareManagedObjectId="
                + m_vmwareManagedObjectId + ", m_vmwareManagedEntityType=" + m_vmwareManagedEntityType
                + ", m_vmwareManagementServer=" + m_vmwareManagementServer + ", m_vmwareTopologyInfo=" + m_vmwareTopologyInfo +
                ", m_vmwareState=" + m_vmwareState + "]";
    }
}
