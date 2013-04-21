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

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.gwt.web.ui.asset.client.tools.fieldsets.FieldSetSuggestBox;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a> Command object
 *         to transfer suggestions for {@link FieldSetSuggestBox}es to ui.
 *         Contains ordered treesets for each suggestion list.
 */
public class AssetSuggCommand implements IsSerializable {

    private Set<String> m_additionalhardware;

    private Set<String> m_address1;

    private Set<String> m_address2;

    private Set<String> m_admin;

    private Set<String> m_building;

    private Set<String> m_category;

    private Set<String> m_circuitId;

    private Set<String> m_city;

    private Set<String> m_country;

    private Set<String> m_cpu;

    private Set<String> m_department;

    private Set<String> m_description;

    private Set<String> m_displayCategory;

    private Set<String> m_division;

    private Set<String> m_floor;

    private Set<String> m_hdd1;

    private Set<String> m_hdd2;

    private Set<String> m_hdd3;

    private Set<String> m_hdd4;

    private Set<String> m_hdd5;

    private Set<String> m_hdd6;

    private Set<String> m_inputpower;

    private Set<String> m_lease;

    private Set<String> m_maintcontract;

    private Set<String> m_manufacturer;

    private Set<String> m_modelNumber;

    private Set<String> m_notifyCategory;

    private Set<String> m_numpowersupplies;

    private Set<String> m_operatingSystem;

    private Set<String> m_pollerCategory;

    private Set<String> m_rack;

    private Set<String> m_ram;

    private Set<String> m_region;

    private Set<String> m_room;

    private Set<String> m_snmpcommunity;

    private Set<String> m_state;

    private Set<String> m_storagectrl;

    private Set<String> m_supportPhone;

    private Set<String> m_thresholdCategory;

    private Set<String> m_vendor;

    private Set<String> m_vendorFax;

    private Set<String> m_vendorPhone;

    private Set<String> m_zip;

    /**
     * VMware managed Object ID
     */
    private Set<String> m_vmwareManagedObjectId;

    /**
     * VMware managed entity Type (virtualMachine | hostSystem)
     */
    private Set<String> m_vmwareManagedEntityType;

    /**
     * VMware management Server
     */
    private Set<String> m_vmwareManagementServer;

    /**
     * VMware topology info
     */
    private Set<String> m_vmwareTopologyInfo;

    /**
     * VMware managed entity state
     */
    private Set<String> m_vmwareState;

    public AssetSuggCommand() {
        m_additionalhardware = new TreeSet<String>();
        m_address1 = new TreeSet<String>();
        m_address2 = new TreeSet<String>();
        m_admin = new TreeSet<String>();
        m_building = new TreeSet<String>();
        m_category = new TreeSet<String>();
        m_circuitId = new TreeSet<String>();
        m_city = new TreeSet<String>();
        m_cpu = new TreeSet<String>();
        m_country = new TreeSet<String>();
        m_department = new TreeSet<String>();
        m_description = new TreeSet<String>();
        m_displayCategory = new TreeSet<String>();
        m_division = new TreeSet<String>();
        m_floor = new TreeSet<String>();
        m_hdd1 = new TreeSet<String>();
        m_hdd2 = new TreeSet<String>();
        m_hdd3 = new TreeSet<String>();
        m_hdd4 = new TreeSet<String>();
        m_hdd5 = new TreeSet<String>();
        m_hdd6 = new TreeSet<String>();
        m_inputpower = new TreeSet<String>();
        m_lease = new TreeSet<String>();
        m_maintcontract = new TreeSet<String>();
        m_manufacturer = new TreeSet<String>();
        m_modelNumber = new TreeSet<String>();
        m_notifyCategory = new TreeSet<String>();
        m_numpowersupplies = new TreeSet<String>();
        m_operatingSystem = new TreeSet<String>();
        m_pollerCategory = new TreeSet<String>();
        m_rack = new TreeSet<String>();
        m_ram = new TreeSet<String>();
        m_region = new TreeSet<String>();
        m_room = new TreeSet<String>();
        m_snmpcommunity = new TreeSet<String>();
        m_state = new TreeSet<String>();
        m_storagectrl = new TreeSet<String>();
        m_supportPhone = new TreeSet<String>();
        m_thresholdCategory = new TreeSet<String>();
        m_vendor = new TreeSet<String>();
        m_vendorFax = new TreeSet<String>();
        m_vendorPhone = new TreeSet<String>();
        m_zip = new TreeSet<String>();
        m_vmwareManagedObjectId = new TreeSet<String>();
        m_vmwareManagedEntityType = new TreeSet<String>();
        m_vmwareManagementServer = new TreeSet<String>();
        m_vmwareTopologyInfo = new TreeSet<String>();
        m_vmwareState = new TreeSet<String>();
        initUnchangedEntry();
    }

