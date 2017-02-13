/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonManagedReference;
import org.hibernate.annotations.Formula;

/**
 * @author Seth
 */
@Entity()
@Table(name="scanReports")
@XmlRootElement(name="scan-report")
@XmlAccessorType(XmlAccessType.NONE)
public class ScanReport implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String PROPERY_APPLICATIONS = "applications";

    @XmlID
    @XmlAttribute(name="id", required=true)
    private String m_id = UUID.randomUUID().toString();

    @XmlAttribute(name="location", required=true)
    private String m_location;

    @XmlAttribute(name="locale")
    private String m_locale;

    @XmlAttribute(name="timestamp")
    private Date m_timestamp;

    @XmlElementWrapper(name="properties")
    private Map<String,String> m_properties = new LinkedHashMap<String,String>();

    @XmlElementWrapper(name="poll-results")
    @XmlElement(name="poll-result")
    @JsonManagedReference
    private List<ScanReportPollResult> m_scanReportPollResults = new ArrayList<>();

    @XmlTransient
    @JsonIgnore
    private ScanReportLog m_logs;

    public ScanReport() {
    }

    /**
     * Copy constructor.
     *
     * @param pkg
     */
    public ScanReport(final ScanReport pkg) {
        m_id = pkg.getId();
        m_locale = pkg.getLocale();
        m_location = pkg.getLocation();
        m_timestamp = pkg.getTimestamp();
        m_properties = pkg.getProperties();
        m_scanReportPollResults = pkg.getPollResults();
        m_logs = pkg.getLog();
    }

    @Id
    @Column(name="id", unique=true)
    public String getId() {
        return m_id;
    }

    public void setId(final String id) {
        m_id = id;
    }

    @ElementCollection
    @JoinTable(name="scanReportProperties", joinColumns = @JoinColumn(name="scanReportId"))
    @MapKeyColumn(name="property", nullable=false)
    @Column(name="propertyValue")
    public Map<String,String> getProperties() {
        return m_properties;
    }

    public void setProperties(final Map<String,String> properties) {
        m_properties = properties;
    }

    public void addProperty(final String name, final String value) {
        m_properties.put(name, value);
    }

    public void addProperty(final Map.Entry<String,String> entry) {
        m_properties.put(entry.getKey(), entry.getValue());
    }

    @Transient
    public String getProperty(final String name) {
        return m_properties.get(name);
    }

    /**
     * This is a transient bean property that is used so that CXF can 
     * perform FIQL searches on the bean property. Use coalesce() so that
     * a 'null' value is returned if there is no property with the specified
     * name.
     */
    @Formula("(select coalesce((select p.propertyvalue from scanreportproperties p where p.scanreportid = id and p.property = '" + PROPERY_APPLICATIONS + "')))")
    @XmlTransient
    @JsonIgnore
    public String getApplications() {
        return getProperty(PROPERY_APPLICATIONS);
    }

    public void setApplications(String applications) {
        addProperty(PROPERY_APPLICATIONS, applications);
    }

    public String getLocation() {
        return m_location;
    }

    public void setLocation(final String location) {
        m_location = location;
    }

    public String getLocale() {
        return m_locale;
    }

    public void setLocale(String m_locale) {
        this.m_locale = m_locale;
    }

    public Date getTimestamp() {
        return m_timestamp;
    }

    public void setTimestamp(Date m_timestamp) {
        this.m_timestamp = m_timestamp;
    }

    @OneToMany(mappedBy="scanReport",orphanRemoval=true, cascade = CascadeType.ALL)
    public List<ScanReportPollResult> getPollResults() {
        return m_scanReportPollResults;
    }

    public void setPollResults(final List<ScanReportPollResult> scanReportPollResults) {
        this.m_scanReportPollResults = scanReportPollResults;
    }

    public boolean addPollResult(final ScanReportPollResult scanReportPollResult) {
        scanReportPollResult.setScanReport(this);
        return m_scanReportPollResults.add(scanReportPollResult);
    }

    @OneToOne(orphanRemoval=true, cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    @PrimaryKeyJoinColumn
    public ScanReportLog getLog() {
        return m_logs;
    }

    public void setLog(final ScanReportLog logs) {
        m_logs = logs;
    }

    @Transient
    public boolean isUp() {
        if (m_scanReportPollResults != null) {
            for (final ScanReportPollResult result : m_scanReportPollResults) {
                if (!result.isUp()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Transient
    public boolean isAvailable() {
        if (m_scanReportPollResults != null) {
            for (final ScanReportPollResult result : m_scanReportPollResults) {
                if (!result.isAvailable()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScanReport [id=" + m_id + ", location=" + m_location + ", locale=" + m_locale + ", timestamp=" + m_timestamp + ", properties="
                + m_properties + ", pollResults=" + m_scanReportPollResults + "]";
    }
}
