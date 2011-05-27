package org.opennms.netmgt.model.updates;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;


public class AssetRecordUpdate {
	private final BeanWrapper m_assetBean;

	private final Set<FieldUpdate<?>> m_updatedFields = new HashSet<FieldUpdate<?>>();
	
	private final FieldUpdate<String> m_building = new FieldUpdate<String>("building");
	private final FieldUpdate<String> m_city     = new FieldUpdate<String>("city");
	private final FieldUpdate<String> m_category = new FieldUpdate<String>("category");
	private final FieldUpdate<String> m_manufacturer = new FieldUpdate<String>("manufacturer");
    private final FieldUpdate<String> m_vendor = new FieldUpdate<String>("vendor");
    private final FieldUpdate<String> m_modelNumber = new FieldUpdate<String>("modelNumber");
    private final FieldUpdate<String> m_serialNumber = new FieldUpdate<String>("serialNumber");
    private final FieldUpdate<String> m_description = new FieldUpdate<String>("description");
    private final FieldUpdate<String> m_circuitId = new FieldUpdate<String>("circuitId");
    private final FieldUpdate<String> m_assetNumber = new FieldUpdate<String>("assetNumber");
    private final FieldUpdate<String> m_operatingSystem = new FieldUpdate<String>("operatingSystem");
    private final FieldUpdate<String> m_rack = new FieldUpdate<String>("rack");
    private final FieldUpdate<String> m_slot = new FieldUpdate<String>("slot");
    private final FieldUpdate<String> m_port = new FieldUpdate<String>("port");
    private final FieldUpdate<String> m_region = new FieldUpdate<String>("region");
    private final FieldUpdate<String> m_division = new FieldUpdate<String>("division");
    private final FieldUpdate<String> m_department = new FieldUpdate<String>("department");
    private final FieldUpdate<String> m_address1 = new FieldUpdate<String>("address1");
    private final FieldUpdate<String> m_address2 = new FieldUpdate<String>("address2");
    private final FieldUpdate<String> m_state = new FieldUpdate<String>("state");
    private final FieldUpdate<String> m_zip = new FieldUpdate<String>("zip");
    private final FieldUpdate<String> m_floor = new FieldUpdate<String>("floor");
    private final FieldUpdate<String> m_room = new FieldUpdate<String>("room");
    private final FieldUpdate<String> m_vendorPhone = new FieldUpdate<String>("vendorPhone");
    private final FieldUpdate<String> m_vendorFax = new FieldUpdate<String>("vendorFax");
    private final FieldUpdate<String> m_vendorAssetNumber = new FieldUpdate<String>("vendorAssetNumber");
    private final FieldUpdate<String> m_username = new FieldUpdate<String>("username");
    private final FieldUpdate<String> m_password = new FieldUpdate<String>("password");
    private final FieldUpdate<String> m_enable = new FieldUpdate<String>("enable");
    private final FieldUpdate<String> m_connection = new FieldUpdate<String>("connection");
    private final FieldUpdate<String> m_autoenable = new FieldUpdate<String>("autoenable");
    private final FieldUpdate<String> m_lastModifiedBy = new FieldUpdate<String>("lastModifiedBy");
    private final FieldUpdate<Date> m_lastModifiedDate = new FieldUpdate<Date>("lastModifiedDate");
    private final FieldUpdate<String> m_dateInstalled = new FieldUpdate<String>("dateInstalled");
    private final FieldUpdate<String> m_lease = new FieldUpdate<String>("lease");
    private final FieldUpdate<String> m_leaseExpires = new FieldUpdate<String>("leaseExpires");
    private final FieldUpdate<String> m_supportPhone = new FieldUpdate<String>("supportPhone");
    private final FieldUpdate<String> m_maintContractNumber = new FieldUpdate<String>("maintContractNumber");
    private final FieldUpdate<String> m_maintContractExpiration = new FieldUpdate<String>("maintContractExpiration");
    private final FieldUpdate<String> m_displayCategory = new FieldUpdate<String>("displayCategory");
    private final FieldUpdate<String> m_notifyCategory = new FieldUpdate<String>("notifyCategory");
    private final FieldUpdate<String> m_pollerCategory = new FieldUpdate<String>("pollerCategory");
    private final FieldUpdate<String> m_thresholdCategory = new FieldUpdate<String>("thresholdCategory");
    private final FieldUpdate<String> m_comment = new FieldUpdate<String>("comment");
    private final FieldUpdate<String> m_cpu = new FieldUpdate<String>("cpu");
    private final FieldUpdate<String> m_ram = new FieldUpdate<String>("ram");
    private final FieldUpdate<String> m_storagectrl = new FieldUpdate<String>("storagectrl");
    private final FieldUpdate<String> m_hdd1 = new FieldUpdate<String>("hdd1");
    private final FieldUpdate<String> m_hdd2 = new FieldUpdate<String>("hdd2");
    private final FieldUpdate<String> m_hdd3 = new FieldUpdate<String>("hdd3");
    private final FieldUpdate<String> m_hdd4 = new FieldUpdate<String>("hdd4");
    private final FieldUpdate<String> m_hdd5 = new FieldUpdate<String>("hdd5");
    private final FieldUpdate<String> m_hdd6 = new FieldUpdate<String>("hdd6");
    private final FieldUpdate<String> m_numpowersupplies = new FieldUpdate<String>("numpowersupplies");
    private final FieldUpdate<String> m_inputpower = new FieldUpdate<String>("inputpower");
    private final FieldUpdate<String> m_additionalhardware = new FieldUpdate<String>("additionalhardware");
    private final FieldUpdate<String> m_admin = new FieldUpdate<String>("admin");
    private final FieldUpdate<String> m_snmpcommunity = new FieldUpdate<String>("snmpcommunity");
    private final FieldUpdate<String> m_rackunitheight = new FieldUpdate<String>("rackunitheight");
    private final FieldUpdate<String> m_managedObjectType = new FieldUpdate<String>("managedObjectType");
    private final FieldUpdate<String> m_managedObjectInstance = new FieldUpdate<String>("managedObjectInstance");

