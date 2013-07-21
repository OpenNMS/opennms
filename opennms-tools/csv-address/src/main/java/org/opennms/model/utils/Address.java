package org.opennms.model.utils;

public class Address {

	private String m_foreignSource;
	private String m_address;
	private String m_city;
	private String m_state;
	private String m_zip;
	private String m_country;
	private String m_department;
	private String m_division;
	private String m_region;

	public Address(String[] fields) {
		
		setForeignSource(fields[0].trim());
		setAddress(fields[1].trim());
		setCity(fields[2].trim());
		setState(fields[3].trim());
		setZip(fields[4].trim());
		setCountry(fields[5].trim());
		setDepartment(fields[6].trim());
		setDistrict(fields[7].trim());
		setRegion(fields[8].trim());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("foreignSource:");
		sb.append(getForeignSource());
		sb.append(", ");
		
		sb.append("Address:");
		sb.append(getAddress());
		sb.append(", ");
		
		sb.append("City:");
		sb.append(getCity());
		sb.append(", ");
		
		sb.append("State:");
		sb.append(getState());
		sb.append(", ");
		
		sb.append("Zip");
		sb.append(getZip());
		sb.append(", ");
		
		sb.append("Country");
		sb.append(getCountry());
		sb.append(", ");
		
		sb.append("Department");
		sb.append(getDepartment());
		sb.append(", ");
		
		sb.append("District:");
		sb.append(getDivision());
		sb.append(", ");
		
		sb.append("Region:");
		sb.append(getRegion());
		
		return sb.toString();
	}

	public String getForeignSource() {
		return m_foreignSource;
	}

	public void setForeignSource(String foreignSource) {
		m_foreignSource = foreignSource;
	}

	public String getAddress() {
		return m_address;
	}

	public void setAddress(String address) {
		m_address = address;
	}

	public String getCity() {
		return m_city;
	}

	public void setCity(String city) {
		m_city = city;
	}

	public String getState() {
		return m_state;
	}

	public void setState(String state) {
		m_state = state;
	}

	public String getZip() {
		return m_zip;
	}

	public void setZip(String zip) {
		m_zip = zip;
	}

	public String getCountry() {
		return m_country;
	}

	public void setCountry(String country) {
		m_country = country;
	}

	public String getDepartment() {
		return m_department;
	}

	public void setDepartment(String department) {
		m_department = department;
	}

	public String getDivision() {
		return m_division;
	}

	public void setDistrict(String division) {
		m_division = division;
	}

	public String getRegion() {
		return m_region;
	}

	public void setRegion(String region) {
		m_region = region;
	}

}
