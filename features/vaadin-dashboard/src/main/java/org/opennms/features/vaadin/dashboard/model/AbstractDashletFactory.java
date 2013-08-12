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

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.dashboard.config.ui.PropertiesWindow;

import java.util.Map;
import java.util.TreeMap;

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
        StringBuilder stringBuilder = new StringBuilder();

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
            stringBuilder.append("<td class='help-table-cell'>'" + entry.getValue() + "'</td>");

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
