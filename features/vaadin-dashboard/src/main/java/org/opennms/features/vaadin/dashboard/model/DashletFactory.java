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

import com.vaadin.ui.Component;

import java.util.Map;

/**
 * This interface defines the required methods for implementing a factory providing {@link Dashlet} instances.
 *
 * @author Christian Pape
 */
public interface DashletFactory {
    /**
     * Returns a new {@link Dashlet} instance for a given {@link DashletSpec}.
     *
     * @param dashletSpec the {@link DashletSpec} to use
     * @return the new {@link Dashlet} instance
     */
    public abstract Dashlet newDashletInstance(DashletSpec dashletSpec);

    /**
     * Returns the name of the {@link Dashlet} instances this factory provides.
     *
     * @return the name
     */
    public String getName();

    /**
     * Returns the {@link Map} with the required parameters and default values.
     *
     * @return the {@link Map} holding the requires parameters
     */
    public Map<String, String> getRequiredParameters();

    /**
     * Returns true, if the factory provides a help component for the {@link Dashlet}.
     *
     * @return true, if help component is provided, false otherwise
     */
    public boolean providesHelpComponent();

    /**
     * Returns the help component for the {@link Dashlet}.
     *
     * @return the help component
     */
    public Component getHelpComponent();

    /**
     * Returns the window used for configuring a {@link DashletSpec} instance.
     *
     * @param dashletSpec the {@link DashletSpec} instance
     * @return the {@link DashletConfigurationWindow}
     */
    public DashletConfigurationWindow configurationWindow(DashletSpec dashletSpec);

}
