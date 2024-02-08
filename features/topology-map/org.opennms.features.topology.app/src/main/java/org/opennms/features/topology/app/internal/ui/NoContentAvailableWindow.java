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
package org.opennms.features.topology.app.internal.ui;

import java.util.List;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.Criteria;

import com.vaadin.server.Sizeable;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class NoContentAvailableWindow extends Window {

    private final Label noDefaultsAvailable;

    public NoContentAvailableWindow(final GraphContainer graphContainer) {
        super("No focus defined");

        setId("no-focus-defined-window");
        setResizable(false);
        setClosable(false);
        setDraggable(true);
        setModal(false);
        setWidth(500, Sizeable.Unit.PIXELS);
        setHeight(300, Sizeable.Unit.PIXELS);

        Label label = new Label("This means" +
                "<ul>" +
                "<li>the last vertex was removed from focus or</li>" +
                "<li>no default focus is available.</li>" +
                "</ul>" +
                "To add a vertex to focus" +
                "<ul>" +
                "<li>manually add a vertex to focus via the search box</li>" +
                "<li>use the default focus</li>" +
                "</ul>",  ContentMode.HTML);

        final HorizontalLayout defaultLayout = new HorizontalLayout();
        defaultLayout.setMargin(true);
        defaultLayout.setSpacing(true);
        noDefaultsAvailable = new Label("No vertices found.<br/>Please add vertices manually.", ContentMode.HTML);
        noDefaultsAvailable.setVisible(false);

        Button defaultFocusButton = new Button("Use Default Focus");
        defaultFocusButton.setId("defaultFocusBtn");
        defaultFocusButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                List<Criteria> defaultCriteriaList = graphContainer.getTopologyServiceClient().getDefaults().getCriteria();
                if (defaultCriteriaList != null && !defaultCriteriaList.isEmpty()) {
                    defaultCriteriaList.forEach(graphContainer::addCriteria);
                    graphContainer.redoLayout();
                    noDefaultsAvailable.setVisible(false);
                } else {
                    noDefaultsAvailable.setVisible(true);
                }
            }
        });
        defaultLayout.setMargin(true);
        defaultLayout.addComponent(defaultFocusButton);
        defaultLayout.addComponent(noDefaultsAvailable);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setMargin(true);
        contentLayout.addComponent(label);
        contentLayout.addComponent(defaultLayout);

        setContent(contentLayout);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            noDefaultsAvailable.setVisible(false);
        } else {
            center();
        }
    }
}