	public AssetRecordUpdate() {
		m_assetBean = new BeanWrapperImpl(this);
	}
	
	public String getBuilding() {
		return m_building.get();
	}
	
	public String getCity() {
		return m_city.get();
	}

	public String getCategory() {
		return m_category.get();
	}

	public String getManufacturer() {
		return m_manufacturer.get();
	}

	public String getVendor() {
		return m_vendor.get();
	}

	public String getModelNumber() {
		return m_modelNumber.get();
	}

	public String getSerialNumber() {
		return m_serialNumber.get();
	}

	public String getDescription() {
		return m_description.get();
	}

	public String getCircuitId() {
		return m_circuitId.get();
	}

	public String getAssetNumber() {
		return m_assetNumber.get();
	}

	public String getOperatingSystem() {
		return m_operatingSystem.get();
	}

	public String getRack() {
		return m_rack.get();
	}

	public String getSlot() {
		return m_slot.get();
	}

	public String getPort() {
		return m_port.get();
	}

	public String getRegion() {
		return m_region.get();
	}

	public String getDivision() {
		return m_division.get();
	}

	public String getDepartment() {
		return m_department.get();
	}

	public String getAddress1() {
		return m_address1.get();
	}

	public String getAddress2() {
		return m_address2.get();
	}

	public String getState() {
		return m_state.get();
	}

	public String getZip() {
		return m_zip.get();
	}

	public String getFloor() {
		return m_floor.get();
	}

	public String getRoom() {
		return m_room.get();
	}

	public String getVendorPhone() {
		return m_vendorPhone.get();
	}

	public String getVendorFax() {
		return m_vendorFax.get();
	}

	public String getVendorAssetNumber() {
		return m_vendorAssetNumber.get();
	}

	public String getUsername() {
		return m_username.get();
	}

	public String getPassword() {
		return m_password.get();
	}

	public String getEnable() {
		return m_enable.get();
	}

	public String getConnection() {
		return m_connection.get();
	}

	public String getAutoenable() {
		return m_autoenable.get();
	}

	public String getLastModifiedBy() {
		return m_lastModifiedBy.get();
	}

	public Date getLastModifiedDate() {
		return m_lastModifiedDate.get();
	}

	public String getDateInstalled() {
		return m_dateInstalled.get();
	}

	public String getLease() {
		return m_lease.get();
	}

	public String getLeaseExpires() {
		return m_leaseExpires.get();
	}

	public String getSupportPhone() {
		return m_supportPhone.get();
	}

