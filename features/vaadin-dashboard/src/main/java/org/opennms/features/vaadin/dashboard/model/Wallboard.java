/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.dashboard.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a collection of {@link DashletSpec} objects.
 *
 * @author Christian Pape
 */
@XmlRootElement
public class Wallboard {
    /**
     * A {@link List} holding the associated {@link DashletSpec} instances
     */
    private List<DashletSpec> m_dashletSpecs = new LinkedList<>();
    /**
     * The title of this wallboard
     */
    private String m_title;
    /**
     * Is this one the default?
     */
    private boolean m_default = false;

    /**
     * Default constructor
     */
    public Wallboard() {
    }

    /**
     * Copy constructor.
     *
     * @param wallboard the {@link Wallboard} to be cloned
     */
    public Wallboard(Wallboard wallboard) {
        m_title = wallboard.getTitle();
        for (DashletSpec dashletSpec : wallboard.getDashletSpecs()) {
            this.m_dashletSpecs.add(dashletSpec.clone());
        }
    }

    /**
     * Returns whether this wallboard is the default wallboard.
     *
     * @return default value
     */
    public boolean isDefault() {
        return m_default;
    }

    /**
     * Sets the default value.
     */
    public void setDefault(boolean defaultValue) {
        m_default = defaultValue;
    }

    /**
     * Sets the title of this wallboard.
     *
     * @param title the name to be set
     */
    public void setTitle(String title) {
        m_title = title;
    }

    /**
     * Returns the title of this wallboard.
     *
     * @return the title of the wallboard
     */
    @XmlAttribute(name = "title")
    public String getTitle() {
        return m_title;
    }

    /**
     * Returns the associated {@link DashletSpec} instances.
     *
     * @return the {@link DashletSpec} instances of this object
     */
    @XmlElement(name = "dashlet")
    @XmlElementWrapper(name = "dashlets")
    public List<DashletSpec> getDashletSpecs() {
        return m_dashletSpecs;
    }
}
