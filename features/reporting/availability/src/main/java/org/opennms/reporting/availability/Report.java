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
 * The top-level element for availability
 *  reports.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
public class Report implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "logo")
    private String logo;

    @XmlElement(name = "created")
    private Created created;

    @XmlElement(name = "author")
    private String author;

    /**
     * element name="viewInfo"
     */
    @XmlElement(name = "viewInfo")
    private ViewInfo viewInfo;

    @XmlElement(name = "categories")
    private Categories categories;

    @XmlElement(name = "catCount")
    private Integer catCount;

    @XmlElement(name = "sectionCount")
    private Integer sectionCount;

    public Report() {
    }

    /**
     */
    public void deleteCatCount() {
        this.catCount= null;
    }

    /**
     */
    public void deleteSectionCount() {
        this.sectionCount= null;
    }

    /**
     * Returns the value of field 'author'.
     * 
     * @return the value of field 'Author'.
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Returns the value of field 'catCount'.
     * 
     * @return the value of field 'CatCount'.
     */
    public Integer getCatCount() {
        return this.catCount;
    }

    /**
     * Returns the value of field 'categories'.
     * 
     * @return the value of field 'Categories'.
     */
    public Categories getCategories() {
        return this.categories;
    }

    /**
     * Returns the value of field 'created'.
     * 
     * @return the value of field 'Created'.
     */
    public Created getCreated() {
        return this.created;
    }

    /**
     * Returns the value of field 'logo'.
     * 
     * @return the value of field 'Logo'.
     */
    public String getLogo() {
        return this.logo;
    }

    /**
     * Returns the value of field 'sectionCount'.
     * 
     * @return the value of field 'SectionCount'.
     */
    public Integer getSectionCount() {
        return this.sectionCount;
    }

    /**
     * Returns the value of field 'viewInfo'. The field 'viewInfo' has the
     * following description: element name="viewInfo"
     * 
     * @return the value of field 'ViewInfo'.
     */
    public ViewInfo getViewInfo() {
        return this.viewInfo;
    }

    /**
     * Method hasCatCount.
     * 
     * @return true if at least one CatCount has been added
     */
    public boolean hasCatCount() {
        return this.catCount != null;
    }

    /**
     * Method hasSectionCount.
     * 
     * @return true if at least one SectionCount has been added
     */
    public boolean hasSectionCount() {
        return this.sectionCount != null;
    }

    /**
     * Sets the value of field 'author'.
     * 
     * @param author the value of field 'author'.
     */
    public void setAuthor(final String author) {
        this.author = author;
    }

    /**
     * Sets the value of field 'catCount'.
     * 
     * @param catCount the value of field 'catCount'.
     */
    public void setCatCount(final Integer catCount) {
        this.catCount = catCount;
    }

    /**
     * Sets the value of field 'categories'.
     * 
     * @param categories the value of field 'categories'.
     */
    public void setCategories(final Categories categories) {
        this.categories = categories;
    }

    /**
     * Sets the value of field 'created'.
     * 
     * @param created the value of field 'created'.
     */
    public void setCreated(final Created created) {
        this.created = created;
    }

    /**
     * Sets the value of field 'logo'.
     * 
     * @param logo the value of field 'logo'.
     */
    public void setLogo(final String logo) {
        this.logo = logo;
    }

    /**
     * Sets the value of field 'sectionCount'.
     * 
     * @param sectionCount the value of field 'sectionCount'.
     */
    public void setSectionCount(final Integer sectionCount) {
        this.sectionCount = sectionCount;
    }

    /**
     * Sets the value of field 'viewInfo'. The field 'viewInfo' has the following
     * description: element name="viewInfo"
     * 
     * @param viewInfo the value of field 'viewInfo'.
     */
    public void setViewInfo(final ViewInfo viewInfo) {
        this.viewInfo = viewInfo;
    }

}
