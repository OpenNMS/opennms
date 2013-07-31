/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
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
