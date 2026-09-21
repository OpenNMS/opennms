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
    @SequenceGenerator(name="reportCatalogSequence", sequenceName="reportCatalogNxtId", allocationSize = 1)
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
