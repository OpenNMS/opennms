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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Model class for a piece of statistics report data.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see StatisticsReport
 * @version $Id: $
 */
@Entity
@Table(name="statisticsReportData")
public class StatisticsReportData implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -6112853375515080125L;
    private Integer m_id;
    private StatisticsReport m_report;
    private ResourceReference m_resource;
    private Double m_value;
    
    /**
     * Unique identifier for data.
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(name="id")
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
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
     * <p>getReport</p>
     *
     * @return a {@link org.opennms.netmgt.model.StatisticsReport} object.
     */
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="reportId") //, nullable=false)
    public StatisticsReport getReport() {
        return m_report;
    }
    /**
     * <p>setReport</p>
     *
     * @param report a {@link org.opennms.netmgt.model.StatisticsReport} object.
     */
    public void setReport(StatisticsReport report) {
        m_report = report;
    }
    
    /**
     * <p>getResource</p>
     *
     * @return a {@link org.opennms.netmgt.model.ResourceReference} object.
     */
    @ManyToOne(optional=false)
    @JoinColumn(name="resourceId")
    public ResourceReference getResource() {
        return m_resource;
    }
    /**
     * <p>setResource</p>
     *
     * @param resource a {@link org.opennms.netmgt.model.ResourceReference} object.
     */
    public void setResource(ResourceReference resource) {
        m_resource = resource;
    }
    
    /**
     * <p>getResourceId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    public String getResourceId() {
        return m_resource.getResourceId();
    }
    
    /**
     * <p>getValue</p>
     *
     * @return a {@link java.lang.Double} object.
     */
    @Column(name="value", nullable=false)
    public Double getValue() {
        return m_value;
    }
    
    /**
     * <p>setValue</p>
     *
     * @param value a {@link java.lang.Double} object.
     */
    public void setValue(Double value) {
        m_value = value;
    }
    
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this);
        tsb.append("report", getReport().getName());
        tsb.append("resourceId", getResourceId());
        tsb.append("value", getValue());
        return tsb.toString();
    }
    
}
