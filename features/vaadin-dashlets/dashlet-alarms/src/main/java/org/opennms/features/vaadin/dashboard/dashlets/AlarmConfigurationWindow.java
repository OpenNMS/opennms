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
package org.opennms.features.vaadin.dashboard.dashlets;

import org.opennms.features.vaadin.dashboard.config.ui.WallboardConfigUI;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.config.ui.editors.CriteriaBuilderComponent;
import org.opennms.features.vaadin.dashboard.config.ui.editors.CriteriaBuilderHelper;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.ui.NativeSelect;

/**
 * This class represents the configuration window for alarm dashlets.
 *
 * @author Christian Pape
 */
public class AlarmConfigurationWindow extends DashletConfigurationWindow {
    /**
     * The {@link DashletSpec} to be used
     */
    private DashletSpec m_dashletSpec;
    /**
     * The field for storing the 'boostSeverity' parameter
     */
    private NativeSelect m_boostedSeveritySelect;

    /**
     * Constructor for instantiating new objects of this class.
     *
     * @param dashletSpec the {@link DashletSpec} to be edited
     */
    public AlarmConfigurationWindow(DashletSpec dashletSpec) {
        /**
         * Setting the members
         */
        m_dashletSpec = dashletSpec;

        /**
         * Setting up the base layouts
         */
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setHeight(100, Unit.PERCENTAGE);
        verticalLayout.setSizeFull();
        verticalLayout.setSpacing(true);
        verticalLayout.setMargin(true);

        /**
         * Adding the selection box
         */
        m_boostedSeveritySelect = new NativeSelect();
        m_boostedSeveritySelect.setCaption("Boosted Severity");
        m_boostedSeveritySelect.setDescription("Select the boost severity");
        m_boostedSeveritySelect.setMultiSelect(false);
        m_boostedSeveritySelect.setNullSelectionAllowed(false);
        m_boostedSeveritySelect.setInvalidAllowed(false);
        m_boostedSeveritySelect.setNewItemsAllowed(false);

        for (OnmsSeverity onmsSeverity : OnmsSeverity.values()) {
            m_boostedSeveritySelect.addItem(onmsSeverity.name());
        }

        String boostSeverity = m_dashletSpec.getParameters().get("boostSeverity");

        if (boostSeverity == null || "".equals(boostSeverity)) {
            boostSeverity = OnmsSeverity.CLEARED.name();
        }

        m_boostedSeveritySelect.setValue(boostSeverity);

        verticalLayout.addComponent(m_boostedSeveritySelect);

        /**
         * Setting up the {@link CriteriaBuilderComponent} component
         */
        CriteriaBuilderHelper criteriaBuilderHelper = new CriteriaBuilderHelper(OnmsAlarm.class, OnmsNode.class, OnmsEvent.class, OnmsCategory.class);

        final CriteriaBuilderComponent criteriaBuilderComponent = new CriteriaBuilderComponent(criteriaBuilderHelper, m_dashletSpec.getParameters().get("criteria"));

        verticalLayout.addComponent(criteriaBuilderComponent);
        verticalLayout.setExpandRatio(criteriaBuilderComponent, 1.0f);

        /**
         * Using an additional {@link com.vaadin.ui.HorizontalLayout} for layouting the buttons
         */
        HorizontalLayout buttonLayout = new HorizontalLayout();

        buttonLayout.setMargin(true);
        buttonLayout.setSpacing(true);
        buttonLayout.setWidth("100%");
        /**
         * Adding the cancel button...
         */
        Button cancel = new Button("Cancel");
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        cancel.setDescription("Cancel editing");
        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        buttonLayout.addComponent(cancel);
        buttonLayout.setExpandRatio(cancel, 1.0f);
        buttonLayout.setComponentAlignment(cancel, Alignment.TOP_RIGHT);

        /**
         * ...and the OK button
         */
        Button ok = new Button("Save");
        ok.setDescription("Save properties and close");
        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                m_dashletSpec.getParameters().put("criteria", criteriaBuilderComponent.getCriteria());
                m_dashletSpec.getParameters().put("boostSeverity", String.valueOf(m_boostedSeveritySelect.getValue()));

                WallboardProvider.getInstance().save();
                ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Properties");

                close();
            }
        });

        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        buttonLayout.addComponent(ok);

        /**
         * Adding the layout and setting the content
         */
        verticalLayout.addComponent(buttonLayout);

        setContent(verticalLayout);
    }
}