    public void addAdditionalhardware(String additionalhardware) {
        if ((additionalhardware != null) && !"".equals(additionalhardware)) {
            m_additionalhardware.add(additionalhardware);
        }
    }

    public void addAddress1(String address1) {
        if ((address1 != null) && !"".equals(address1)) {
            m_address1.add(address1);
        }
    }

    public void addAddress2(String address2) {
        if ((address2 != null) && !"".equals(address2)) {
            m_address2.add(address2);
        }
    }

    public void addAdmin(String admin) {
        if ((admin != null) && !"".equals(admin)) {
            m_admin.add(admin);
        }
    }

    public void addBuilding(String building) {
        if ((building != null) && !"".equals(building)) {
            m_building.add(building);
        }
    }

    public void addCategory(String category) {
        if ((category != null) && !"".equals(category)) {
            m_category.add(category);
        }
    }

    public void addCircuitId(String circuitId) {
        if ((circuitId != null) && !"".equals(circuitId)) {
            m_circuitId.add(circuitId);
        }
    }

    public void addCity(String city) {
        if ((city != null) && !"".equals(city)) {
            m_city.add(city);
        }
    }

    public void addCpu(String cpu) {
        if ((cpu != null) && !"".equals(cpu)) {
            m_cpu.add(cpu);
        }
    }

    public void addCountry(String country) {
        if ((country != null) && !"".equals(country)) {
            m_country.add(country);
        }
    }

    public void addDepartment(String department) {
        if ((department != null) && !"".equals(department)) {
            m_department.add(department);
        }
    }

    public void addDescription(String description) {
        if ((description != null) && !"".equals(description)) {
            m_description.add(description);
        }
    }

    public void addDisplayCategory(String displayCategory) {
        if ((displayCategory != null) && !"".equals(displayCategory)) {
            m_displayCategory.add(displayCategory);
        }
    }

    public void addDivision(String division) {
        if ((division != null) && !"".equals(division)) {
            m_division.add(division);
        }
    }

    public void addFloor(String floor) {
        if ((floor != null) && !"".equals(floor)) {
            m_floor.add(floor);
        }
    }

    public void addHdd1(String hdd1) {
        if ((hdd1 != null) && !"".equals(hdd1)) {
            m_hdd1.add(hdd1);
        }
    }

    public void addHdd2(String hdd2) {
        if ((hdd2 != null) && !"".equals(hdd2)) {
            m_hdd2.add(hdd2);
        }
    }

    public void addHdd3(String hdd3) {
        if ((hdd3 != null) && !"".equals(hdd3)) {
            m_hdd3.add(hdd3);
        }
    }

    public void addHdd4(String hdd4) {
        if ((hdd4 != null) && !"".equals(hdd4)) {
            m_hdd4.add(hdd4);
        }
    }

    public void addHdd5(String hdd5) {
        if ((hdd5 != null) && !"".equals(hdd5)) {
            m_hdd5.add(hdd5);
        }
    }

    public void addHdd6(String hdd6) {
        if ((hdd6 != null) && !"".equals(hdd6)) {
            m_hdd6.add(hdd6);
        }
    }

    public void addInputpower(String inputpower) {
        if ((inputpower != null) && !"".equals(inputpower)) {
            m_inputpower.add(inputpower);
        }
    }

    public void addLease(String lease) {
        if ((lease != null) && !"".equals(lease)) {
            m_lease.add(lease);
        }
    }

    public void addMaintcontract(String maintcontract) {
        if ((maintcontract != null) && !"".equals(maintcontract)) {
            m_maintcontract.add(maintcontract);
        }
    }

    public void addManufacturer(String manufacturer) {
        if ((manufacturer != null) && !"".equals(manufacturer)) {
            m_manufacturer.add(manufacturer);
        }
    }

    public void addModelNumber(String modelNumber) {
        if ((modelNumber != null) && !"".equals(modelNumber)) {
            m_modelNumber.add(modelNumber);
        }
    }

    public void addNotifyCategory(String notifyCategory) {
        if ((notifyCategory != null) && !"".equals(notifyCategory)) {
            m_notifyCategory.add(notifyCategory);
        }
    }

