/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
