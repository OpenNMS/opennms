/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
@Table(name="reportCatalog")

/**
 * ReportStoreCatalog contains details of reports that have already been run
 *
 *  @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 */
public class ReportCatalogEntry implements Serializable {

    private static final long serialVersionUID = -5351014623584691820L;
    
    private Integer m_id;
    
    private String m_reportId;
    
    private String m_title;
    
    private Date m_date;
    
    private String m_location;

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(name="id")
    @SequenceGenerator(name="reportCatalogSequence", sequenceName="reportCatalogNxtId")
    @GeneratedValue(generator="reportCatalogSequence")
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
     * <p>getReportId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="reportId", length=256)
    public String getReportId() {
        return m_reportId;
    }

    /**
     * <p>setReportId</p>
     *
     * @param reportId a {@link java.lang.String} object.
     */
    public void setReportId(String reportId) {
        m_reportId = reportId;
    }

    /**
     * <p>getTitle</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="title", length=256)
    public String getTitle() {
        return m_title;
    }

    /**
     * <p>setTitle</p>
     *
     * @param title a {@link java.lang.String} object.
     */
    public void setTitle(String title) {
        m_title = title;
    }

    /**
     * <p>getDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="date", nullable=false)
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
     * <p>getLocation</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="location", length=256)
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

}
