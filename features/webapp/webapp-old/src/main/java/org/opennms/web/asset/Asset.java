//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// 2004 Jan 06: Added support for Display, Notify, Poller, and Threshold categories
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.asset;

import java.util.Date;

public class Asset extends Object {

    public static final String UNSPECIFIED_CATEGORY = "Unspecified";

    public static final String INFRASTRUCTURE_CATEGORY = "Infrastructure";

    public static final String SERVER_CATEGORY = "Server";

    public static final String DESKTOP_CATEGORY = "Desktop";

    public static final String LAPTOP_CATEGORY = "Laptop";

    public static final String PRINTER_CATEGORY = "Printer";

    public static final String TELEPHONY_CATEGORY = "Telephony";

    public static final String OTHER_CATEGORY = "Other";

    public static final String[] CATEGORIES = new String[] { UNSPECIFIED_CATEGORY, INFRASTRUCTURE_CATEGORY, SERVER_CATEGORY, DESKTOP_CATEGORY, LAPTOP_CATEGORY, PRINTER_CATEGORY, TELEPHONY_CATEGORY, OTHER_CATEGORY };

    public static final String AUTOENABLE = "A";
    
    public static final String[] AUTOENABLES = new String[] { AUTOENABLE };

    public static final String TELNET_CONNECTION = "telnet";
    
    public static final String SSH_CONNECTION = "ssh";
    
    public static final String RSH_CONNECTION = "rsh";
    
    public static final String[] CONNECTIONS = new String[] { TELNET_CONNECTION,SSH_CONNECTION, RSH_CONNECTION};

    protected int nodeId;

    protected Date lastModifiedDate;

    protected String userLastModified = "";

    protected String category = UNSPECIFIED_CATEGORY;

    protected String manufacturer = "";

    protected String vendor = "";

    protected String modelNumber = "";

    protected String serialNumber = "";

    protected String description = "";

    protected String circuitId = "";

    protected String assetNumber = "";

    protected String operatingSystem = "";

    protected String rack = "";

    protected String slot = "";

    protected String port = "";

    protected String region = "";

    protected String division = "";

    protected String department = "";

    protected String address1 = "";

    protected String address2 = "";

    protected String city = "";

    protected String state = "";

    protected String zip = "";

    protected String building = "";

    protected String floor = "";

    protected String room = "";

    protected String vendorPhone = "";

    protected String vendorFax = "";

    protected String dateInstalled = "";

    protected String lease = "";

    protected String leaseExpires = "";

    protected String supportPhone = "";

    protected String maintContract = "";

    protected String vendorAssetNumber = "";

    protected String maintContractExpires = "";

    protected String displayCategory = "";

    protected String notifyCategory = "";

    protected String pollerCategory = "";

    protected String thresholdCategory = "";

    protected String comments = "";
    
    protected String username ="";
    
    protected String password ="";

    protected String enable ="";

    protected String connection ="";

    protected String autoenable ="";
    
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return (this.nodeId);
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getLastModifiedDate() {
        return (this.lastModifiedDate);
    }

    public void setCategory(String category) {
        if (category != null) {
            this.category = category;
        } else {
            this.category = UNSPECIFIED_CATEGORY;
        }
    }

    public String getCategory() {
        return (this.category);
    }

    public void setDisplayCategory(String displayCategory) {
        if (displayCategory != null) {
            this.displayCategory = displayCategory;
        } else {
            this.displayCategory = "";
        }
    }

    public String getDisplayCategory() {
        return (this.displayCategory);
    }

    public void setNotifyCategory(String notifyCategory) {
        if (notifyCategory != null) {
            this.notifyCategory = notifyCategory;
        } else {
            this.notifyCategory = "";
        }
    }

    public String getNotifyCategory() {
        return (this.notifyCategory);
    }

    public void setPollerCategory(String pollerCategory) {
        if (pollerCategory != null) {
            this.pollerCategory = pollerCategory;
        } else {
            this.pollerCategory = "";
        }
    }

    public String getPollerCategory() {
        return (this.pollerCategory);
    }

