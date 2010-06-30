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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="reportLocator")

/**
 * AvailabilityReportLocator contains details of a pre-run availability report
 *
 *  @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 */
public class AvailabilityReportLocator implements Serializable {
	
	private static final long serialVersionUID = 4674675490758669590L;

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
	

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(name="reportId")
    @SequenceGenerator(name="reportSequence", sequenceName="reportNxtId")
    @GeneratedValue(generator="reportSequence")
	public Integer getId() {
		return m_id;
	}

	/**
	 * <p>setId</p>
	 *
	 * @param id a {@link java.lang.Integer} object.
	 */
	public void setId(Integer id) {
		m_id = id;
	}
	
	/**
	 * <p>getCategory</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Column(name="reportCategory", length=256)
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
     * <p>getDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="reportDate", nullable=false)
    	public Date getDate() {
		return m_date;
	}
	
	/**
	 * <p>setDate</p>
	 *
	 * @param date a {@link java.util.Date} object.
	 */
	public void setDate(Date date) {
		m_date = date;
	}
	
	/**
	 * <p>getFormat</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Column(name="reportFormat", length=256)
	public String getFormat() {
		return m_format;
	}
	
	/**
	 * <p>setFormat</p>
	 *
	 * @param format a {@link java.lang.String} object.
	 */
	public void setFormat(String format) {
		m_format = format;
	}

	/**
	 * <p>getType</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Column(name="reportType", length=256)
	public String getType() {
		return m_type;
	}
	
	/**
	 * <p>setType</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 */
	public void setType(String type) {
		m_type = type;
	}

	/**
	 * <p>getLocation</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Column(name="reportLocation", length=256)
	public String getLocation() {
		return m_location;
	}

	/**
	 * <p>setLocation</p>
	 *
	 * @param location a {@link java.lang.String} object.
	 */
	public void setLocation(String location) {
		m_location = location;
	}

	
	/**
	 * <p>getAvailable</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@Column(name="reportAvailable")
	public Boolean getAvailable() {
		return m_available;
	}
	
	/**
	 * <p>setAvailable</p>
	 *
	 * @param available a {@link java.lang.Boolean} object.
	 */
	public void setAvailable(Boolean available) {
		m_available = available;
	}
	
	
	
}
