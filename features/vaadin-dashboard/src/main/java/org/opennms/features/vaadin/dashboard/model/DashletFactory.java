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

import com.vaadin.ui.Component;

import java.util.Map;

/**
 * This interface defines the required methods for implementing a factory providing {@link Dashlet} instances.
 *
 * @author Christian Pape
 */
public interface DashletFactory {
    /**
     * Returns whether this dashlet is suitable for displaying in the dashboard view.
     *
     * @return true if suitable, false otherwise
     */
    public boolean isSuitableForDashboard();
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

    /**
     * This method returns whether this dashlet is boostable.
     */
    public boolean isBoostable();
}
