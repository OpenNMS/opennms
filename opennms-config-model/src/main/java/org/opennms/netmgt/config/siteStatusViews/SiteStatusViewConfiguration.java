/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.siteStatusViews;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Level element containing surveillance view definitions
 */
@XmlRootElement(name = "site-status-view-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("site-status-views.xsd")
public class SiteStatusViewConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_VIEW = "default";

    @XmlAttribute(name = "default-view")
    private String m_defaultView;

    @XmlElementWrapper(name = "views", required = true)
    @XmlElement(name = "view", required = true)
    private List<View> m_views = new ArrayList<>();

    public String getDefaultView() {
        return m_defaultView != null ? this.m_defaultView : DEFAULT_VIEW;
    }

    public void setDefaultView(final String defaultView) {
        m_defaultView = ConfigUtils.normalizeString(defaultView);
    }

    public List<View> getViews() {
        return m_views;
    }

    public void setViews(final List<View> views) {
        if (views == m_views) return;
        m_views.clear();
        if (views != null) m_views.addAll(views);
    }

    public void addView(final View view) {
        m_views.add(view);
    }

    public boolean removeView(final View view) {
        return m_views.remove(view);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_defaultView, m_views);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof SiteStatusViewConfiguration) {
            final SiteStatusViewConfiguration that = (SiteStatusViewConfiguration)obj;
            return Objects.equals(this.m_defaultView, that.m_defaultView)
                    && Objects.equals(this.m_views, that.m_views);
        }
        return false;
    }

}
