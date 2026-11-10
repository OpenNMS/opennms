/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId", allocationSize = 1)
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
