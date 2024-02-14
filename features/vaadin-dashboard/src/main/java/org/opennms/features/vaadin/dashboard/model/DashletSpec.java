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

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class defines the required configuration for instantiating a {@link Dashlet}.
 *
 * @author Christian Pape
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
@XmlRootElement
public class DashletSpec {

    /**
     * The normal duration before moving to the next {@link Dashlet}
     */
    private int m_duration = 15;
    /**
     * The priority of the {@link Dashlet}
     */
    private int m_priority = 5;
    /**
     * The boost duration is used to increment the normal duration when the {@link Dashlet} is boosted
     */
    private int m_boostDuration = 0;
    /**
     * The boost priority is used to decrement the normal priority when the {@link Dashlet} is boosted
     */
    private int m_boostPriority = 0;
    /**
     * The name of the associated {@link Dashlet}
     */
    private String m_dashlet = "Undefined";
    /**
     * A {@link Map} representing the parameters
     */
    private Map<String, String> m_parameters = new TreeMap<String, String>();
    /**
     * dashlet instance title
     */
    private String m_title = "";

    /**
     * Default constructor
     */
    public DashletSpec() {
    }

    /**
     * Return the normal duration for transitions.
     *
     * @return the duration
     */
    public int getDuration() {
        return m_duration;
    }

    /**
     * Sets the normal duration for transitions.
     *
     * @param duration the duration to set
     */
    public void setDuration(int duration) {
        m_duration = duration;
    }

    /**
     * Returns the boost duration value.
     *
     * @return the boost duration
     */
    public int getBoostDuration() {
        return m_boostDuration;
    }

    /**
     * Sets the boost duration value of this instance.
     *
     * @param boostDuration the boost duration to be set
     */
    public void setBoostDuration(int boostDuration) {
        m_boostDuration = boostDuration;
    }

    /**
     * Returns the normal priority of this instance.
     *
     * @return the normal priority
     */
    public int getPriority() {
        return m_priority;
    }

    /**
     * Sets the priority for this instance.
     *
     * @param priority the priority to be set
     */
    public void setPriority(int priority) {
        m_priority = priority;
    }

    /**
     * Returns the boost priority value for this instance.
     *
     * @return the boost priority value
     */
    public int getBoostPriority() {
        return m_boostPriority;
    }

    /**
     * Sets the boost priority value.
     *
     * @param boostPriority the boost priority value to be set
     */
    public void setBoostPriority(int boostPriority) {
        m_boostPriority = boostPriority;
    }

    /**
     * This method sets the {@link Dashlet} name.
     *
     * @param dashletName the name to be set
     */
    public void setDashletName(String dashletName) {
        m_dashlet = dashletName;
    }

    /**
     * This method returns the {@link Dashlet} name.
     *
     * @return the {@link Dashlet} name
     */
    public String getDashletName() {
        return m_dashlet;
    }

    /**
     * Returns the parameter {@link Map}.
     *
     * @return the parameters
     */
    @XmlElementWrapper(name = "parameters")
    public Map<String, String> getParameters() {
        return m_parameters;
    }

    /**
     * Sets the parameter {@link Map}.
     *
     * @param parameters the parameters to be set
     */
    public void setParameters(Map<String, String> parameters) {
        m_parameters = parameters;
    }

    /**
     * Returns the dashlet's title
     */
    public String getTitle() {
        return m_title;
    }

    /**
     * Sets the title.
     *
     * @param title the title to be set
     */
    public void setTitle(String title) {
        m_title = title;
    }

    @Override
    public DashletSpec clone() {
        DashletSpec dashletSpec = new DashletSpec();
        dashletSpec.setPriority(getPriority());
        dashletSpec.setDuration(getDuration());
        dashletSpec.setBoostPriority(getBoostPriority());
        dashletSpec.setBoostDuration(getBoostDuration());
        dashletSpec.setDashletName(getDashletName());
        return dashletSpec;
    }
}
