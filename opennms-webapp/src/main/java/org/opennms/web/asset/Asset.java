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

package org.opennms.web.asset;

import java.util.Date;

/**
 * <p>Asset class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Asset extends Object {

    /** Constant <code>UNSPECIFIED_CATEGORY="Unspecified"</code> */
    public static final String UNSPECIFIED_CATEGORY = "Unspecified";

    /** Constant <code>INFRASTRUCTURE_CATEGORY="Infrastructure"</code> */
    public static final String INFRASTRUCTURE_CATEGORY = "Infrastructure";

    /** Constant <code>SERVER_CATEGORY="Server"</code> */
    public static final String SERVER_CATEGORY = "Server";

    /** Constant <code>DESKTOP_CATEGORY="Desktop"</code> */
    public static final String DESKTOP_CATEGORY = "Desktop";

    /** Constant <code>LAPTOP_CATEGORY="Laptop"</code> */
    public static final String LAPTOP_CATEGORY = "Laptop";

    /** Constant <code>PRINTER_CATEGORY="Printer"</code> */
    public static final String PRINTER_CATEGORY = "Printer";

    /** Constant <code>TELEPHONY_CATEGORY="Telephony"</code> */
    public static final String TELEPHONY_CATEGORY = "Telephony";

    /** Constant <code>OTHER_CATEGORY="Other"</code> */
    public static final String OTHER_CATEGORY = "Other";

    /** Constant <code>CATEGORIES="new String[] { UNSPECIFIED_CATEGORY, IN"{trunked}</code> */
    public static final String[] CATEGORIES = new String[] { UNSPECIFIED_CATEGORY, INFRASTRUCTURE_CATEGORY, SERVER_CATEGORY, DESKTOP_CATEGORY, LAPTOP_CATEGORY, PRINTER_CATEGORY, TELEPHONY_CATEGORY, OTHER_CATEGORY };

    /** Constant <code>AUTOENABLE="A"</code> */
    public static final String AUTOENABLE = "A";
    
    /** Constant <code>AUTOENABLES="new String[] { AUTOENABLE }"</code> */
    public static final String[] AUTOENABLES = new String[] { AUTOENABLE };

    /** Constant <code>TELNET_CONNECTION="telnet"</code> */
    public static final String TELNET_CONNECTION = "telnet";
    
    /** Constant <code>SSH_CONNECTION="ssh"</code> */
    public static final String SSH_CONNECTION = "ssh";
    
    /** Constant <code>RSH_CONNECTION="rsh"</code> */
    public static final String RSH_CONNECTION = "rsh";
    
    /** Constant <code>CONNECTIONS="new String[] { TELNET_CONNECTION,SSH_CO"{trunked}</code> */
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

    protected String country = "";

    protected String longitude = "";

    protected String latitude = "";

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
    
    protected String cpu ="";
    
    protected String ram ="";

    protected String storagectrl ="";
    
    protected String hdd1 ="";

    protected String hdd2 ="";

    protected String hdd3 ="";

    protected String hdd4 ="";

    protected String hdd5 ="";

    protected String hdd6 ="";

    protected String numpowersupplies ="";

    protected String inputpower ="";

    protected String additionalhardware ="";

    protected String admin ="";

    protected String snmpcommunity ="";

    protected String rackunitheight ="";

    /**
     * <p>Getter for the field <code>rackunitheight</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRackunitheight() {
        return rackunitheight;
    }

    /**
     * <p>Setter for the field <code>rackunitheight</code>.</p>
     *
     * @param rackunitheight a {@link java.lang.String} object.
     */
    public void setRackunitheight(String rackunitheight) {
        if (rackunitheight != null) {
            this.rackunitheight = rackunitheight;
        } else {
            this.rackunitheight = "";
        }        
      }
    /**
     * <p>Getter for the field <code>snmpcommunity</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpcommunity() {
        return snmpcommunity;
    }

    /**
     * <p>Setter for the field <code>snmpcommunity</code>.</p>
     *
     * @param snmpcommunity a {@link java.lang.String} object.
     */
    public void setSnmpcommunity(String snmpcommunity) {
        if (snmpcommunity != null) {
            this.snmpcommunity = snmpcommunity;
        } else {
            this.snmpcommunity = "";
        }        
      }
    /**
     * <p>Getter for the field <code>admin</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAdmin() {
        return admin;
    }

    /**
     * <p>Setter for the field <code>admin</code>.</p>
     *
     * @param admin a {@link java.lang.String} object.
     */
    public void setAdmin(String admin) {
        if (admin != null) {
            this.admin = admin;
        } else {
            this.admin = "";
        }        
      }
    /**
     * <p>Getter for the field <code>additionalhardware</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAdditionalhardware() {
        return additionalhardware;
    }

    /**
     * <p>Setter for the field <code>additionalhardware</code>.</p>
     *
     * @param additionalhardware a {@link java.lang.String} object.
     */
    public void setAdditionalhardware(String additionalhardware) {
        if (additionalhardware != null) {
            this.additionalhardware = additionalhardware;
        } else {
            this.additionalhardware = "";
        }        
      }
    /**
     * <p>Getter for the field <code>inputpower</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInputpower() {
        return inputpower;
    }

    /**
     * <p>Setter for the field <code>inputpower</code>.</p>
     *
     * @param inputpower a {@link java.lang.String} object.
     */
    public void setInputpower(String inputpower) {
        if (inputpower != null) {
            this.inputpower = inputpower;
        } else {
            this.inputpower = "";
        }        
      }
    /**
     * <p>Getter for the field <code>numpowersupplies</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNumpowersupplies() {
        return numpowersupplies;
    }

    /**
     * <p>Setter for the field <code>numpowersupplies</code>.</p>
     *
     * @param numpowersupplies a {@link java.lang.String} object.
     */
    public void setNumpowersupplies(String numpowersupplies) {
        if (numpowersupplies != null) {
            this.numpowersupplies = numpowersupplies;
        } else {
            this.numpowersupplies = "";
        }        
      }
    /**
     * <p>Getter for the field <code>hdd6</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHdd6() {
        return hdd6;
    }

    /**
     * <p>Setter for the field <code>hdd6</code>.</p>
     *
     * @param hdd6 a {@link java.lang.String} object.
     */
    public void setHdd6(String hdd6) {
        if (hdd6 != null) {
            this.hdd6 = hdd6;
        } else {
            this.hdd6 = "";
        }        
      }
    /**
     * <p>Getter for the field <code>hdd5</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHdd5() {
        return hdd5;
    }

    /**
     * <p>Setter for the field <code>hdd5</code>.</p>
     *
     * @param hdd5 a {@link java.lang.String} object.
     */
    public void setHdd5(String hdd5) {
        if (hdd5 != null) {
            this.hdd5 = hdd5;
        } else {
            this.hdd5 = "";
        }        
      }
    /**
     * <p>Getter for the field <code>hdd4</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHdd4() {
        return hdd4;
    }

    /**
     * <p>Setter for the field <code>hdd4</code>.</p>
     *
     * @param hdd4 a {@link java.lang.String} object.
     */
    public void setHdd4(String hdd4) {
        if (hdd4 != null) {
            this.hdd4 = hdd4;
        } else {
            this.hdd4 = "";
        }        
      }
    /**
     * <p>Getter for the field <code>hdd3</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHdd3() {
        return hdd3;
    }

    /**
     * <p>Setter for the field <code>hdd3</code>.</p>
     *
     * @param hdd3 a {@link java.lang.String} object.
     */
    public void setHdd3(String hdd3) {
        if (hdd3 != null) {
            this.hdd3 = hdd3;
        } else {
            this.hdd3 = "";
        }        
      }
    /**
     * <p>Getter for the field <code>hdd2</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHdd2() {
        return hdd2;
    }

    /**
     * <p>Setter for the field <code>hdd2</code>.</p>
     *
     * @param hdd2 a {@link java.lang.String} object.
     */
    public void setHdd2(String hdd2) {
        if (hdd2 != null) {
            this.hdd2 = hdd2;
        } else {
            this.hdd2 = "";
        }        
      }
    /**
     * <p>Getter for the field <code>hdd1</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHdd1() {
        return hdd1;
    }

    /**
     * <p>Setter for the field <code>hdd1</code>.</p>
     *
     * @param hdd1 a {@link java.lang.String} object.
     */
    public void setHdd1(String hdd1) {
        if (hdd1 != null) {
            this.hdd1 = hdd1;
        } else {
            this.hdd1 = "";
        }        
      }

    /**
     * <p>Getter for the field <code>storagectrl</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStoragectrl() {
        return storagectrl;
    }

    /**
     * <p>Setter for the field <code>storagectrl</code>.</p>
     *
     * @param storagectrl a {@link java.lang.String} object.
     */
    public void setStoragectrl(String storagectrl) {
        if (storagectrl != null) {
            this.storagectrl = storagectrl;
        } else {
            this.storagectrl = "";
        }        
      }
    
    /**
     * <p>Setter for the field <code>nodeId</code>.</p>
     *
     * @param nodeId a int.
     */
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * <p>Getter for the field <code>nodeId</code>.</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return (this.nodeId);
    }

    /**
     * <p>Setter for the field <code>lastModifiedDate</code>.</p>
     *
     * @param lastModifiedDate a {@link java.util.Date} object.
     */
    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * <p>Getter for the field <code>lastModifiedDate</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getLastModifiedDate() {
        return (this.lastModifiedDate);
    }

    /**
     * <p>Setter for the field <code>category</code>.</p>
     *
     * @param category a {@link java.lang.String} object.
     */
    public void setCategory(String category) {
        if (category != null) {
            this.category = category;
        } else {
            this.category = UNSPECIFIED_CATEGORY;
        }
    }

    /**
     * <p>Getter for the field <code>category</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCategory() {
        return (this.category);
    }

    /**
     * <p>Setter for the field <code>displayCategory</code>.</p>
     *
     * @param displayCategory a {@link java.lang.String} object.
     */
    public void setDisplayCategory(String displayCategory) {
        if (displayCategory != null) {
            this.displayCategory = displayCategory;
        } else {
            this.displayCategory = "";
        }
    }

    /**
     * <p>Getter for the field <code>displayCategory</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDisplayCategory() {
        return (this.displayCategory);
    }

    /**
     * <p>Setter for the field <code>notifyCategory</code>.</p>
     *
     * @param notifyCategory a {@link java.lang.String} object.
     */
    public void setNotifyCategory(String notifyCategory) {
        if (notifyCategory != null) {
            this.notifyCategory = notifyCategory;
        } else {
            this.notifyCategory = "";
        }
    }

    /**
     * <p>Getter for the field <code>notifyCategory</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNotifyCategory() {
        return (this.notifyCategory);
    }

    /**
     * <p>Setter for the field <code>pollerCategory</code>.</p>
     *
     * @param pollerCategory a {@link java.lang.String} object.
     */
    public void setPollerCategory(String pollerCategory) {
        if (pollerCategory != null) {
            this.pollerCategory = pollerCategory;
        } else {
            this.pollerCategory = "";
        }
    }

    /**
     * <p>Getter for the field <code>pollerCategory</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPollerCategory() {
        return (this.pollerCategory);
    }

    /**
     * <p>Setter for the field <code>thresholdCategory</code>.</p>
     *
     * @param thresholdCategory a {@link java.lang.String} object.
     */
    public void setThresholdCategory(String thresholdCategory) {
        if (thresholdCategory != null) {
            this.thresholdCategory = thresholdCategory;
        } else {
            this.thresholdCategory = "";
        }
    }

    /**
     * <p>Getter for the field <code>thresholdCategory</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getThresholdCategory() {
        return (this.thresholdCategory);
    }

    /**
     * <p>Setter for the field <code>manufacturer</code>.</p>
     *
     * @param manufacturer a {@link java.lang.String} object.
     */
    public void setManufacturer(String manufacturer) {
        if (manufacturer != null) {
            this.manufacturer = manufacturer;
        } else {
            this.manufacturer = "";
        }
    }

    /**
     * <p>Getter for the field <code>manufacturer</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getManufacturer() {
        return (this.manufacturer);
    }

    /**
     * <p>Setter for the field <code>vendor</code>.</p>
     *
     * @param vendor a {@link java.lang.String} object.
     */
    public void setVendor(String vendor) {
        if (vendor != null) {
            this.vendor = vendor;
        } else {
            this.vendor = "";
        }
    }

    /**
     * <p>Getter for the field <code>vendor</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVendor() {
        return (this.vendor);
    }

    /**
     * <p>Setter for the field <code>modelNumber</code>.</p>
     *
     * @param modelNumber a {@link java.lang.String} object.
     */
    public void setModelNumber(String modelNumber) {
        if (modelNumber != null) {
            this.modelNumber = modelNumber;
        } else {
            this.modelNumber = "";
        }
    }

    /**
     * <p>Getter for the field <code>modelNumber</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getModelNumber() {
        return (this.modelNumber);
    }

    /**
     * <p>Setter for the field <code>serialNumber</code>.</p>
     *
     * @param serialNumber a {@link java.lang.String} object.
     */
    public void setSerialNumber(String serialNumber) {
        if (serialNumber != null) {
            this.serialNumber = serialNumber;
        } else {
            this.serialNumber = "";
        }
    }

    /**
     * <p>Getter for the field <code>serialNumber</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSerialNumber() {
        return (this.serialNumber);
    }

    /**
     * <p>Setter for the field <code>description</code>.</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        if (description != null) {
            this.description = description;
        } else {
            this.description = "";
        }
    }

    /**
     * <p>Getter for the field <code>description</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return (this.description);
    }

    /**
     * <p>Setter for the field <code>circuitId</code>.</p>
     *
     * @param circuitId a {@link java.lang.String} object.
     */
    public void setCircuitId(String circuitId) {
        if (circuitId != null) {
            this.circuitId = circuitId;
        } else {
            this.circuitId = "";
        }
    }

    /**
     * <p>Getter for the field <code>circuitId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCircuitId() {
        return (this.circuitId);
    }

    /**
     * <p>Setter for the field <code>assetNumber</code>.</p>
     *
     * @param assetNumber a {@link java.lang.String} object.
     */
    public void setAssetNumber(String assetNumber) {
        if (assetNumber != null) {
            this.assetNumber = assetNumber;
        } else {
            this.assetNumber = "";
        }
    }

    /**
     * <p>Getter for the field <code>assetNumber</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAssetNumber() {
        return (this.assetNumber);
    }

    /**
     * <p>Setter for the field <code>operatingSystem</code>.</p>
     *
     * @param operatingSystem a {@link java.lang.String} object.
     */
    public void setOperatingSystem(String operatingSystem) {
        if (operatingSystem != null) {
            this.operatingSystem = operatingSystem;
        } else {
            this.operatingSystem = "";
        }
    }

    /**
     * <p>Getter for the field <code>operatingSystem</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOperatingSystem() {
        return (this.operatingSystem);
    }

    /**
     * <p>Setter for the field <code>rack</code>.</p>
     *
     * @param rack a {@link java.lang.String} object.
     */
    public void setRack(String rack) {
        if (rack != null) {
            this.rack = rack;
        } else {
            this.rack = "";
        }
    }

    /**
     * <p>Getter for the field <code>rack</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRack() {
        return (this.rack);
    }

    /**
     * <p>Setter for the field <code>slot</code>.</p>
     *
     * @param slot a {@link java.lang.String} object.
     */
    public void setSlot(String slot) {
        if (slot != null) {
            this.slot = slot;
        } else {
            this.slot = "";
        }
    }

    /**
     * <p>Getter for the field <code>slot</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSlot() {
        return (this.slot);
    }

    /**
     * <p>Setter for the field <code>port</code>.</p>
     *
     * @param port a {@link java.lang.String} object.
     */
    public void setPort(String port) {
        if (port != null) {
            this.port = port;
        } else {
            this.port = "";
        }
    }

    /**
     * <p>Getter for the field <code>port</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPort() {
        return (this.port);
    }

    /**
     * <p>Setter for the field <code>region</code>.</p>
     *
     * @param region a {@link java.lang.String} object.
     */
    public void setRegion(String region) {
        if (region != null) {
            this.region = region;
        } else {
            this.region = "";
        }
    }

    /**
     * <p>Getter for the field <code>region</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRegion() {
        return (this.region);
    }

    /**
     * <p>Setter for the field <code>division</code>.</p>
     *
     * @param division a {@link java.lang.String} object.
     */
    public void setDivision(String division) {
        if (division != null) {
            this.division = division;
        } else {
            this.division = "";
        }
    }

    /**
     * <p>Getter for the field <code>division</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDivision() {
        return (this.division);
    }

    /**
     * <p>Setter for the field <code>department</code>.</p>
     *
     * @param department a {@link java.lang.String} object.
     */
    public void setDepartment(String department) {
        if (department != null) {
            this.department = department;
        } else {
            this.department = "";
        }
    }

    /**
     * <p>Getter for the field <code>department</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDepartment() {
        return (this.department);
    }

    /**
     * <p>Setter for the field <code>address1</code>.</p>
     *
     * @param address1 a {@link java.lang.String} object.
     */
    public void setAddress1(String address1) {
        if (address1 != null) {
            this.address1 = address1;
        } else {
            this.address1 = "";
        }
    }

    /**
     * <p>Getter for the field <code>address1</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAddress1() {
        return (this.address1);
    }

    /**
     * <p>Setter for the field <code>address2</code>.</p>
     *
     * @param address2 a {@link java.lang.String} object.
     */
    public void setAddress2(String address2) {
        if (address2 != null) {
            this.address2 = address2;
        } else {
            this.address2 = "";
        }
    }

    /**
     * <p>Getter for the field <code>address2</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAddress2() {
        return (this.address2);
    }

    /**
     * <p>Setter for the field <code>city</code>.</p>
     *
     * @param city a {@link java.lang.String} object.
     */
    public void setCity(String city) {
        if (city != null) {
            this.city = city;
        } else {
            this.city = "";
        }
    }

    /**
     * <p>Getter for the field <code>city</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCity() {
        return (this.city);
    }

    /**
     * <p>Setter for the field <code>state</code>.</p>
     *
     * @param state a {@link java.lang.String} object.
     */
    public void setState(String state) {
        if (state != null) {
            this.state = state;
        } else {
            this.state = "";
        }
    }

    /**
     * <p>Getter for the field <code>state</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getState() {
        return (this.state);
    }

    /**
     * <p>Setter for the field <code>zip</code>.</p>
     *
     * @param zip a {@link java.lang.String} object.
     */
    public void setZip(String zip) {
        if (zip != null) {
            this.zip = zip;
        } else {
            this.zip = "";
        }
    }

    /**
     * <p>Getter for the field <code>zip</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getZip() {
        return (this.zip);
    }

    /**
     * <p>Setter for the field <code>country</code>.</p>
     *
     * @param country a {@link java.lang.String} object.
     */
    public void setCountry(String country) {
        if (country != null) {
            this.country = country;
        } else {
            this.country = "";
        }
    }

    /**
     * <p>Getter for the field <code>country</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCountry() {
        return (this.country);
    }

    /**
     * <p>Setter for the field <code>longitude</code>.</p>
     *
     * @param longitude a {@link java.lang.String} object.
     */
    public void setLongitude(String longitude) {
        if (longitude != null) {
            this.longitude = longitude;
        } else {
            this.longitude = "";
        }
    }

    /**
     * <p>Getter for the field <code>longitude</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLongitude() {
        return (this.longitude);
    }

    /**
     * <p>Setter for the field <code>latitude</code>.</p>
     *
     * @param latitude a {@link java.lang.String} object.
     */
    public void setLatitude(String latitude) {
        if (latitude != null) {
            this.latitude = latitude;
        } else {
            this.latitude = "";
        }
    }

    /**
     * <p>Getter for the field <code>latitude</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLatitude() {
        return (this.latitude);
    }

    /**
     * <p>Setter for the field <code>building</code>.</p>
     *
     * @param building a {@link java.lang.String} object.
     */
    public void setBuilding(String building) {
        if (building != null) {
            this.building = building;
        } else {
            this.building = "";
        }
    }

    /**
     * <p>Getter for the field <code>building</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBuilding() {
        return (this.building);
    }

    /**
     * <p>Setter for the field <code>floor</code>.</p>
     *
     * @param floor a {@link java.lang.String} object.
     */
    public void setFloor(String floor) {
        if (floor != null) {
            this.floor = floor;
        } else {
            this.floor = "";
        }
    }

    /**
     * <p>Getter for the field <code>floor</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFloor() {
        return (this.floor);
    }

    /**
     * <p>Setter for the field <code>room</code>.</p>
     *
     * @param room a {@link java.lang.String} object.
     */
    public void setRoom(String room) {
        if (room != null) {
            this.room = room;
        } else {
            this.room = "";
        }
    }

    /**
     * <p>Getter for the field <code>room</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRoom() {
        return (this.room);
    }

    /**
     * <p>Setter for the field <code>vendorPhone</code>.</p>
     *
     * @param vendorPhone a {@link java.lang.String} object.
     */
    public void setVendorPhone(String vendorPhone) {
        if (vendorPhone != null) {
            this.vendorPhone = vendorPhone;
        } else {
            this.vendorPhone = "";
        }
    }

    /**
     * <p>Getter for the field <code>vendorPhone</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVendorPhone() {
        return (this.vendorPhone);
    }

    /**
     * <p>Setter for the field <code>vendorFax</code>.</p>
     *
     * @param vendorFax a {@link java.lang.String} object.
     */
    public void setVendorFax(String vendorFax) {
        if (vendorFax != null) {
            this.vendorFax = vendorFax;
        } else {
            this.vendorFax = "";
        }
    }

    /**
     * <p>Getter for the field <code>vendorFax</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVendorFax() {
        return (this.vendorFax);
    }

    /**
     * <p>Setter for the field <code>userLastModified</code>.</p>
     *
     * @param userLastModified a {@link java.lang.String} object.
     */
    public void setUserLastModified(String userLastModified) {
        if (userLastModified != null) {
            this.userLastModified = userLastModified;
        } else {
            this.userLastModified = "";
        }
    }

    /**
     * <p>Getter for the field <code>userLastModified</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUserLastModified() {
        return (this.userLastModified);
    }

    /**
     * <p>Setter for the field <code>dateInstalled</code>.</p>
     *
     * @param dateInstalled a {@link java.lang.String} object.
     */
    public void setDateInstalled(String dateInstalled) {
        if (dateInstalled != null) {
            this.dateInstalled = dateInstalled;
        } else {
            this.dateInstalled = "";
        }
    }

    /**
     * <p>Getter for the field <code>dateInstalled</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDateInstalled() {
        return (this.dateInstalled);
    }

    /**
     * <p>Setter for the field <code>lease</code>.</p>
     *
     * @param lease a {@link java.lang.String} object.
     */
    public void setLease(String lease) {
        if (lease != null) {
            this.lease = lease;
        } else {
            this.lease = "";
        }
    }

    /**
     * <p>Getter for the field <code>lease</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLease() {
        return (this.lease);
    }

    /**
     * <p>Setter for the field <code>leaseExpires</code>.</p>
     *
     * @param leaseExpires a {@link java.lang.String} object.
     */
    public void setLeaseExpires(String leaseExpires) {
        if (leaseExpires != null) {
            this.leaseExpires = leaseExpires;
        } else {
            this.leaseExpires = "";
        }
    }

    /**
     * <p>Getter for the field <code>leaseExpires</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLeaseExpires() {
        return (this.leaseExpires);
    }

    /**
     * <p>Setter for the field <code>supportPhone</code>.</p>
     *
     * @param supportPhone a {@link java.lang.String} object.
     */
    public void setSupportPhone(String supportPhone) {
        if (supportPhone != null) {
            this.supportPhone = supportPhone;
        } else {
            this.supportPhone = "";
        }
    }

    /**
     * <p>Getter for the field <code>supportPhone</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSupportPhone() {
        return (this.supportPhone);
    }

    /**
     * <p>Setter for the field <code>maintContract</code>.</p>
     *
     * @param maintContract a {@link java.lang.String} object.
     */
    public void setMaintContract(String maintContract) {
        if (maintContract != null) {
            this.maintContract = maintContract;
        } else {
            this.maintContract = "";
        }
    }

    /**
     * <p>Getter for the field <code>maintContract</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMaintContract() {
        return (this.maintContract);
    }

    /**
     * <p>Setter for the field <code>vendorAssetNumber</code>.</p>
     *
     * @param vendorAssetNumber a {@link java.lang.String} object.
     */
    public void setVendorAssetNumber(String vendorAssetNumber) {
        if (vendorAssetNumber != null) {
            this.vendorAssetNumber = vendorAssetNumber;
        } else {
            this.vendorAssetNumber = "";
        }
    }

    /**
     * <p>Getter for the field <code>vendorAssetNumber</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVendorAssetNumber() {
        return (this.vendorAssetNumber);
    }

    /**
     * <p>Setter for the field <code>maintContractExpires</code>.</p>
     *
     * @param maintContractExpires a {@link java.lang.String} object.
     */
    public void setMaintContractExpires(String maintContractExpires) {
        if (maintContractExpires != null) {
            this.maintContractExpires = maintContractExpires;
        } else {
            this.maintContractExpires = "";
        }
    }

    /**
     * <p>Getter for the field <code>maintContractExpires</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMaintContractExpires() {
        return (this.maintContractExpires);
    }

    /**
     * <p>Setter for the field <code>comments</code>.</p>
     *
     * @param comments a {@link java.lang.String} object.
     */
    public void setComments(String comments) {
        if (comments != null) {
            this.comments = comments;
        } else {
            this.comments = "";
        }
    }

    /**
     * <p>Getter for the field <code>comments</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getComments() {
        return (this.comments);
    }

    /**
     * <p>Getter for the field <code>username</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUsername() {
        return username;
    }

    /**
     * <p>Setter for the field <code>username</code>.</p>
     *
     * @param username a {@link java.lang.String} object.
     */
    public void setUsername(String username) {
        if (username != null) {
            this.username = username;
        } else {
            this.username = "";
        }        
    }

    /**
     * <p>Getter for the field <code>password</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPassword() {
        return password;
    }

    /**
     * <p>Setter for the field <code>password</code>.</p>
     *
     * @param password a {@link java.lang.String} object.
     */
    public void setPassword(String password) {
        if (password != null) {
            this.password = password;
        } else {
            this.password = "";
        }        
    }

    /**
     * <p>Getter for the field <code>enable</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getEnable() {
        return enable;
    }

    /**
     * <p>Setter for the field <code>enable</code>.</p>
     *
     * @param enable a {@link java.lang.String} object.
     */
    public void setEnable(String enable) {
        if (enable != null) {
            this.enable = enable;
        } else {
            this.enable = "";
        }        
    }

    /**
     * <p>Getter for the field <code>connection</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getConnection() {
        return connection;
    }

    /**
     * <p>Setter for the field <code>connection</code>.</p>
     *
     * @param connection a {@link java.lang.String} object.
     */
    public void setConnection(String connection) {
        if (connection != null) {
            this.connection = connection;
        } else {
            this.connection = "";
        }        
   }

    /**
     * <p>Getter for the field <code>autoenable</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAutoenable() {
        return autoenable;
    }

    /**
     * <p>Setter for the field <code>autoenable</code>.</p>
     *
     * @param autoenable a {@link java.lang.String} object.
     */
    public void setAutoenable(String autoenable) {
        if (autoenable != null) {
            this.autoenable = autoenable;
        } else {
            this.autoenable = "";
        }        
      }

    /**
     * <p>Getter for the field <code>cpu</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCpu() {
        return cpu;
    }

    /**
     * <p>Setter for the field <code>cpu</code>.</p>
     *
     * @param cpu a {@link java.lang.String} object.
     */
    public void setCpu(String cpu) {
        if (cpu != null) {
            this.cpu = cpu;
        } else {
            this.cpu = "";
        }        
      }

    /**
     * <p>Getter for the field <code>ram</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRam() {
        return ram;
    }

    /**
     * <p>Setter for the field <code>ram</code>.</p>
     * 
     * @param ram a {@link java.lang.String} object.
     */
    public void setRam(String ram) {
        if (ram != null) {
            this.ram = ram;
        } else {
            this.ram = "";
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Asset [nodeId=");
        builder.append(nodeId);
        builder.append(", lastModifiedDate=");
        builder.append(lastModifiedDate);
        builder.append(", userLastModified=");
        builder.append(userLastModified);
        builder.append(", category=");
        builder.append(category);
        builder.append(", manufacturer=");
        builder.append(manufacturer);
        builder.append(", vendor=");
        builder.append(vendor);
        builder.append(", modelNumber=");
        builder.append(modelNumber);
        builder.append(", serialNumber=");
        builder.append(serialNumber);
        builder.append(", description=");
        builder.append(description);
        builder.append(", circuitId=");
        builder.append(circuitId);
        builder.append(", assetNumber=");
        builder.append(assetNumber);
        builder.append(", operatingSystem=");
        builder.append(operatingSystem);
        builder.append(", rack=");
        builder.append(rack);
        builder.append(", slot=");
        builder.append(slot);
        builder.append(", port=");
        builder.append(port);
        builder.append(", region=");
        builder.append(region);
        builder.append(", division=");
        builder.append(division);
        builder.append(", department=");
        builder.append(department);
        builder.append(", address1=");
        builder.append(address1);
        builder.append(", address2=");
        builder.append(address2);
        builder.append(", city=");
        builder.append(city);
        builder.append(", state=");
        builder.append(state);
        builder.append(", zip=");
        builder.append(zip);
        builder.append(", country=");
        builder.append(country);
        builder.append(", longitude=");
        builder.append(longitude);
        builder.append(", latitude=");
        builder.append(latitude);
        builder.append(", building=");
        builder.append(building);
        builder.append(", floor=");
        builder.append(floor);
        builder.append(", room=");
        builder.append(room);
        builder.append(", vendorPhone=");
        builder.append(vendorPhone);
        builder.append(", vendorFax=");
        builder.append(vendorFax);
        builder.append(", dateInstalled=");
        builder.append(dateInstalled);
        builder.append(", lease=");
        builder.append(lease);
        builder.append(", leaseExpires=");
        builder.append(leaseExpires);
        builder.append(", supportPhone=");
        builder.append(supportPhone);
        builder.append(", maintContract=");
        builder.append(maintContract);
        builder.append(", vendorAssetNumber=");
        builder.append(vendorAssetNumber);
        builder.append(", maintContractExpires=");
        builder.append(maintContractExpires);
        builder.append(", displayCategory=");
        builder.append(displayCategory);
        builder.append(", notifyCategory=");
        builder.append(notifyCategory);
        builder.append(", pollerCategory=");
        builder.append(pollerCategory);
        builder.append(", thresholdCategory=");
        builder.append(thresholdCategory);
        builder.append(", comments=");
        builder.append(comments);
        builder.append(", username=");
        builder.append(username);
        builder.append(", password=");
        builder.append(password);
        builder.append(", enable=");
        builder.append(enable);
        builder.append(", connection=");
        builder.append(connection);
        builder.append(", autoenable=");
        builder.append(autoenable);
        builder.append(", cpu=");
        builder.append(cpu);
        builder.append(", ram=");
        builder.append(ram);
        builder.append(", storagectrl=");
        builder.append(storagectrl);
        builder.append(", hdd1=");
        builder.append(hdd1);
        builder.append(", hdd2=");
        builder.append(hdd2);
        builder.append(", hdd3=");
        builder.append(hdd3);
        builder.append(", hdd4=");
        builder.append(hdd4);
        builder.append(", hdd5=");
        builder.append(hdd5);
        builder.append(", hdd6=");
        builder.append(hdd6);
        builder.append(", numpowersupplies=");
        builder.append(numpowersupplies);
        builder.append(", inputpower=");
        builder.append(inputpower);
        builder.append(", additionalhardware=");
        builder.append(additionalhardware);
        builder.append(", admin=");
        builder.append(admin);
        builder.append(", snmpcommunity=");
        builder.append(snmpcommunity);
        builder.append(", rackunitheight=");
        builder.append(rackunitheight);
        builder.append("]");
        return builder.toString();
    }
}