    public void addNumpowersupplies(String numpowersupplies) {
        if ((numpowersupplies != null) && !"".equals(numpowersupplies)) {
            m_numpowersupplies.add(numpowersupplies);
        }
    }

    public void addOperatingSystem(String operatingSystem) {
        if ((operatingSystem != null) && !"".equals(operatingSystem)) {
            m_operatingSystem.add(operatingSystem);
        }
    }

    public void addPollerCategory(String pollerCategory) {
        if ((pollerCategory != null) && !"".equals(pollerCategory)) {
            m_pollerCategory.add(pollerCategory);
        }
    }

    public void addRack(String rack) {
        if ((rack != null) && !"".equals(rack)) {
            m_rack.add(rack);
        }
    }

    public void addRam(String ram) {
        if ((ram != null) && !"".equals(ram)) {
            m_ram.add(ram);
        }
    }

    public void addRegion(String region) {
        if ((region != null) && !"".equals(region)) {
            m_region.add(region);
        }
    }

    public void addRoom(String room) {
        if ((room != null) && !"".equals(room)) {
            m_room.add(room);
        }
    }

    public void addSnmpcommunity(String snmpcommunity) {
        if ((snmpcommunity != null) && !"".equals(snmpcommunity)) {
            m_snmpcommunity.add(snmpcommunity);
        }
    }

    public void addState(String state) {
        if ((state != null) && !"".equals(state)) {
            m_state.add(state);
        }
    }

    public void addStoragectrl(String storagectrl) {
        if ((storagectrl != null) && !"".equals(storagectrl)) {
            m_storagectrl.add(storagectrl);
        }
    }

    public void addSupportPhone(String supportPhone) {
        if ((supportPhone != null) && !"".equals(supportPhone)) {
            m_supportPhone.add(supportPhone);
        }
    }

    public void addThresholdCategory(String thresholdCategory) {
        if ((thresholdCategory != null) && !"".equals(thresholdCategory)) {
            m_thresholdCategory.add(thresholdCategory);
        }
    }

    public void addVendor(String vendor) {
        if ((vendor != null) && !"".equals(vendor)) {
            m_vendor.add(vendor);
        }
    }

    public void addVendorFax(String vendorFax) {
        if ((vendorFax != null) && !"".equals(vendorFax)) {
            m_vendorFax.add(vendorFax);
        }
    }

    public void addVendorPhone(String vendorPhone) {
        if ((vendorPhone != null) && !"".equals(vendorPhone)) {
            m_vendorPhone.add(vendorPhone);
        }
    }

    public void addZip(String zip) {
        if ((zip != null) && !"".equals(zip)) {
            m_zip.add(zip);
        }
    }

    public void addVmwareManagedObjectId(String vmwareManagedObjectId) {
        if ((vmwareManagedObjectId != null) && !"".equals(vmwareManagedObjectId)) {
            m_vmwareManagedObjectId.add(vmwareManagedObjectId);
        }
    }

    public void addVmwareManagedEntityType(String vmwareManagedEntityType) {
        if ((vmwareManagedEntityType != null) && !"".equals(vmwareManagedEntityType)) {
            m_vmwareManagedEntityType.add(vmwareManagedEntityType);
        }
    }

    public void addVmwareManagementServer(String vmwareManagementServer) {
        if ((vmwareManagementServer != null) && !"".equals(vmwareManagementServer)) {
            m_vmwareManagementServer.add(vmwareManagementServer);
        }
    }

    public void addVmwareTopologyInfo(String vmwareTopologyInfo) {
        if ((vmwareTopologyInfo != null) && !"".equals(vmwareTopologyInfo)) {
            m_vmwareTopologyInfo.add(vmwareTopologyInfo);
        }
    }

    public void addVmwareState(String vmwareState) {
        if ((vmwareState != null) && !"".equals(vmwareState)) {
            m_vmwareState.add(vmwareState);
        }
    }

    public Collection<String> getAdditionalhardware() {
        return m_additionalhardware;
    }

    public Collection<String> getAddress1() {
        return m_address1;
    }

    public Collection<String> getAddress2() {
        return m_address2;
    }

    public Collection<String> getAdmin() {
        return m_admin;
    }

    public Collection<String> getBuilding() {
        return m_building;
    }

    public Collection<String> getCategory() {
        return m_category;
    }

    public Collection<String> getCircuitId() {
        return m_circuitId;
    }

    public Collection<String> getCity() {
        return m_city;
    }

    public Collection<String> getCountry() {
        return m_country;
    }

