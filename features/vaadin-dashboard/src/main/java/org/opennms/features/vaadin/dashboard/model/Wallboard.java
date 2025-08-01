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
