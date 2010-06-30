/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Aug 13: Added methods to return the minimum and maximum Datum values for a report. ayres@opennms.org
 * 2007 Apr 10: Created this file.
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Model class for a statistics report.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see StatisticsReportData
 * @version $Id: $
 */
@Entity
@Table(name="statisticsReport")
public class StatisticsReport implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer m_id;
    private Date m_startDate;
    private Date m_endDate;
    private String m_name;
    private String m_description;
    private Date m_jobStartedDate;
    private Date m_jobCompletedDate;
    private Date m_purgeDate;
    private Set<StatisticsReportData> m_data = new HashSet<StatisticsReportData>();

    
    
    /**
     * Unique identifier for report.
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
     * The beginning date for the report (data starting at this time stamp is included).
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="startDate", nullable=false)
    public Date getStartDate() {
        return m_startDate;
    }
    /**
     * <p>setStartDate</p>
     *
     * @param startDate a {@link java.util.Date} object.
     */
    public void setStartDate(Date startDate) {
        m_startDate = startDate;
    }


    /**
     * The end date for the report (data up to,
     * but not including this time stamp is included).
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="endDate", nullable=false)
    public Date getEndDate() {
        return m_endDate;
    }
    /**
     * <p>setEndDate</p>
     *
     * @param endDate a {@link java.util.Date} object.
     */
    public void setEndDate(Date endDate) {
        m_endDate = endDate;
    }

    
    /**
     * Report name this references a report definition
     * in statsd-configuration.xml.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="name", length=63, nullable=false)
    public String getName() {
        return m_name;
    }
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * User-friendly description for this report.
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="description", length=255, nullable=false)
    public String getDescription() {
        return m_description;
    }
    /**
     * <p>setDescription</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        m_description = description;
    }
    
    /**
     * The date when this report run started.
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="jobStartedDate", nullable=false)
    public Date getJobStartedDate() {
        return m_jobStartedDate;
    }
    /**
     * <p>setJobStartedDate</p>
     *
     * @param jobStartedDate a {@link java.util.Date} object.
     */
    public void setJobStartedDate(Date jobStartedDate) {
        m_jobStartedDate = jobStartedDate;
    }

    /**
     * The date when this report run completed.
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="jobCompletedDate", nullable=false)
    public Date getJobCompletedDate() {
        return m_jobCompletedDate;
    }
    /**
     * <p>setJobCompletedDate</p>
     *
     * @param jobCompletedDate a {@link java.util.Date} object.
     */
    public void setJobCompletedDate(Date jobCompletedDate) {
        m_jobCompletedDate = jobCompletedDate;
    }

    /**
     * The date at which this report can be purged
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="purgeDate", nullable=false)
    public Date getPurgeDate() {
        return m_purgeDate;
    }
    /**
     * <p>setPurgeDate</p>
     *
     * @param purgeDate a {@link java.util.Date} object.
     */
    public void setPurgeDate(Date purgeDate) {
        m_purgeDate = purgeDate;
    }
    
    /**
     * <p>getData</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @OneToMany(mappedBy="report", fetch=FetchType.LAZY)
    @org.hibernate.annotations.Cascade( {
        org.hibernate.annotations.CascadeType.ALL,
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<StatisticsReportData> getData() {
        return m_data;
    }
    /**
     * <p>setData</p>
     *
     * @param data a {@link java.util.Set} object.
     */
    public void setData(Set<StatisticsReportData> data) {
        m_data = data;
    }
    /**
     * <p>addData</p>
     *
     * @param datum a {@link org.opennms.netmgt.model.StatisticsReportData} object.
     */
    public void addData(StatisticsReportData datum) {
        m_data.add(datum);
    }

    /**
     * <p>getDuration</p>
     *
     * @return a long.
     */
    @Transient
    public long getDuration() {
        return m_endDate.getTime() - m_startDate.getTime();
    }
    
    /**
     * <p>getDurationString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    public String getDurationString() {
        return getStringForInterval(getDuration());
    }

    /**
     * <p>getJobDuration</p>
     *
     * @return a long.
     */
    @Transient
    public long getJobDuration() {
        return m_jobCompletedDate.getTime() - m_jobStartedDate.getTime();
    }
    
    /**
     * <p>getJobDurationString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Transient
    public String getJobDurationString() {
        return getStringForInterval(getJobDuration());
    }
    
    /**
     * <p>getMaxDatumValue</p>
     *
     * @return a {@link java.lang.Double} object.
     */
    @Transient
    public Double getMaxDatumValue() {
        Double mv = null;
        for ( Iterator<StatisticsReportData> it = m_data.iterator(); it.hasNext(); ) {
            Double val = it.next().getValue();
            if (mv == null) {
                 mv = val;
            }
            else if ( val > mv) {
                 mv = val;
            }
        }
        return mv;
    }
    
    /**
     * <p>getMinDatumValue</p>
     *
     * @return a {@link java.lang.Double} object.
     */
    @Transient
    public Double getMinDatumValue() {
        Double mv = null;
        for ( Iterator<StatisticsReportData> it = m_data.iterator(); it.hasNext(); ) {
            Double val = it.next().getValue();
            if (mv == null) {
                 mv = val;
            }
            else if ( val < mv) {
                 mv = val;
            }
        }
        return mv;
    }
    
    private String getStringForInterval(long interval) {
        double value = interval;
        
        value = value / 1000;
        String unit = "seconds";

        if (value < 60) {
            return value + " " + unit;
        }
        
        value = value / 60;
        unit = "minutes";

        if (value < 60) {
            return value + " " + unit;
        }

        value = value / 60;
        unit = "hours";

        if (value < 24) {
            return value + " " + unit;
        }

        value = value / 24;
        unit = "days";

        return value + " " + unit;
    }
}
