/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.surveillanceviews.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the root element of the surveillance view configuration xml.
 *
 * @author Christian Pape
 */
@XmlRootElement(name = "surveillance-view-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class SurveillanceViewConfiguration implements java.io.Serializable {
    /**
     * the field for storing the default view attribute
     */
    @XmlAttribute(name = "default-view", required = false)
    private java.lang.String m_defaultView = "default";
    /**
     * the list of defined view elements
     */
    @XmlElement(name = "view")
    @XmlElementWrapper(name = "views")
    private List<View> m_views = new LinkedList<View>();

    /**
     * Returns the name of the default view.
     *
     * @return the default view
     */
    public String getDefaultView() {
        return m_defaultView;
    }

    /**
     * Sets the default view.
     *
     * @param defaultView the name of the default view to use
     */
    public void setDefaultView(String defaultView) {
        this.m_defaultView = defaultView;
    }

    /**
     * Returns the list of views.
     *
     * @return the list of views
     */
    public List<View> getViews() {
        return m_views;
    }

    /**
     * Checks whether this configuration instance contains a view with the given name.
     *
     * @param name the name to search for
     * @return true, if a view with this name exists, false otherwise
     */
    public boolean containsView(String name) {
        for (View view : getViews()) {
            if (name.equals(view.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SurveillanceViewConfiguration that = (SurveillanceViewConfiguration) o;

        if (m_defaultView != null ? !m_defaultView.equals(that.m_defaultView) : that.m_defaultView != null) {
            return false;
        }
        if (m_views != null ? !m_views.equals(that.m_views) : that.m_views != null) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = m_defaultView != null ? m_defaultView.hashCode() : 0;
        result = 31 * result + (m_views != null ? m_views.hashCode() : 0);
        return result;
    }
}
