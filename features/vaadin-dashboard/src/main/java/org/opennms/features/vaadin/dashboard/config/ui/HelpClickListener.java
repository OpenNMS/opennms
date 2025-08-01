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
package org.opennms.features.vaadin.dashboard.config.ui;

import java.util.List;

import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.DashletFactory;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Tree;

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
        final StringBuilder sb = new StringBuilder();
        sb.append("<div class='help-title'>Overview</div>");
        sb.append("");
        sb.append("<div class='help-content'>");
        sb.append("On this configuration page you can create, edit, preview or delete ");
        sb.append("Ops Board configurations. Each Ops Board configuration consists of ");
        sb.append("multiple Dashlet entries. Each Dashlet entry requires to set at least ");
        sb.append("four parameters:<br/><br/>");
        sb.append("");
        sb.append("<table class='help-table'>");
        sb.append("  <tr>");
        sb.append("    <th>Name</th>");
        sb.append("    <th>Description</th>");
        sb.append("  </tr>");
        sb.append("  <tr><td class='help-table-cell'>title</td><td class='help-table-cell'>The title for this Dashlet instance</td></tr>");
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