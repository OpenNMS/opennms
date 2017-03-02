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
 * element name="viewInfo"
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "viewInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class ViewInfo implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "viewName")
    private String viewName;

    @XmlElement(name = "viewTitle")
    private String viewTitle;

    @XmlElement(name = "viewComments")
    private String viewComments;

    public ViewInfo() {
    }

    /**
     * Returns the value of field 'viewComments'.
     * 
     * @return the value of field 'ViewComments'.
     */
    public String getViewComments() {
        return this.viewComments;
    }

    /**
     * Returns the value of field 'viewName'.
     * 
     * @return the value of field 'ViewName'.
     */
    public String getViewName() {
        return this.viewName;
    }

    /**
     * Returns the value of field 'viewTitle'.
     * 
     * @return the value of field 'ViewTitle'.
     */
    public String getViewTitle() {
        return this.viewTitle;
    }

    /**
     * Sets the value of field 'viewComments'.
     * 
     * @param viewComments the value of field 'viewComments'.
     */
    public void setViewComments(final String viewComments) {
        this.viewComments = viewComments;
    }

    /**
     * Sets the value of field 'viewName'.
     * 
     * @param viewName the value of field 'viewName'.
     */
    public void setViewName(final String viewName) {
        this.viewName = viewName;
    }

    /**
     * Sets the value of field 'viewTitle'.
     * 
     * @param viewTitle the value of field 'viewTitle'.
     */
    public void setViewTitle(final String viewTitle) {
        this.viewTitle = viewTitle;
    }

}
