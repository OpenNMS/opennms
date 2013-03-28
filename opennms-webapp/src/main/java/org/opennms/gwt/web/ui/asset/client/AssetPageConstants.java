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

package org.opennms.gwt.web.ui.asset.client;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 *         Basic static string i18n mechanism by GWT. Just add:
 *         DefaultStringValue("English default String")
 *         Key("Key to map value to the translated property files") String
 *         myI18nString() method to get the i18n string
 */
public interface AssetPageConstants extends com.google.gwt.i18n.client.Constants {

    @DefaultStringValue("Additional hardware")
    @Key("additionalhardware")
    String additionalhardware();

    @DefaultStringValue("Additional hardware")
    @Key("additionalhardwareHelp")
    String additionalhardwareHelp();

    @DefaultStringValue("Address 1")
    @Key("address1")
    String address1();

    @DefaultStringValue("Street address of this device (For technician dispatch)")
    @Key("address1Help")
    String address1Help();

    @DefaultStringValue("Longitude")
    @Key("longitude")
    String longitude();

    @DefaultStringValue("Geo Longitude")
    @Key("longitudeHelp")
    String longitudeHelp();

    @DefaultStringValue("Latitude")
    @Key("latitude")
    String latitude();

    @DefaultStringValue("Geo Latitude")
    @Key("latitudeHelp")
    String latitudeHelp();

    @DefaultStringValue("Address 2")
    @Key("address2")
    String address2();

    @DefaultStringValue("Continuation of address")
    @Key("address2Help")
    String address2Help();

    @DefaultStringValue("Admin")
    @Key("admin")
    String admin();

    @DefaultStringValue("Admin contact at the given location")
    @Key("adminHelp")
    String adminHelp();

    @DefaultStringValue("Asset Number")
    @Key("assetNumber")
    String assetNumber();

    @DefaultStringValue("This field should be used if the organization employs asset tags for inventory purposes")
    @Key("assetNumberHelp")
    String assetNumberHelp();

    @DefaultStringValue("Error saving: One or more fields contains an invalid value. Please correct your input and try again. ")
    @Key("assetPageNotValidDontSave")
    String assetPageNotValidDontSave();

    /* Authentication */
    @DefaultStringValue("Authentication")
    @Key("authenticationHeader")
    String authenticationHeader();

    @DefaultStringValue("AutoEnable")
    @Key("autoEnable")
    String autoEnable();

    @DefaultStringValue("Whether the provided authentication user goes directly into 'enable' mode on this node upon login.")
    @Key("autoEnableHelp")
    String autoEnableHelp();

    @DefaultStringValue("Building")
    @Key("building")
    String building();

    @DefaultStringValue("If this address is part of a complex/campus, this field allows additional granularity.")
    @Key("buildingHelp")
    String buildingHelp();

    @DefaultStringValue("Category")
    @Key("category")
    String category();

    @DefaultStringValue("This column is to be used to specify what category of Network Element this device would fall into (E.G. WAN Router, Firewall, Etc.)")
    @Key("categoryHelp")
    String categoryHelp();

    @DefaultStringValue("Circuit ID")
    @Key("circuitId")
    String circuitId();

    @DefaultStringValue("This field should contain the Circuit ID of the ISP/Carrier's designation to which this equipment terminates a connection. For DSL a phone number (or whatever identifying charistic of the LEC should be populated here) to this device's Voice or Data egress.")
    @Key("circuitIdHelp")
    String circuitIdHelp();

    @DefaultStringValue("City")
    @Key("city")
    String city();

    @DefaultStringValue("Continuation of address")
    @Key("cityHelp")
    String cityHelp();

    @DefaultStringValue("Comment")
    @Key("comment")
    String comment();

    @DefaultStringValue("Comments for this asset")
    @Key("commentHelp")
    String commentHelp();

    /* Comments */
    @DefaultStringValue("Comments")
    @Key("commentsHeader")
    String commentsHeader();

    /* Configuration Categories */
    @DefaultStringValue("Configuration Categories")
    @Key("configurationCatHeader")
    String configurationCatHeader();

    @DefaultStringValue("Connection")
    @Key("connection")
    String connection();

    @DefaultStringValue("Connection")
    @Key("connectionHelp")
    String connectionHelp();

    @DefaultStringValue("Contract Expires")
    @Key("contractExpires")
    String contractExpires();

    @DefaultStringValue("Date when maintenance contract expires")
    @Key("contractExpiresHelp")
    String contractExpiresHelp();