    public void setThresholdCategory(String thresholdCategory) {
        if (thresholdCategory != null) {
            this.thresholdCategory = thresholdCategory;
        } else {
            this.thresholdCategory = "";
        }
    }

    public String getThresholdCategory() {
        return (this.thresholdCategory);
    }

    public void setManufacturer(String manufacturer) {
        if (manufacturer != null) {
            this.manufacturer = manufacturer;
        } else {
            this.manufacturer = "";
        }
    }

    public String getManufacturer() {
        return (this.manufacturer);
    }

    public void setVendor(String vendor) {
        if (vendor != null) {
            this.vendor = vendor;
        } else {
            this.vendor = "";
        }
    }

    public String getVendor() {
        return (this.vendor);
    }

    public void setModelNumber(String modelNumber) {
        if (modelNumber != null) {
            this.modelNumber = modelNumber;
        } else {
            this.modelNumber = "";
        }
    }

    public String getModelNumber() {
        return (this.modelNumber);
    }

    public void setSerialNumber(String serialNumber) {
        if (serialNumber != null) {
            this.serialNumber = serialNumber;
        } else {
            this.serialNumber = "";
        }
    }

    public String getSerialNumber() {
        return (this.serialNumber);
    }

    public void setDescription(String description) {
        if (description != null) {
            this.description = description;
        } else {
            this.description = "";
        }
    }

    public String getDescription() {
        return (this.description);
    }

    public void setCircuitId(String circuitId) {
        if (circuitId != null) {
            this.circuitId = circuitId;
        } else {
            this.circuitId = "";
        }
    }

    public String getCircuitId() {
        return (this.circuitId);
    }

    public void setAssetNumber(String assetNumber) {
        if (assetNumber != null) {
            this.assetNumber = assetNumber;
        } else {
            this.assetNumber = "";
        }
    }

    public String getAssetNumber() {
        return (this.assetNumber);
    }

    public void setOperatingSystem(String operatingSystem) {
        if (operatingSystem != null) {
            this.operatingSystem = operatingSystem;
        } else {
            this.operatingSystem = "";
        }
    }

    public String getOperatingSystem() {
        return (this.operatingSystem);
    }

    public void setRack(String rack) {
        if (rack != null) {
            this.rack = rack;
        } else {
            this.rack = "";
        }
    }

    public String getRack() {
        return (this.rack);
    }

    public void setSlot(String slot) {
        if (slot != null) {
            this.slot = slot;
        } else {
            this.slot = "";
        }
    }

    public String getSlot() {
        return (this.slot);
    }

    public void setPort(String port) {
        if (port != null) {
            this.port = port;
        } else {
            this.port = "";
        }
    }

    public String getPort() {
        return (this.port);
    }

    public void setRegion(String region) {
        if (region != null) {
            this.region = region;
        } else {
            this.region = "";
        }
    }

    public String getRegion() {
        return (this.region);
    }

    public void setDivision(String division) {
        if (division != null) {
            this.division = division;
        } else {
            this.division = "";
        }
    }

    public String getDivision() {
        return (this.division);
    }

    public void setDepartment(String department) {
        if (department != null) {
            this.department = department;
        } else {
            this.department = "";
        }
    }

    public String getDepartment() {
        return (this.department);
    }

    public void setAddress1(String address1) {
        if (address1 != null) {
            this.address1 = address1;
        } else {
            this.address1 = "";
        }
    }

    public String getAddress1() {
        return (this.address1);
    }

    public void setAddress2(String address2) {
        if (address2 != null) {
            this.address2 = address2;
        } else {
            this.address2 = "";
        }
    }

    public String getAddress2() {
        return (this.address2);
    }

    public void setCity(String city) {
        if (city != null) {
            this.city = city;
        } else {
            this.city = "";
        }
    }

    public String getCity() {
        return (this.city);
    }

    public void setState(String state) {
        if (state != null) {
            this.state = state;
        } else {
            this.state = "";
        }
    }

    public String getState() {
        return (this.state);
    }

