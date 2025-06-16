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