    @DefaultStringValue("CPU")
    @Key("cpu")
    String cpu();

    @DefaultStringValue("Type of CPU in this node")
    @Key("cpuHelp")
    String cpuHelp();

    @DefaultStringValue("Custom")
    @Key("custom")
    String custom();

    @DefaultStringValue("Customer contract")
    @Key("customerContract")
    String customerContract();

    @DefaultStringValue("Customer contract expires")
    @Key("customerContractExp")
    String customerContractExp();

    @DefaultStringValue("Customer contract expires")
    @Key("customerContractExpHelp")
    String customerContractExpHelp();

    @DefaultStringValue("Customer contract")
    @Key("customerContractHelp")
    String customerContractHelp();

    /* Customer */
    @DefaultStringValue("Customer")
    @Key("customerHeader")
    String customerHeader();

    @DefaultStringValue("Customer mail")
    @Key("customerMail")
    String customerMail();

    @DefaultStringValue("Customer mail")
    @Key("customerMailHelp")
    String customerMailHelp();

    @DefaultStringValue("Customer name")
    @Key("customerName")
    String customerName();

    @DefaultStringValue("Customer name")
    @Key("customerNameHelp")
    String customerNameHelp();

    @DefaultStringValue("Customer number")
    @Key("customerNumber")
    String customerNumber();

    @DefaultStringValue("Customer number")
    @Key("customerNumberHelp")
    String customerNumberHelp();

    @DefaultStringValue("Customer phone")
    @Key("customerPhone")
    String customerPhone();

    @DefaultStringValue("Customer phone")
    @Key("customerPhoneHelp")
    String customerPhoneHelp();

    /* Custom */
    @DefaultStringValue("Custom")
    @Key("customHeader")
    String customHeader();

    @DefaultStringValue("Custom")
    @Key("customHelp")
    String customHelp();

    @DefaultStringValue("Date Installed")
    @Key("dateInstalled")
    String dateInstalled();

    @DefaultStringValue("A handy dandy place to keep the date this equipment went into service, just in case the bean counters or your boss ask you for any particulars on this device")
    @Key("dateInstalledHelp")
    String dateInstalledHelp();

    @DefaultStringValue("Department")
    @Key("department")
    String department();

    @DefaultStringValue("More of the above, but more simple (E.G. Accounting, Collections, IT, Etc.)")
    @Key("departmentHelp")
    String departmentHelp();

    @DefaultStringValue("Description")
    @Key("description")
    String description();

    @DefaultStringValue("Description of the device's purpose (E.G. Core P2P Router, Egress Internet Router, Etc.)")
    @Key("descriptionHelp")
    String descriptionHelp();

    @DefaultStringValue("Display Category")
    @Key("displayCategory")
    String displayCat();

    @DefaultStringValue("This column is to be used to specify what category of Network Element this device would fall into (E.G. WAN Router, Firewall, Etc.).")
    @Key("displayCategoryHelp")
    String displayCatHelp();

    @DefaultStringValue("Division")
    @Key("division")
    String division();

    @DefaultStringValue("Standard corporate mumbo jumbo for the bean counters getting ever more granular on where money gets spent. Populate as you fee fit, or as dictated.")
    @Key("divisionHelp")
    String divisionHelp();

    @DefaultStringValue("Enable Password")
    @Key("enablePassword")
    String enablePassword();

    @DefaultStringValue("Enable Password: used only if AutoEnable is not set to 'A'")
    @Key("enablePasswordHelp")
    String enablePasswordHelp();

    @DefaultStringValue("Error fetching asset data for node with ID: ")
    @Key("errorFatchingAssetData")
    String errorFatchingAssetData();

    @DefaultStringValue("Error fetching asset suggestion data for node ID: ")
    @Key("errorFetchingAssetSuggData")
    String errorFetchingAssetSuggData();

    @DefaultStringValue("Error saving asset data for node ID: ")
    @Key("errorSavingAssetData")
    String errorSavingAssetData();

    @DefaultStringValue("Fax")
    @Key("fax")
    String fax();

    @DefaultStringValue("Fax number of the above vendor")
    @Key("faxHelp")
    String faxHelp();

    @DefaultStringValue("Floor")
    @Key("floor")
    String floor();

    @DefaultStringValue("Floor on which this node is located, for technician dispatch.")
    @Key("floorHelp")
    String floorHelp();

