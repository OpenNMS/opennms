package org.opennms.netmgt.model;

import java.util.Date;


public class AvailabilityReportLocator {

	private Integer m_id;
	
	/** type of report (HTML/SVG/PDF */
	
	private String m_type;
	
	/** format (calendar or classic) */
	
	private String m_format;
	
	/** date report generated */
	
	private Date m_date;
	
	/** location on disk */
	
	private String m_location;
	
	/** Name of the category for report (not the object) */
	
	private String m_category;
	
	/** has the report been run yet? */
	
	private Boolean m_available;
	
	//* getters and setters */
	
	public String getLocation() {
		return m_location;
	}

	public void setLocation(String location) {
		m_location = location;
	}

	public String getType() {
		return m_type;
	}
	
	public void setType(String type) {
		m_type = type;
	}
	
	public String getFormat() {
		return m_format;
	}
	
	public void setFormat(String format) {
		m_format = format;
	}
	
	public Date getDate() {
		return m_date;
	}
	
	public void setDate(Date date) {
		m_date = date;
	}
	
	public String getCategory() {
		return m_category;
	}
	
	public void setCategory(String category) {
		m_category = category;
	}

	public Integer getId() {
		return m_id;
	}

	public void setId(Integer id) {
		m_id = id;
	}
	
	public Boolean getAvailable() {
		return m_available;
	}
	
	public void setAvailable(Boolean available) {
		m_available = available;
	}
	
	
	
}