    public Collection<String> getCpu() {
        return m_cpu;
    }

    public Collection<String> getDepartment() {
        return m_department;
    }

    public Collection<String> getDescription() {
        return m_description;
    }

    public Collection<String> getDisplayCategory() {
        return m_displayCategory;
    }

    public Collection<String> getDivision() {
        return m_division;
    }

    public Collection<String> getFloor() {
        return m_floor;
    }

    public Collection<String> getHdd1() {
        return m_hdd1;
    }

    public Collection<String> getHdd2() {
        return m_hdd2;
    }

    public Collection<String> getHdd3() {
        return m_hdd3;
    }

    public Collection<String> getHdd4() {
        return m_hdd4;
    }

    public Collection<String> getHdd5() {
        return m_hdd5;
    }

    public Collection<String> getHdd6() {
        return m_hdd6;
    }

    public Collection<String> getInputpower() {
        return m_inputpower;
    }

    public Collection<String> getLease() {
        return m_lease;
    }

    public Collection<String> getMaintcontract() {
        return m_maintcontract;
    }

    public Collection<String> getManufacturer() {
        return m_manufacturer;
    }

    public Collection<String> getModelNumber() {
        return m_modelNumber;
    }

    public Collection<String> getNotifyCategory() {
        return m_notifyCategory;
    }

    public Collection<String> getNumpowersupplies() {
        return m_numpowersupplies;
    }

    public Collection<String> getOperatingSystem() {
        return m_operatingSystem;
    }

    public Collection<String> getPollerCategory() {
        return m_pollerCategory;
    }

    public Collection<String> getRack() {
        return m_rack;
    }

    public Collection<String> getRam() {
        return m_ram;
    }

    public Collection<String> getRegion() {
        return m_region;
    }

    public Collection<String> getRoom() {
        return m_room;
    }

    public Collection<String> getSnmpcommunity() {
        return m_snmpcommunity;
    }

    public Collection<String> getState() {
        return m_state;
    }

    public Collection<String> getStoragectrl() {
        return m_storagectrl;
    }

    public Collection<String> getSupportPhone() {
        return m_supportPhone;
    }

    public Collection<String> getThresholdCategory() {
        return m_thresholdCategory;
    }

    public Collection<String> getVendor() {
        return m_vendor;
    }

    public Collection<String> getVendorFax() {
        return m_vendorFax;
    }

    public Collection<String> getVendorPhone() {
        return m_vendorPhone;
    }

    public Collection<String> getZip() {
        return m_zip;
    }

    public Collection<String> getVmwareManagedObjectId() {
        return m_vmwareManagedObjectId;
    }

    public Collection<String> getVmwareManagedEntityType() {
        return m_vmwareManagedEntityType;
    }

    public Collection<String> getVmwareManagementServer() {
        return m_vmwareManagementServer;
    }

    public Collection<String> getVmwareTopologyInfo() {
        return m_vmwareTopologyInfo;
    }

    public Collection<String> getVmwareState() {
        return m_vmwareState;
    }

    private void initUnchangedEntry() {
        m_additionalhardware.add("");
        m_address1.add("");
        m_address2.add("");
        m_admin.add("");
        m_building.add("");
        m_category.add("");
        m_circuitId.add("");
        m_city.add("");
        m_cpu.add("");
        m_country.add("");
        m_department.add("");
        m_description.add("");
        m_displayCategory.add("");
        m_division.add("");
        m_floor.add("");
        m_hdd1.add("");
        m_hdd2.add("");
        m_hdd3.add("");
        m_hdd4.add("");
        m_hdd5.add("");
        m_hdd6.add("");
        m_inputpower.add("");
        m_lease.add("");
        m_maintcontract.add("");
        m_manufacturer.add("");
        m_modelNumber.add("");
        m_notifyCategory.add("");
        m_numpowersupplies.add("");
        m_operatingSystem.add("");
        m_pollerCategory.add("");
        m_rack.add("");
        m_ram.add("");
        m_region.add("");
        m_room.add("");
        m_snmpcommunity.add("");
        m_state.add("");
        m_storagectrl.add("");
        m_supportPhone.add("");
        m_thresholdCategory.add("");
        m_vendor.add("");
        m_vendorFax.add("");
        m_vendorPhone.add("");
        m_zip.add("");
        m_vmwareManagedObjectId.add("");
        m_vmwareManagedEntityType.add("");
        m_vmwareManagementServer.add("");
        m_vmwareTopologyInfo.add("");
        m_vmwareState.add("");
    }
}
