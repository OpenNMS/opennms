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

import java.util.Map;
import java.util.TreeMap;

import org.opennms.features.vaadin.dashboard.config.ui.PropertiesWindow;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;

/**
 * This class represents an abstract factory for instantiating {@link Dashlet} objects.
 *
 * @author Christian Pape
 */
public abstract class AbstractDashletFactory implements DashletFactory {
    /**
     * The name of the provided {@link Dashlet}
     */
    protected String m_name;
    /**
     * A map holding the required parameters for the {@link Dashlet}
     */
    protected Map<String, String> m_requiredParameters = new TreeMap<String, String>();
    /**
     * A map holding the required parameter descriptions for the {@link Dashlet}
     */
    protected Map<String, String> m_requiredParameterDescriptions = new TreeMap<String, String>();

    /**
     * boostable flag
     */
    protected boolean m_boostable = true;
    /**
     * Are this dashlet suitable for displaying in the dashboard view.
     */
    protected boolean m_dashboardSuitable = false;

    /**
     * Constructor for instantiating a new factory.
     */
    public AbstractDashletFactory() {
    }

    /**
     * Constructor for instantiating a new factory with the given name.
     *
     * @param name the dashlet's name
     */
    public AbstractDashletFactory(String name) {
        m_name = name;
    }

    /**
     * Add a required parameter for this factory.
     *
     * @param key          the key to use
     * @param defaultValue the default value for this parameter
     */
    protected void addRequiredParameter(String key, String defaultValue) {
        m_requiredParameters.put(key, defaultValue);
    }

    /**
     * Returns the name of the {@link Dashlet} instances this object provides.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name of the {@link Dashlet} instances this object provides.
     *
     * @param name the name
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * Returns the {@link Map} with the required parameters and default values.
     *
     * @return the {@link Map} holding the requires parameters
     */
    public Map<String, String> getRequiredParameters() {
        return m_requiredParameters;
    }

    /**
     * Returns the {@link Map} with the required parameter descriptions.
     *
     * @return the {@link Map} holding the requires parameter descriptions
     */
    public Map<String, String> getRequiredParameterDescriptions() {
        return m_requiredParameterDescriptions;
    }

    /**
     * This method sets the required parameters {@link Map}.
     *
     * @param requiredParameters the parameter {@link Map} to be set
     */
    public void setRequiredParameters(Map<String, String> requiredParameters) {
        m_requiredParameters = new TreeMap<String, String>(requiredParameters);
    }

    /**
     * This method sets the required parameter descriptions {@link Map}.
     *
     * @param requiredParameterDescriptions the parameter description {@link Map} to be set
     */
    public void setRequiredParameterDescriptions(Map<String, String> requiredParameterDescriptions) {
        m_requiredParameterDescriptions = requiredParameterDescriptions;
    }

    /**
     * This method sets the boostable flag.
     */
    public void setBoostable(boolean boostable) {
        m_boostable = boostable;
    }

    /**
     * This method sets whether this dashlet is suitable for displaying in the dashboard view.
     */
    public void setDashboardSuitable(boolean dashletSuitable) {
        m_dashboardSuitable = dashletSuitable;
    }

    /**
     * Returns whether this dashlet is suitable for displaying in the dashboard view.
     *
     * @return true if suitable, false otherwise
     */
    public boolean isSuitableForDashboard() {
        return m_dashboardSuitable;
    }

    /**
     * This method returns whether this dashlet is boostable.
     */
    public boolean isBoostable() {
        return m_boostable;
    }

    /**
     * Returns true, if the factory provides a help component for the {@link org.opennms.features.vaadin.dashboard.model.Dashlet}.
     *
     * @return true, if help component is provided, false otherwise
     */
    @Override
    public boolean providesHelpComponent() {
        return true;
    }

    /**
     * Returns the help component for the {@link Dashlet}.
     *
     * @return the help component
     */
    @Override
    public Component getHelpComponent() {
        VerticalLayout verticalLayout = new VerticalLayout();

        Label helpContent = new Label(getHelpContentHTML(), ContentMode.HTML);
        helpContent.addStyleName("help-content");

        Label helpParameters = new Label(getParameterDescriptionsHTML(), ContentMode.HTML);
        helpParameters.addStyleName("help-content");

        verticalLayout.addComponent(helpContent);
        verticalLayout.addComponent(helpParameters);

        return verticalLayout;
    }

    /**
     * Returns the help content {@link String}
     *
     * @return the help content
     */
    public abstract String getHelpContentHTML();

    /**
     * Returns the parameter help HTML content.
     *
     * @return the parameter help content
     */
    private String getParameterDescriptionsHTML() {
        final StringBuilder stringBuilder = new StringBuilder();

        if (m_requiredParameters.size() == 0) {
            return "";
        }

        stringBuilder.append("<br/><table class='help-table'>");
        stringBuilder.append("<tr>");
        stringBuilder.append("<th>Name</th>");
        stringBuilder.append("<th>Default</th>");
        stringBuilder.append("<th>Description</th>");
        stringBuilder.append("</tr>");

        for (Map.Entry<String, String> entry : m_requiredParameters.entrySet()) {
            stringBuilder.append("<tr>");
            stringBuilder.append("<td class='help-table-cell'>" + entry.getKey() + "</td>");

            String value = entry.getValue();

            if (value.length() > 20) {
                value = value.substring(0, 19) + "...";
            }

            stringBuilder.append("<td class='help-table-cell'>'" + value + "'</td>");

            if (getRequiredParameterDescriptions().containsKey(entry.getKey())) {
                stringBuilder.append("<td class='help-table-cell'>" + getRequiredParameterDescriptions().get(entry.getKey()) + "</td>");
            } else {
                stringBuilder.append("<td class='help-table-cell'>-</td>");
            }
            stringBuilder.append("</tr>");
        }

        stringBuilder.append("</table>");

        return stringBuilder.toString();
    }

    /**
     * Returns the window used for configuring a {@link DashletSpec} instance.
     *
     * @param dashletSpec the {@link DashletSpec} instance
     * @return the {@link DashletConfigurationWindow}
     */
    public DashletConfigurationWindow configurationWindow(DashletSpec dashletSpec) {
        return new PropertiesWindow(dashletSpec, this);
    }
}
