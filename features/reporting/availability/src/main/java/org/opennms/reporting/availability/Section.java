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
package org.opennms.reporting.availability;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Section.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "section")
@XmlAccessorType(XmlAccessType.FIELD)
public class Section implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "sectionName")
    private String sectionName;

    @XmlElement(name = "sectionTitle")
    private String sectionTitle;

    @XmlElement(name = "sectionDescr")
    private String sectionDescr;

    @XmlElement(name = "period")
    private String period;

    @XmlElement(name = "classicTable")
    private ClassicTable classicTable;

    @XmlElement(name = "calendarTable")
    private CalendarTable calendarTable;

    @XmlElement(name = "sectionIndex")
    private Integer sectionIndex;

    public Section() {
    }

    /**
     */
    public void deleteSectionIndex() {
        this.sectionIndex= null;
    }

    /**
     * Returns the value of field 'calendarTable'.
     * 
     * @return the value of field 'CalendarTable'.
     */
    public CalendarTable getCalendarTable() {
        return this.calendarTable;
    }

    /**
     * Returns the value of field 'classicTable'.
     * 
     * @return the value of field 'ClassicTable'.
     */
    public ClassicTable getClassicTable() {
        return this.classicTable;
    }

    /**
     * Returns the value of field 'period'.
     * 
     * @return the value of field 'Period'.
     */
    public String getPeriod() {
        return this.period;
    }

    /**
     * Returns the value of field 'sectionDescr'.
     * 
     * @return the value of field 'SectionDescr'.
     */
    public String getSectionDescr() {
        return this.sectionDescr;
    }

    /**
     * Returns the value of field 'sectionIndex'.
     * 
     * @return the value of field 'SectionIndex'.
     */
    public Integer getSectionIndex() {
        return this.sectionIndex;
    }

    /**
     * Returns the value of field 'sectionName'.
     * 
     * @return the value of field 'SectionName'.
     */
    public String getSectionName() {
        return this.sectionName;
    }

    /**
     * Returns the value of field 'sectionTitle'.
     * 
     * @return the value of field 'SectionTitle'.
     */
    public String getSectionTitle() {
        return this.sectionTitle;
    }

    /**
     * Method hasSectionIndex.
     * 
     * @return true if at least one SectionIndex has been added
     */
    public boolean hasSectionIndex() {
        return this.sectionIndex != null;
    }

    /**
     * Sets the value of field 'calendarTable'.
     * 
     * @param calendarTable the value of field 'calendarTable'.
     */
    public void setCalendarTable(final CalendarTable calendarTable) {
        this.calendarTable = calendarTable;
    }

    /**
     * Sets the value of field 'classicTable'.
     * 
     * @param classicTable the value of field 'classicTable'.
     */
    public void setClassicTable(final ClassicTable classicTable) {
        this.classicTable = classicTable;
    }

    /**
     * Sets the value of field 'period'.
     * 
     * @param period the value of field 'period'.
     */
    public void setPeriod(final String period) {
        this.period = period;
    }

    /**
     * Sets the value of field 'sectionDescr'.
     * 
     * @param sectionDescr the value of field 'sectionDescr'.
     */
    public void setSectionDescr(final String sectionDescr) {
        this.sectionDescr = sectionDescr;
    }

    /**
     * Sets the value of field 'sectionIndex'.
     * 
     * @param sectionIndex the value of field 'sectionIndex'.
     */
    public void setSectionIndex(final Integer sectionIndex) {
        this.sectionIndex = sectionIndex;
    }

    /**
     * Sets the value of field 'sectionName'.
     * 
     * @param sectionName the value of field 'sectionName'.
     */
    public void setSectionName(final String sectionName) {
        this.sectionName = sectionName;
    }

    /**
     * Sets the value of field 'sectionTitle'.
     * 
     * @param sectionTitle the value of field 'sectionTitle'.
     */
    public void setSectionTitle(final String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

}
