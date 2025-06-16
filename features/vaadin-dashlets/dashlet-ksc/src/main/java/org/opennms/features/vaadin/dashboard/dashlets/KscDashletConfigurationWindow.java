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

import java.util.Map;

import org.opennms.features.vaadin.dashboard.config.ui.WallboardConfigUI;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.ui.NativeSelect;

/**
 * This class is used to display a custom configuration window for the Ksc dashlet.
 *
 * @author Christian Pape
 */
public class KscDashletConfigurationWindow extends DashletConfigurationWindow {

    /**
     * The {@link DashletSpec} to be used
     */
    private DashletSpec m_dashletSpec;
    /**
     * The field for storing the 'boostSeverity' parameter
     */
    private NativeSelect m_kscSelect;

    /**
     * Constructor for instantiating new objects of this class.
     *
     * @param dashletSpec the {@link org.opennms.features.vaadin.dashboard.model.DashletSpec} to be edited
     */
    public KscDashletConfigurationWindow(DashletSpec dashletSpec) {
        /**
         * Setting the members
         */
        m_dashletSpec = dashletSpec;

        setHeight(210, Unit.PIXELS);
        setWidth(40, Unit.PERCENTAGE);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth(100, Unit.PERCENTAGE);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setMargin(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidth(100, Unit.PERCENTAGE);
        formLayout.setSpacing(true);
        formLayout.setMargin(true);

        m_kscSelect = new NativeSelect();
        m_kscSelect.setDescription("Select KSC-report to be displayed");
        m_kscSelect.setCaption("KSC-Report");
        m_kscSelect.setImmediate(true);
        m_kscSelect.setNewItemsAllowed(false);
        m_kscSelect.setMultiSelect(false);
        m_kscSelect.setInvalidAllowed(false);
        m_kscSelect.setNullSelectionAllowed(false);
        m_kscSelect.setImmediate(true);

        final KSC_PerformanceReportFactory kscPerformanceReportFactory = KSC_PerformanceReportFactory.getInstance();

        Map<Integer, String> reportsMap = kscPerformanceReportFactory.getReportList();

        for (Map.Entry<Integer, String> entry : reportsMap.entrySet()) {
            m_kscSelect.addItem(entry.getKey());
            m_kscSelect.setItemCaption(entry.getKey(), entry.getValue());
            if (m_kscSelect.getValue() == null) {
                m_kscSelect.setValue(entry.getKey());
            }
        }

        String chartName = m_dashletSpec.getParameters().get("kscReport");

        if (chartName != null) {
            if (reportsMap.values().contains(chartName)) {
                m_kscSelect.setValue(chartName);
            }
        }

        formLayout.addComponent(m_kscSelect);

        m_kscSelect.setValue(chartName);
        m_kscSelect.setImmediate(true);

        horizontalLayout.addComponent(formLayout);

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
        cancel.setDescription("Cancel editing");
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

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
                Map<Integer, String> reportsMap = kscPerformanceReportFactory.getReportList();

                m_dashletSpec.getParameters().put("kscReport", reportsMap.get(m_kscSelect.getValue()));

                WallboardProvider.getInstance().save();
                ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Properties");

                close();
            }
        });

        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        buttonLayout.addComponent(ok);

        if (reportsMap.size() == 0) {
            m_kscSelect.setEnabled(false);
            ok.setEnabled(false);
        }

        /**
         * Adding the layout and setting the content
         */

        VerticalLayout verticalLayout = new VerticalLayout();

        verticalLayout.addComponent(horizontalLayout);
        verticalLayout.addComponent(buttonLayout);

        setContent(verticalLayout);
    }
}
