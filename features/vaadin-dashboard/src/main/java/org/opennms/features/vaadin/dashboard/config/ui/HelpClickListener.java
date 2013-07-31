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
package org.opennms.features.vaadin.dashboard.config.ui;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.DashletFactory;

import java.util.List;

/**
 * This class represents a {@link Button.ClickListener} used to invoke the help window.
 *
 * @author Christian Pape
 */
public class HelpClickListener implements Button.ClickListener {
    /**
     * The 'parent' component
     */
    private Component m_component;
    /**
     * the {@link DashletSelector} to be used
     */
    private DashletSelector m_dashletSelector;

    /**
     * The constructor of this class.
     *
     * @param component       the 'parent' component
     * @param dashletSelector the {@link DashletSelector} to be used
     */
    public HelpClickListener(Component component, DashletSelector dashletSelector) {
        m_component = component;
        m_dashletSelector = dashletSelector;
    }

    @Override
    public void buttonClick(Button.ClickEvent clickEvent) {
        final Window window = new Window("Help");

        window.setModal(true);
        window.setClosable(false);
        window.setResizable(false);

        window.setWidth("55%");
        window.setHeight("80%");

        m_component.getUI().addWindow(window);

        window.setContent(new VerticalLayout() {
            {
                setMargin(true);
                setSpacing(true);
                setSizeFull();

                HorizontalLayout horizontalLayout = new HorizontalLayout();
                horizontalLayout.setSizeFull();
                horizontalLayout.setSpacing(true);

                Tree tree = new Tree();
                tree.setNullSelectionAllowed(false);
                tree.setMultiSelect(false);
                tree.setImmediate(true);

                tree.addItem("Overview");
                tree.setChildrenAllowed("Overview", false);

                tree.addItem("Installed Dashlets");
                tree.setChildrenAllowed("Installed Dashlets", true);

                final List<DashletFactory> factories = m_dashletSelector.getDashletFactoryList();

                for (DashletFactory dashletFactory : factories) {
                    tree.addItem(dashletFactory.getName());
                    tree.setParent(dashletFactory.getName(), "Installed Dashlets");
                    tree.setChildrenAllowed(dashletFactory.getName(), false);
                }
                horizontalLayout.addComponent(tree);

                for (final Object id : tree.rootItemIds()) {
                    tree.expandItemsRecursively(id);
                }

                final Panel panel = new Panel();
                panel.setSizeFull();

                horizontalLayout.addComponent(panel);
                horizontalLayout.setExpandRatio(panel, 1.0f);

                addComponent(horizontalLayout);
                setExpandRatio(horizontalLayout, 1.0f);

                tree.addValueChangeListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                        String itemId = String.valueOf(valueChangeEvent.getProperty().getValue());

                        if ("Installed Dashlets".equals(itemId)) {
                            return;
                        }

                        if ("Overview".equals(itemId)) {
                            VerticalLayout verticalLayout = new VerticalLayout();
                            verticalLayout.setSpacing(true);
                            verticalLayout.setMargin(true);

                            verticalLayout.addComponent(new Label(getOverviewHelpHTML(), ContentMode.HTML));

                            panel.setContent(verticalLayout);
                        } else {
                            DashletFactory dashletFactory = m_dashletSelector.getDashletFactoryForName(itemId);

                            if (dashletFactory != null) {
                                if (dashletFactory.providesHelpComponent()) {
                                    VerticalLayout verticalLayout = new VerticalLayout();
                                    verticalLayout.setSpacing(true);
                                    verticalLayout.setMargin(true);

                                    Label helpTitle = new Label("Help for Dashlet '" + dashletFactory.getName() + "'");
                                    helpTitle.addStyleName("help-title");

                                    verticalLayout.addComponent(helpTitle);
                                    verticalLayout.addComponent(dashletFactory.getHelpComponent());

                                    panel.setContent(verticalLayout);
                                }
                            }
                        }
                    }
                });

                tree.select("Overview");

                addComponent(new HorizontalLayout() {
                    {
                        setMargin(true);
                        setSpacing(true);
                        setWidth("100%");

                        Button closeButton = new Button("Close");

                        addComponent(closeButton);
                        setComponentAlignment(closeButton, Alignment.MIDDLE_RIGHT);
                        closeButton.addClickListener(new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent clickEvent) {
                                window.close();
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Returns the Overview help HTML fragment.
     *
     * @return a string with HTML code
     */
    private String getOverviewHelpHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='help-title'>Overview</div>");
        sb.append("");
        sb.append("<div class='help-content'>");
        sb.append("On this configuration page you can create, edit, preview or delete ");
        sb.append("Wallboard configurations. Each Wallboard configuration consists of ");
        sb.append("multiple Dashlet entries. Each Dashlet entry requires to set at least ");
        sb.append("four parameters:<br/><br/>");
        sb.append("");
        sb.append("<table class='help-table'>");
        sb.append("  <tr>");
        sb.append("    <th>Name</th>");
        sb.append("    <th>Description</th>");
        sb.append("  </tr>");
        sb.append("  <tr><td class='help-table-cell'>duration</td><td class='help-table-cell'>Time in seconds the Dashlet will be displayed</td></tr>");
        sb.append("  <tr><td class='help-table-cell'>priority</td><td class='help-table-cell'>Lower priority means that the Dashlet will be displayed more often</td></tr>");
        sb.append("  <tr><td class='help-table-cell'>boostedDuration</td><td class='help-table-cell'>This value is added to the duration value if the Dashlet is \"boosted\"</td></tr>");
        sb.append("  <tr><td class='help-table-cell'>boostedPriority</td><td class='help-table-cell'>This value is subtracted from the Dashlet's priority then the Dashlet is \"boosted\"</td></tr>");
        sb.append("</table>");
        sb.append("<br/>");
        sb.append("The Dashlet itself computes whether it is \"boosted\" or not. So, a Dashlet ");
        sb.append("displaying critical data will be displayed more often and for a longer duration. ");
        sb.append("Please select a Dashlet entry on the left for a brief description of the Dashlet ");
        sb.append("and its required parameters.");
        sb.append("</div>");
        sb.append("");
        return sb.toString();
    }
}