    /* Hardware */
    @DefaultStringValue("Hardware")
    @Key("hardwareHeader")
    String hardwareHeader();

    @DefaultStringValue("HDD")
    @Key("hdd")
    String hdd();

    @DefaultStringValue("Hard disk drive information")
    @Key("hddHelp")
    String hddHelp();

    /* Identification */
    @DefaultStringValue("Identification")
    @Key("identificationHeader")
    String identificationHeader();

    @DefaultStringValue("Asset Info of Node: ")
    @Key("infoAsset")
    String infoAsset();

    @DefaultStringValue("Loading Asset Info of Node: ")
    @Key("infoAssetLoging")
    String infoAssetLoging();

    @DefaultStringValue("Resetting Asset Info of Node: ")
    @Key("infoAssetRestting")
    String infoAssetRestting();

    @DefaultStringValue("Saved Asset Info of Node: ")
    @Key("infoAssetSaved")
    String infoAssetSaved();

    @DefaultStringValue("Saving Asset Info of Node: ")
    @Key("infoAssetSaving")
    String infoAssetSaving();

    @DefaultStringValue("Inputpower")
    @Key("inputpower")
    String inputpower();

    @DefaultStringValue("Input power type")
    @Key("inputpowerHelp")
    String inputpowerHelp();

    @DefaultStringValue("Last Modified: ")
    @Key("lastModified")
    String lastModified();

    @DefaultStringValue("Lease")
    @Key("lease")
    String lease();

    @DefaultStringValue("Lease Expires")
    @Key("leaseExpires")
    String leaseExpires();

    @DefaultStringValue("If all goes according to plan, this should be a date after you’ve got new equipment commissioned to take over for the service this equipment provides")
    @Key("leaseExpiresHelp")
    String leaseExpiresHelp();

    @DefaultStringValue("A nice spot to populate the name of the leasing company or lease ID for this equipment")
    @Key("leaseHelp")
    String leaseHelp();

    @DefaultStringValue("Changed value, valid")
    @Key("legendGreen")
    String legendGreen();

    @DefaultStringValue("Already saved value")
    @Key("legendGrey")
    String legendGrey();

    @DefaultStringValue("legend")
    @Key("legendHeadline")
    String legendHeadline();

    @DefaultStringValue("Changed value with error, can't be saved")
    @Key("legendRed")
    String legendRed();

    @DefaultStringValue("Changed value with warning, save possible")
    @Key("legendYellow")
    String legendYellow();

    /* Location */
    @DefaultStringValue("Location")
    @Key("locationHeader")
    String locationHeader();

    @DefaultStringValue("Maint Contract Number")
    @Key("maintContract")
    String maintContract();

    @DefaultStringValue("Number / ID of maintenance contract")
    @Key("maintContractHelp")
    String maintContractHelp();

    @DefaultStringValue("Maint Phone")
    @Key("maintPhone")
    String maintPhone();

    @DefaultStringValue("Phone number for technical operational support for the device in question (Think Helpdesk, Phone Company, ISP NOC, Etc.)")
    @Key("maintPhoneHelp")
    String maintPhoneHelp();

    @DefaultStringValue("Manufacturer")
    @Key("manufacturer")
    String manufacturer();

    @DefaultStringValue("Manufacturer -Self explanatory")
    @Key("manufacturerHelp")
    String manufacturerHelp();

    @DefaultStringValue("Model Number")
    @Key("modelNumber")
    String modelNumber();

    @DefaultStringValue("Model number of the device (E.G. Cisco 3845, Oki B4400, Etc.)")
    @Key("modelNumberHelp")
    String modelNumberHelp();

    @DefaultStringValue("Node ID: ")
    @Key("nodeIdLabel")
    String nodeIdLabel();

    @DefaultStringValue("General Information")
    @Key("nodeInfoLink")
    String nodeInfoLink();

    @DefaultStringValue("Parameter node is not an parseable Node ID: ")
    @Key("nodeParamNotValidInt")
    String nodeParamNotValidInt();

    @DefaultStringValue("Notification Category")
    @Key("notificationCategory")
    String notificationCat();

    @DefaultStringValue("This could be something like 'serverAdmin' or 'networkAdmin' to be used in filter rules for directing notifications.")
    @Key("notificationCategoryHelp")
    String notificationCatHelp();

    @DefaultStringValue("Number of power supplies")
    @Key("numpowersupplies")
    String numpowersupplies();

