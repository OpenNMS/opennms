package org.opennms.features.vaadin.surveillanceviews.model;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "surveillance-view-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class SurveillanceViewConfiguration implements java.io.Serializable {

    @XmlAttribute(name = "default-view", required = false)
    private java.lang.String m_defaultView = "default";

    public String getDefaultView() {
        return m_defaultView;
    }

    public void setDefaultView(String defaultView) {
        this.m_defaultView = defaultView;
    }

    @XmlElement(name = "view")
    @XmlElementWrapper(name = "views")
    private List<View> m_views = new LinkedList<View>();

    public List<View> getViews() {
        return m_views;
    }

    public boolean containsView(String name) {
        for (View view : getViews()) {
            if (name.equals(view.getName())) {
                return true;
            }
        }
        return false;
    }

}