	public String getMaintContractNumber() {
		return m_maintContractNumber.get();
	}

	public String getMaintContractExpiration() {
		return m_maintContractExpiration.get();
	}

	public String getDisplayCategory() {
		return m_displayCategory.get();
	}

	public String getNotifyCategory() {
		return m_notifyCategory.get();
	}

	public String getPollerCategory() {
		return m_pollerCategory.get();
	}

	public String getThresholdCategory() {
		return m_thresholdCategory.get();
	}

	public String getComment() {
		return m_comment.get();
	}

	public String getCpu() {
		return m_cpu.get();
	}

	public String getRam() {
		return m_ram.get();
	}

	public String getStoragectrl() {
		return m_storagectrl.get();
	}

	public String getHdd1() {
		return m_hdd1.get();
	}

	public String getHdd2() {
		return m_hdd2.get();
	}

	public String getHdd3() {
		return m_hdd3.get();
	}

	public String getHdd4() {
		return m_hdd4.get();
	}

	public String getHdd5() {
		return m_hdd5.get();
	}

	public String getHdd6() {
		return m_hdd6.get();
	}

	public String getNumpowersupplies() {
		return m_numpowersupplies.get();
	}

	public String getInputpower() {
		return m_inputpower.get();
	}

	public String getAdditionalhardware() {
		return m_additionalhardware.get();
	}

	public String getAdmin() {
		return m_admin.get();
	}

	public String getSnmpcommunity() {
		return m_snmpcommunity.get();
	}

	public String getRackunitheight() {
		return m_rackunitheight.get();
	}

	public String getManagedObjectType() {
		return m_managedObjectType.get();
	}

	public String getManagedObjectInstance() {
		return m_managedObjectInstance.get();
	}

	public AssetRecordUpdate setBuilding(final String building) {
		m_updatedFields.add(m_building);
        m_building.set(building);
		return this;
	}

	public AssetRecordUpdate setCity(final String city) {
		m_updatedFields.add(m_city);
        m_updatedFields.add(m_city);
        m_city.set(city);
		return this;
	}

	public AssetRecordUpdate setCategory(String category) {
		m_updatedFields.add(m_category);
        m_category.set(category);
		return this;
	}

	public AssetRecordUpdate setManufacturer(String manufacturer) {
		m_updatedFields.add(m_manufacturer);
        m_manufacturer.set(manufacturer);
		return this;
	}

	public AssetRecordUpdate setVendor(String vendor) {
		m_updatedFields.add(m_vendor);
        m_vendor.set(vendor);
		return this;
	}

	public AssetRecordUpdate setModelNumber(String modelNumber) {
		m_updatedFields.add(m_modelNumber);
        m_modelNumber.set(modelNumber);
		return this;
	}

	public AssetRecordUpdate setSerialNumber(String serialNumber) {
		m_updatedFields.add(m_serialNumber);
        m_serialNumber.set(serialNumber);
		return this;
	}

	public AssetRecordUpdate setDescription(String description) {
		m_updatedFields.add(m_description);
        m_description.set(description);
		return this;
	}

	public AssetRecordUpdate setCircuitId(String circuitId) {
		m_updatedFields.add(m_circuitId);
        m_circuitId.set(circuitId);
		return this;
	}

	public AssetRecordUpdate setAssetNumber(String assetNumber) {
		m_updatedFields.add(m_assetNumber);
        m_assetNumber.set(assetNumber);
		return this;
	}

	public AssetRecordUpdate setOperatingSystem(String operatingSystem) {
		m_updatedFields.add(m_operatingSystem);
        m_operatingSystem.set(operatingSystem);
		return this;
	}

	public AssetRecordUpdate setRack(String rack) {
		m_updatedFields.add(m_rack);
        m_rack.set(rack);
		return this;
	}

	public AssetRecordUpdate setSlot(String slot) {
		m_updatedFields.add(m_slot);
        m_slot.set(slot);
		return this;
	}

	public AssetRecordUpdate setPort(String port) {
		m_updatedFields.add(m_port);
        m_port.set(port);
		return this;
	}

	public AssetRecordUpdate setRegion(String region) {
		m_updatedFields.add(m_region);
        m_region.set(region);
		return this;
	}

	public AssetRecordUpdate setDivision(String division) {
		m_updatedFields.add(m_division);
        m_division.set(division);
		return this;
	}