    @DefaultStringValue("Number of power supplies")
    @Key("numpowersuppliesHelp")
    String numpowersuppliesHelp();

    @DefaultStringValue("Operating System")
    @Key("operatingSystem")
    String operatingSystem();

    @DefaultStringValue("Self explanatory")
    @Key("operatingSystemHelp")
    String operatingSystemHelp();

    @DefaultStringValue("Password")
    @Key("password")
    String password();

    @DefaultStringValue("Password")
    @Key("passwordHelp")
    String passwordHelp();

    @DefaultStringValue("Phone")
    @Key("phone")
    String phone();

    @DefaultStringValue("Phone number of vendor that services (or provides service to) this equipment (E.G. ISP, PBX Vendor, Phone company, etc.)")
    @Key("phoneHelp")
    String phoneHelp();

    @DefaultStringValue("Poller Category")
    @Key("pollerCategory")
    String pollerCat();

    @DefaultStringValue("This is to be used in filter rules to define devices in a particular poller package.")
    @Key("pollerCategoryHelp")
    String pollerCatHelp();

    @DefaultStringValue("Port")
    @Key("port")
    String port();

    @DefaultStringValue("Port on a given card or device being monitored")
    @Key("portHelp")
    String portHelp();

    @DefaultStringValue("Rack")
    @Key("rack")
    String rack();

    @DefaultStringValue("This field should be used to designate the rack in specific that this piece of equipment is located in at a given location (E.G. Server3; Network5; Isle C-Bay5;105.12, Etc.) preferably by using both Bay & Isle coordinates")
    @Key("rackHelp")
    String rackHelp();

    @DefaultStringValue("Rack unit height")
    @Key("rackUnitHeight")
    String rackUnitHeight();

    @DefaultStringValue("Rack unit height of node: 1, 2, 3, ...")
    @Key("rackUnitHeightHelp")
    String rackUnitHeightHelp();

    @DefaultStringValue("RAM")
    @Key("ram")
    String ram();

    @DefaultStringValue("RAM")
    @Key("ramHelp")
    String ramHelp();

    @DefaultStringValue("Region")
    @Key("region")
    String region();

    @DefaultStringValue("On a geographically or otherwise determined regional basis")
    @Key("regionHelp")
    String regionHelp();

    @DefaultStringValue("Reset")
    @Key("resetButton")
    String resetButton();

    @DefaultStringValue("Room")
    @Key("room")
    String room();

    @DefaultStringValue("Room number where this node is located, for technician dispatch")
    @Key("roomHelp")
    String roomHelp();

    /* Submit */
    @DefaultStringValue("Save")
    @Key("saveButton")
    String saveButton();

    @DefaultStringValue("Serial Number")
    @Key("serialNumber")
    String serialNumber();

    @DefaultStringValue("Self explanatory")
    @Key("serialNumberHelp")
    String serialNumberHelp();

    @DefaultStringValue("Slot")
    @Key("slot")
    String slot();

    @DefaultStringValue("This field should be used to designate what slot in a chassis/shelf this node occupies")
    @Key("slotHelp")
    String slotHelp();

    @DefaultStringValue("SNMP community")
    @Key("snmpcommunity")
    String snmpcommunity();

    @DefaultStringValue("SNMP community string")
    @Key("snmpcommunityHelp")
    String snmpcommunityHelp();

    /* SNMP Labels */
    @DefaultStringValue("SNMP Info")
    @Key("snmpHeader")
    String snmpHeader();

    @DefaultStringValue("State")
    @Key("state")
    String state();

    @DefaultStringValue("Continuation of address")
    @Key("stateHelp")
    String stateHelp();

    @DefaultStringValue("Storage Controller")
    @Key("storagectrl")
    String storagectrl();

    @DefaultStringValue("Storage Controller")
    @Key("storagectrlHelp")
    String storagectrlHelp();

    @DefaultStringValue("Can't read given text as date. Please use the date picker.")
    @Key("stringNotADate")
    String stringNotADate();

    @DefaultStringValue("Input is not an parseable as Integer: ")
    @Key("stringNoValidInteger")
    String stringNoValidInteger();

    @DefaultStringValue("The text is too long; maximum length is: ")
    @Key("stringToLongError")
    String stringToLongError();

    @DefaultStringValue("Please just use A-Z a-z 0-9 or - and _ to avoid configuration problems")
    @Key("stringBasicValidationError")
    String stringBasicValidationError();