    public void setZip(String zip) {
        if (zip != null) {
            this.zip = zip;
        } else {
            this.zip = "";
        }
    }

    public String getZip() {
        return (this.zip);
    }

    public void setBuilding(String building) {
        if (building != null) {
            this.building = building;
        } else {
            this.building = "";
        }
    }

    public String getBuilding() {
        return (this.building);
    }

    public void setFloor(String floor) {
        if (floor != null) {
            this.floor = floor;
        } else {
            this.floor = "";
        }
    }

    public String getFloor() {
        return (this.floor);
    }

    public void setRoom(String room) {
        if (room != null) {
            this.room = room;
        } else {
            this.room = "";
        }
    }

    public String getRoom() {
        return (this.room);
    }

    public void setVendorPhone(String vendorPhone) {
        if (vendorPhone != null) {
            this.vendorPhone = vendorPhone;
        } else {
            this.vendorPhone = "";
        }
    }

    public String getVendorPhone() {
        return (this.vendorPhone);
    }

    public void setVendorFax(String vendorFax) {
        if (vendorFax != null) {
            this.vendorFax = vendorFax;
        } else {
            this.vendorFax = "";
        }
    }

    public String getVendorFax() {
        return (this.vendorFax);
    }

    public void setUserLastModified(String userLastModified) {
        if (userLastModified != null) {
            this.userLastModified = userLastModified;
        } else {
            this.userLastModified = "";
        }
    }

    public String getUserLastModified() {
        return (this.userLastModified);
    }

    public void setDateInstalled(String dateInstalled) {
        if (dateInstalled != null) {
            this.dateInstalled = dateInstalled;
        } else {
            this.dateInstalled = "";
        }
    }

    public String getDateInstalled() {
        return (this.dateInstalled);
    }

    public void setLease(String lease) {
        if (lease != null) {
            this.lease = lease;
        } else {
            this.lease = "";
        }
    }

    public String getLease() {
        return (this.lease);
    }

    public void setLeaseExpires(String leaseExpires) {
        if (leaseExpires != null) {
            this.leaseExpires = leaseExpires;
        } else {
            this.leaseExpires = "";
        }
    }

    public String getLeaseExpires() {
        return (this.leaseExpires);
    }

    public void setSupportPhone(String supportPhone) {
        if (supportPhone != null) {
            this.supportPhone = supportPhone;
        } else {
            this.supportPhone = "";
        }
    }

    public String getSupportPhone() {
        return (this.supportPhone);
    }

    public void setMaintContract(String maintContract) {
        if (maintContract != null) {
            this.maintContract = maintContract;
        } else {
            this.maintContract = "";
        }
    }

    public String getMaintContract() {
        return (this.maintContract);
    }

    public void setVendorAssetNumber(String vendorAssetNumber) {
        if (vendorAssetNumber != null) {
            this.vendorAssetNumber = vendorAssetNumber;
        } else {
            this.vendorAssetNumber = "";
        }
    }

    public String getVendorAssetNumber() {
        return (this.vendorAssetNumber);
    }

    public void setMaintContractExpires(String maintContractExpires) {
        if (maintContractExpires != null) {
            this.maintContractExpires = maintContractExpires;
        } else {
            this.maintContractExpires = "";
        }
    }

    public String getMaintContractExpires() {
        return (this.maintContractExpires);
    }

    public void setComments(String comments) {
        if (comments != null) {
            this.comments = comments;
        } else {
            this.comments = "";
        }
    }

    public String getComments() {
        return (this.comments);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username != null) {
            this.username = username;
        } else {
            this.username = "";
        }        
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null) {
            this.password = password;
        } else {
            this.password = "";
        }        
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        if (enable != null) {
            this.enable = enable;
        } else {
            this.enable = "";
        }        
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        if (connection != null) {
            this.connection = connection;
        } else {
            this.connection = "";
        }        
   }

    public String getAutoenable() {
        return autoenable;
    }

    public void setAutoenable(String autoenable) {
        if (autoenable != null) {
            this.autoenable = autoenable;
        } else {
            this.autoenable = "";
        }        
      }

}