	public AssetRecordUpdate setDepartment(String department) {
		m_updatedFields.add(m_department);
        m_department.set(department);
		return this;
	}

	public AssetRecordUpdate setAddress1(String address1) {
		m_updatedFields.add(m_address1);
        m_address1.set(address1);
		return this;
	}

	public AssetRecordUpdate setAddress2(String address2) {
		m_updatedFields.add(m_address2);
        m_address2.set(address2);
		return this;
	}

	public AssetRecordUpdate setState(String state) {
		m_updatedFields.add(m_state);
        m_state.set(state);
		return this;
	}

	public AssetRecordUpdate setZip(String zip) {
		m_updatedFields.add(m_zip);
        m_zip.set(zip);
		return this;
	}

	public AssetRecordUpdate setFloor(String floor) {
		m_updatedFields.add(m_floor);
        m_floor.set(floor);
		return this;
	}

	public AssetRecordUpdate setRoom(String room) {
		m_updatedFields.add(m_room);
        m_room.set(room);
		return this;
	}

	public AssetRecordUpdate setVendorPhone(String vendorPhone) {
		m_updatedFields.add(m_vendorPhone);
        m_vendorPhone.set(vendorPhone);
		return this;
	}

	public AssetRecordUpdate setVendorFax(String vendorFax) {
		m_updatedFields.add(m_vendorFax);
        m_vendorFax.set(vendorFax);
		return this;
	}

	public AssetRecordUpdate setVendorAssetNumber(String vendorAssetNumber) {
		m_updatedFields.add(m_vendorAssetNumber);
        m_vendorAssetNumber.set(vendorAssetNumber);
		return this;
	}

	public AssetRecordUpdate setUsername(String username) {
		m_updatedFields.add(m_username);
        m_username.set(username);
		return this;
	}

	public AssetRecordUpdate setPassword(String password) {
		m_updatedFields.add(m_password);
        m_password.set(password);
		return this;
	}

	public AssetRecordUpdate setEnable(String enable) {
		m_updatedFields.add(m_enable);
        m_enable.set(enable);
		return this;
	}

	public AssetRecordUpdate setConnection(String connection) {
		m_updatedFields.add(m_connection);
        m_connection.set(connection);
		return this;
	}

	public AssetRecordUpdate setAutoenable(String autoenable) {
		m_updatedFields.add(m_autoenable);
        m_autoenable.set(autoenable);
		return this;
	}

	public AssetRecordUpdate setLastModifiedBy(String lastModifiedBy) {
		m_updatedFields.add(m_lastModifiedBy);
        m_lastModifiedBy.set(lastModifiedBy);
		return this;
	}

	public AssetRecordUpdate setLastModifiedDate(Date lastModifiedDate) {
		m_updatedFields.add(m_lastModifiedDate);
        m_lastModifiedDate.set(lastModifiedDate);
		return this;
	}

	public AssetRecordUpdate setDateInstalled(String dateInstalled) {
		m_updatedFields.add(m_dateInstalled);
        m_dateInstalled.set(dateInstalled);
		return this;
	}

	public AssetRecordUpdate setLease(String lease) {
		m_updatedFields.add(m_lease);
        m_lease.set(lease);
		return this;
	}

	public AssetRecordUpdate setLeaseExpires(String leaseExpires) {
		m_updatedFields.add(m_leaseExpires);
        m_leaseExpires.set(leaseExpires);
		return this;
	}

	public AssetRecordUpdate setSupportPhone(String supportPhone) {
		m_updatedFields.add(m_supportPhone);
        m_supportPhone.set(supportPhone);
		return this;
	}

	public AssetRecordUpdate setMaintContractNumber(String maintContractNumber) {
		m_updatedFields.add(m_maintContractNumber);
        m_maintContractNumber.set(maintContractNumber);
		return this;
	}

	public AssetRecordUpdate setMaintContractExpiration(
			String maintContractExpiration) {
		m_updatedFields.add(m_maintContractExpiration);
        m_maintContractExpiration.set(maintContractExpiration);
		return this;
	}

	public AssetRecordUpdate setDisplayCategory(String displayCategory) {
		m_updatedFields.add(m_displayCategory);
        m_displayCategory.set(displayCategory);
		return this;
	}