    @DefaultStringValue("The text contains whitespaces; please remove them")
    @Key("stringContainsWhiteSpacesError")
    String stringContainsWhiteSpacesError();

    @DefaultStringValue("The text dosen't maches the ergexp; ")
    @Key("stringNotMatchingRegexpError")
    String stringNotMatchingRegexpError();

    @DefaultStringValue("System Contact")
    @Key("systemContact")
    String systemContact();

    @DefaultStringValue("System Contact, information from SNMP agent")
    @Key("systemContactHelp")
    String systemContactHelp();

    @DefaultStringValue("System Description")
    @Key("systemDescription")
    String systemDescription();

    @DefaultStringValue("System Description, information from SNMP agent")
    @Key("systemDescriptionHelp")
    String systemDescriptionHelp();

    @DefaultStringValue("System Id")
    @Key("systemId")
    String systemId();

    @DefaultStringValue("System ID, information from SNMP agent")
    @Key("systemIdHelp")
    String systemIdHelp();

    @DefaultStringValue("System Location")
    @Key("systemLocation")
    String systemLocation();

    @DefaultStringValue("System Location, information from SNMP agent")
    @Key("systemLocationHelp")
    String systemLocationHelp();

    @DefaultStringValue("System Name")
    @Key("systemName")
    String systemName();

    @DefaultStringValue("System Name, information from SNMP agent")
    @Key("systemNameHelp")
    String systemNameHelp();

    @DefaultStringValue("Threshold Category")
    @Key("thresholdCategory")
    String thresholdCat();

    @DefaultStringValue("This is to be used in filter rules to define devices in a particular thresholding package.")
    @Key("thresholdCategoryHelp")
    String thresholdCatHelp();

    @DefaultStringValue("Username")
    @Key("username")
    String username();

    @DefaultStringValue("Username")
    @Key("usernameHelp")
    String usernameHelp();

    @DefaultStringValue("Vendor Asset")
    @Key("vendorAsset")
    String vendorAsset();

    @DefaultStringValue("If the vendor that supplies this equipment uses a asset tag of their own, populate that data here")
    @Key("vendorAssetHelp")
    String vendorAssetHelp();

    /* Vendor */
    @DefaultStringValue("Vendor")
    @Key("vendorHeader")
    String vendorHeader();

    @DefaultStringValue("Name")
    @Key("vendorName")
    String vendorName();

    @DefaultStringValue("Vendor who provides service for this device (If applicable, E.G. ISP, Local PBX Maintenance vendor, etc.)")
    @Key("vendorNameHelp")
    String vendorNameHelp();

    @DefaultStringValue("ZIP")
    @Key("zip")
    String zip();

    @DefaultStringValue("Postal code (ZIP code)")
    @Key("zipHelp")
    String zipHelp();

    @DefaultStringValue("Country")
    @Key("country")
    String country();

    @DefaultStringValue("Country")
    @Key("countryHelp")
    String countryHelp();

    /* VMware asset fields */
    @DefaultStringValue("VMware")
    @Key("vmwareHeader")
    String vmwareHeader();

    @DefaultStringValue("VMware managed object ID")
    @Key("vmwareManagedObjectId")
    String vmwareManagedObjectId();

    @DefaultStringValue("Internal id in VMware vCenter")
    @Key("vmwareManagedObjectIdHelp")
    String vmwareManagedObjectIdHelp();

    @DefaultStringValue("VMware managed entity type")
    @Key("vmwareManagedEntityType")
    String vmwareManagedEntityType();

    @DefaultStringValue("Defines a VMware host system or virtual machine")
    @Key("vmwareManagedEntityTypeHelp")
    String vmwareManagedEntityTypeHelp();

    @DefaultStringValue("VMware management server")
    @Key("vmwareManagementServer")
    String vmwareManagementServer();

    @DefaultStringValue("VMware vCenter host")
    @Key("vmwareManagementServerHelp")
    String vmwareManagementServerHelp();

    @DefaultStringValue("VMware Topology Info")
    @Key("vmwareTopologyInfo")
    String vmwareTopologyInfo();

    @DefaultStringValue("VMware topology information")
    @Key("vmwareTopologyInfoHelp")
    String vmwareTopologyInfoHelp();

    @DefaultStringValue("VMware state")
    @Key("vmwareState")
    String vmwareState();

    @DefaultStringValue("VMware managed entity state")
    @Key("vmwareStateHelp")
    String vmwareStateHelp();
}
