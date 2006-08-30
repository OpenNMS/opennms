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
package org.opennms.netmgt.dao.jdbc.asset;

import java.io.Serializable;
import java.util.Date;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;

public class LazyAssetRecord extends OnmsAssetRecord {
	
	private boolean m_loaded = false;
	private DataSource m_dataSource;
	private boolean m_dirty;
	
	public LazyAssetRecord(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	public String getAddress1() {
		load();
		return super.getAddress1();
	}

	public String getAddress2() {
		load();
		return super.getAddress2();
	}

	public String getAssetNumber() {
		load();
		return super.getAssetNumber();
	}

	public String getBuilding() {
		load();
		return super.getBuilding();
	}

	public String getCategory() {
		load();
		return super.getCategory();
	}

	public String getCircuitId() {
		load();
		return super.getCircuitId();
	}

	public String getCity() {
		load();
		return super.getCity();
	}

	public String getComment() {
		load();
		return super.getComment();
	}

	public String getDateInstalled() {
		load();
		return super.getDateInstalled();
	}

	public String getDepartment() {
		load();
		return super.getDepartment();
	}

	public String getDescription() {
		load();
		return super.getDescription();
	}

	public String getDisplayCategory() {
		load();
		return super.getDisplayCategory();
	}

	public String getDivision() {
		load();
		return super.getDivision();
	}

	public String getFloor() {
		load();
		return super.getFloor();
	}

	public String getLastModifiedBy() {
		load();
		return super.getLastModifiedBy();
	}

	public Date getLastModifiedDate() {
		load();
		return super.getLastModifiedDate();
	}

	public String getLease() {
		load();
		return super.getLease();
	}

	public String getLeaseExpires() {
		load();
		return super.getLeaseExpires();
	}

	public String getMaintContractExpiration() {
		load();
		return super.getMaintContractExpiration();
	}

	public String getMaintContractNumber() {
		load();
		return super.getMaintContractNumber();
	}

	public String getManufacturer() {
		load();
		return super.getManufacturer();
	}

	public String getModelNumber() {
		load();
		return super.getModelNumber();
	}

	public String getNotifyCategory() {
		load();
		return super.getNotifyCategory();
	}

	public String getOperatingSystem() {
		load();
		return super.getOperatingSystem();
	}

	public String getPollerCategory() {
		load();
		return super.getPollerCategory();
	}

	public String getPort() {
		load();
		return super.getPort();
	}

	public String getRack() {
		load();
		return super.getRack();
	}

	public String getRegion() {
		load();
		return super.getRegion();
	}

	public String getRoom() {
		load();
		return super.getRoom();
	}

	public String getSerialNumber() {
		load();
		return super.getSerialNumber();
	}

	public String getSlot() {
		load();
		return super.getSlot();
	}

	public String getState() {
		load();
		return super.getState();
	}

	public String getSupportPhone() {
		load();
		return super.getSupportPhone();
	}

	public String getThresholdCategory() {
		load();
		return super.getThresholdCategory();
	}

	public String getVendor() {
		load();
		return super.getVendor();
	}

	public String getVendorAssetNumber() {
		load();
		return super.getVendorAssetNumber();
	}

	public String getVendorFax() {
		load();
		return super.getVendorFax();
	}

	public String getVendorPhone() {
		load();
		return super.getVendorPhone();
	}

	public String getZip() {
		load();
		return super.getZip();
	}
    
    @Override
    public String getManagedObjectInstance() {
       load();
       return super.getManagedObjectInstance();
    }
    
    @Override
    public void setManagedObjectInstance(String moi) {
        load();
        setDirty(true);
        super.setManagedObjectInstance(moi);
    }
    
    @Override
    public String getManagedObjectType() {
        load();
        return super.getManagedObjectType();
    }
    
    @Override
    public void setManagedObjectType(String mot) {
        load();
        setDirty(true);
        super.setManagedObjectType(mot);
    }

	public void setAddress1(String address1) {
		load();
		setDirty(true);
		super.setAddress1(address1);
	}

	public void setAddress2(String address2) {
		load();
		setDirty(true);
		super.setAddress2(address2);
	}

	public void setAssetNumber(String assetnumber) {
		load();
		setDirty(true);
		super.setAssetNumber(assetnumber);
	}

	public void setBuilding(String building) {
		load();
		setDirty(true);
		super.setBuilding(building);
	}

	public void setCategory(String category) {
		load();
		setDirty(true);
		super.setCategory(category);
	}

	public void setCircuitId(String circuitid) {
		load();
		setDirty(true);
		super.setCircuitId(circuitid);
	}

	public void setCity(String city) {
		load();
		setDirty(true);
		super.setCity(city);
	}

	public void setComment(String comment) {
		load();
		setDirty(true);
		super.setComment(comment);
	}

	public void setDateInstalled(String dateinstalled) {
		load();
		setDirty(true);
		super.setDateInstalled(dateinstalled);
	}

	public void setDepartment(String department) {
		load();
		setDirty(true);
		super.setDepartment(department);
	}

	public void setDescription(String description) {
		load();
		setDirty(true);
		super.setDescription(description);
	}

	public void setDisplayCategory(String displaycategory) {
		load();
		setDirty(true);
		super.setDisplayCategory(displaycategory);
	}

	public void setDivision(String division) {
		load();
		setDirty(true);
		super.setDivision(division);
	}

	public void setFloor(String floor) {
		load();
		setDirty(true);
		super.setFloor(floor);
	}

	public void setLastModifiedBy(String userlastmodified) {
		load();
		setDirty(true);
		super.setLastModifiedBy(userlastmodified);
	}

	public void setLastModifiedDate(Date lastmodifieddate) {
		load();
		setDirty(true);
		super.setLastModifiedDate(lastmodifieddate);
	}

	public void setLease(String lease) {
		load();
		setDirty(true);
		super.setLease(lease);
	}

	public void setLeaseExpires(String leaseexpires) {
		load();
		setDirty(true);
		super.setLeaseExpires(leaseexpires);
	}

	public void setMaintContractExpiration(String maintcontractexpires) {
		load();
		setDirty(true);
		super.setMaintContractExpiration(maintcontractexpires);
	}

	public void setMaintContractNumber(String maintcontract) {
		load();
		setDirty(true);
		super.setMaintContractNumber(maintcontract);
	}

	public void setManufacturer(String manufacturer) {
		load();
		setDirty(true);
		super.setManufacturer(manufacturer);
	}

	public void setModelNumber(String modelnumber) {
		load();
		setDirty(true);
		super.setModelNumber(modelnumber);
	}

	public void setNotifyCategory(String notifycategory) {
		load();
		setDirty(true);
		super.setNotifyCategory(notifycategory);
	}

	public void setOperatingSystem(String operatingsystem) {
		load();
		setDirty(true);
		super.setOperatingSystem(operatingsystem);
	}

	public void setPollerCategory(String pollercategory) {
		load();
		setDirty(true);
		super.setPollerCategory(pollercategory);
	}

	public void setPort(String port) {
		load();
		setDirty(true);
		super.setPort(port);
	}

	public void setRack(String rack) {
		load();
		setDirty(true);
		super.setRack(rack);
	}

	public void setRegion(String region) {
		load();
		setDirty(true);
		super.setRegion(region);
	}

	public void setRoom(String room) {
		load();
		setDirty(true);
		super.setRoom(room);
	}

	public void setSerialNumber(String serialnumber) {
		load();
		setDirty(true);
		super.setSerialNumber(serialnumber);
	}

	public void setSlot(String slot) {
		load();
		setDirty(true);
		super.setSlot(slot);
	}

	public void setState(String state) {
		load();
		setDirty(true);
		super.setState(state);
	}

	public void setSupportPhone(String supportphone) {
		load();
		setDirty(true);
		super.setSupportPhone(supportphone);
	}

	public void setThresholdCategory(String thresholdcategory) {
		load();
		setDirty(true);
		super.setThresholdCategory(thresholdcategory);
	}

	public void setVendor(String vendor) {
		load();
		setDirty(true);
		super.setVendor(vendor);
	}

	public void setVendorAssetNumber(String vendorassetnumber) {
		load();
		setDirty(true);
		super.setVendorAssetNumber(vendorassetnumber);
	}

	public void setVendorFax(String vendorfax) {
		load();
		setDirty(true);
		super.setVendorFax(vendorfax);
	}

	public void setVendorPhone(String vendorphone) {
		load();
		setDirty(true);
		super.setVendorPhone(vendorphone);
	}

	public void setZip(String zip) {
		load();
		setDirty(true);
		super.setZip(zip);
	}
    
	public String toString() {
		load();
		setDirty(true);
		return super.toString();
	}

	private void load() {
		if (!m_loaded) {
			new FindByAssetId(m_dataSource).findUnique(getNode().getId());
		}
	}

	public boolean isLoaded() {
		return m_loaded;
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}

	public boolean isDirty() {
		return m_dirty;
	}
	
	public void setDirty(boolean dirty) {
		m_dirty = dirty;
	}

	public void setNode(OnmsNode node) {
		setDirty(true);
		super.setNode(node);
	}


}