	public AssetRecordUpdate setNotifyCategory(String notifyCategory) {
		m_updatedFields.add(m_notifyCategory);
        m_notifyCategory.set(notifyCategory);
		return this;
	}

	public AssetRecordUpdate setPollerCategory(String pollerCategory) {
		m_updatedFields.add(m_pollerCategory);
        m_pollerCategory.set(pollerCategory);
		return this;
	}

	public AssetRecordUpdate setThresholdCategory(String thresholdCategory) {
		m_updatedFields.add(m_thresholdCategory);
        m_thresholdCategory.set(thresholdCategory);
		return this;
	}

	public AssetRecordUpdate setComment(String comment) {
		m_updatedFields.add(m_comment);
        m_comment.set(comment);
		return this;
	}

	public AssetRecordUpdate setCpu(String cpu) {
		m_updatedFields.add(m_cpu);
        m_cpu.set(cpu);
		return this;
	}

	public AssetRecordUpdate setRam(String ram) {
		m_updatedFields.add(m_ram);
        m_ram.set(ram);
		return this;
	}

	public AssetRecordUpdate setStoragectrl(String storagectrl) {
		m_updatedFields.add(m_storagectrl);
        m_storagectrl.set(storagectrl);
		return this;
	}

	public AssetRecordUpdate setHdd1(String hdd1) {
		m_updatedFields.add(m_hdd1);
        m_hdd1.set(hdd1);
		return this;
	}

	public AssetRecordUpdate setHdd2(String hdd2) {
		m_updatedFields.add(m_hdd2);
        m_hdd2.set(hdd2);
		return this;
	}

	public AssetRecordUpdate setHdd3(String hdd3) {
		m_updatedFields.add(m_hdd3);
        m_hdd3.set(hdd3);
		return this;
	}

	public AssetRecordUpdate setHdd4(String hdd4) {
		m_updatedFields.add(m_hdd4);
        m_hdd4.set(hdd4);
		return this;
	}

	public AssetRecordUpdate setHdd5(String hdd5) {
		m_updatedFields.add(m_hdd5);
        m_hdd5.set(hdd5);
		return this;
	}

	public AssetRecordUpdate setHdd6(String hdd6) {
		m_updatedFields.add(m_hdd6);
        m_hdd6.set(hdd6);
		return this;
	}

	public AssetRecordUpdate setNumpowersupplies(String numpowersupplies) {
		m_updatedFields.add(m_numpowersupplies);
        m_numpowersupplies.set(numpowersupplies);
		return this;
	}

	public AssetRecordUpdate setInputpower(String inputpower) {
		m_updatedFields.add(m_inputpower);
        m_inputpower.set(inputpower);
		return this;
	}

	public AssetRecordUpdate setAdditionalhardware(String additionalhardware) {
		m_updatedFields.add(m_additionalhardware);
        m_additionalhardware.set(additionalhardware);
		return this;
	}

	public AssetRecordUpdate setAdmin(String admin) {
		m_updatedFields.add(m_admin);
        m_admin.set(admin);
		return this;
	}

	public AssetRecordUpdate setSnmpcommunity(String snmpcommunity) {
		m_updatedFields.add(m_snmpcommunity);
        m_snmpcommunity.set(snmpcommunity);
		return this;
	}

	public AssetRecordUpdate setRackunitheight(String rackunitheight) {
		m_updatedFields.add(m_rackunitheight);
        m_rackunitheight.set(rackunitheight);
		return this;
	}

	public AssetRecordUpdate setManagedObjectType(String managedObjectType) {
		m_updatedFields.add(m_managedObjectType);
        m_managedObjectType.set(managedObjectType);
		return this;
	}

	public AssetRecordUpdate setManagedObjectInstance(String managedObjectInstance) {
		m_updatedFields.add(m_managedObjectInstance);
        m_managedObjectInstance.set(managedObjectInstance);
		return this;
	}

	public AssetRecordUpdate setAssetAttribute(final String name, final String value) {
		try {
			m_assetBean.setPropertyValue(name, value);
		} catch (final BeansException e) {
        	LogUtils.warnf(this, e, "Could not set property '%v' on asset '%v'", value, name);
		}
		return this;
	}

	public OnmsAssetRecord apply(final OnmsAssetRecord assetRecord) {
		for (final FieldUpdate<?> field : m_updatedFields) {
			field.apply(assetRecord);
		}
		return assetRecord;
	}

}
