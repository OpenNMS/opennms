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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.DashletFactory;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.features.vaadin.dashboard.model.Wallboard;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.validator.AbstractStringValidator;
import com.vaadin.v7.event.FieldEvents;
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextField;

/**
 * This class represents a component for editing {@link Wallboard} configurations.
 *
 * @author Christian Pape
 */
public class WallboardEditor extends VerticalLayout {
    /**
     * The {@link TabSheet.Tab} object used in the {@link WallboardConfigView} instance to represent this view
     */
    private TabSheet.Tab m_tab;
    /**
     * The {@link VerticalLayout} instance used to display the {@link DashletSpecEditor} views
     */
    private VerticalLayout m_verticalLayout = new VerticalLayout();
    /**
     * The {@link DashletSelector} used for querying configuration data
     */
    private DashletSelector m_dashletSelector;
    /**
     * The associated {@link Wallboard} instance
     */
    private Wallboard m_wallboard;
    /**
     * A map for holding the {@link DashletSpec} and {@link DashletSpecEditor} instances
     */
    private Map<DashletSpec, DashletSpecEditor> m_dashletSpecEditorMap = new HashMap<DashletSpec, DashletSpecEditor>();

    /**
     * Constructor used for instantiating a new object.
     *
     * @param dashletSelector the {@link DashletSelector} to be used
     * @param wallboard       the associated {@link Wallboard} instance
     */
    public WallboardEditor(DashletSelector dashletSelector, Wallboard wallboard) {
        /**
         * Setting the member fields
         */
        this.m_dashletSelector = dashletSelector;
        this.m_wallboard = wallboard;

        /**
         * Adding the {@link DashletSpec} instances
         */
        for (DashletSpec dashletSpec : wallboard.getDashletSpecs()) {
            addDashletSpec(dashletSpec);
        }

        /**
         * Setting up layout component and adding text field and button
         */
        setMargin(true);

        HorizontalLayout upperHorizontalLayout = new HorizontalLayout();
        Label label = new Label("Ops Board configuration");
        label.addStyleName("configuration-title");
        upperHorizontalLayout.addComponent(label);

        upperHorizontalLayout.addComponent(label);
        Button helpButton = new Button("Help");
        helpButton.setDescription("Display help and usage");

        helpButton.setStyleName("small");
        helpButton.addClickListener(new HelpClickListener(this, m_dashletSelector));

        upperHorizontalLayout.addComponent(helpButton);
        upperHorizontalLayout.setWidth(100, Unit.PERCENTAGE);

        upperHorizontalLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
        upperHorizontalLayout.setComponentAlignment(helpButton, Alignment.MIDDLE_RIGHT);

        addComponent(upperHorizontalLayout);

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        final Button addButton = new Button("Add dashlet");
        addButton.setId("opsboard.action.addDashlet");
        addButton.setStyleName("small");
        addButton.setDescription("Add a new dashlet instance");
        addButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent clickEvent) {
                addDashletSpec(new DashletSpec());
            }
        });

        final TextField titleField = new TextField();
        titleField.setDescription("Title for this Ops Board configuration");
        titleField.setValue(wallboard.getTitle());
        titleField.setImmediate(true);
        titleField.addValidator(new AbstractStringValidator("Title must be unique") {
            @Override
            protected boolean isValidValue(String s) {
                return (!WallboardProvider.getInstance().containsWallboard(s) || WallboardProvider.getInstance().getWallboard(s).equals(m_wallboard)) && !"".equals(s);
            }
        });

        titleField.addTextChangeListener(new FieldEvents.TextChangeListener() {
            public void textChange(FieldEvents.TextChangeEvent textChangeEvent) {
                AbstractTextField source = (AbstractTextField) textChangeEvent.getSource();
                source.setValue(textChangeEvent.getText());
                if (source.isValid()) {
                    m_tab.setCaption(textChangeEvent.getText());
                    m_wallboard.setTitle(textChangeEvent.getText());
                    WallboardProvider.getInstance().save();
                    ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Title");
                }
            }
        });

        titleField.setCaption("Title");

        final Button previewButton = new Button("Preview");
        previewButton.setId("opsboard.action.preview");
        previewButton.setDescription("Preview this Ops Board configuration");
        previewButton.setStyleName("small");
        previewButton.addClickListener(new PreviewClickListener(this, m_wallboard));

        /**
         * Adding the layout components to this component
         */
        FormLayout formLayout1 = new FormLayout();
        formLayout1.addComponent(titleField);
        horizontalLayout.addComponent(formLayout1);

        FormLayout formLayout2 = new FormLayout();
        formLayout2.addComponent(addButton);
        horizontalLayout.addComponent(formLayout2);

        FormLayout formLayout3 = new FormLayout();
        formLayout3.addComponent(previewButton);
        horizontalLayout.addComponent(formLayout3);

        addComponent(horizontalLayout);
        addComponent(m_verticalLayout);
    }

    public void swapDashletSpec(DashletSpec dashletSpec, int direction) {
        int index = m_wallboard.getDashletSpecs().indexOf(dashletSpec);

        if (index + direction >= 0 && index + direction < m_wallboard.getDashletSpecs().size()) {
            Collections.swap(m_wallboard.getDashletSpecs(), index, index + direction);
            updateDashletSpecs();
            WallboardProvider.getInstance().save();
        }
    }

    /**
     * Method used for updating the {@link DashletFactory} list
     *
     * @param serviceList the available {@link DashletFactory} instances
     */
    public void updateServiceList(List<DashletFactory> serviceList) {
        for (DashletSpecEditor dashletSpecEditor : m_dashletSpecEditorMap.values()) {
            dashletSpecEditor.updateDashletSelection(serviceList);
        }
        ((WallboardConfigUI) getUI()).notifyMessage("Configuration change", "Dashlet list modified");
    }

    /**
     * Returns the associated {@link Wallboard} instance.
     *
     * @return the associated {@link Wallboard} instance
     */
    public Wallboard getWallboard() {
        return m_wallboard;
    }

    /**
     * Method to set the {@link TabSheet.Tab} this view belongs to.
     *
     * @param tab the {@link TabSheet.Tab} to be set
     */
    public void setTab(TabSheet.Tab tab) {
        m_tab = tab;
    }

    /**
     * Updates the vertical layout to reflect ordering changes
     */
    public void updateDashletSpecs() {
        m_verticalLayout.removeAllComponents();

        for (DashletSpec dashletSpec : m_wallboard.getDashletSpecs()) {
            m_verticalLayout.addComponent(m_dashletSpecEditorMap.get(dashletSpec));
        }
    }

    /**
     * This method removes the given {@link DashletSpecEditor}.
     *
     * @param dashletSpecEditor the {@link DashletSpecEditor} to be removed
     */
    public void removeDashletSpecEditor(DashletSpecEditor dashletSpecEditor) {
        m_verticalLayout.removeComponent(dashletSpecEditor);
        m_dashletSpecEditorMap.remove(dashletSpecEditor.getDashletSpec());
        m_wallboard.getDashletSpecs().remove(dashletSpecEditor.getDashletSpec());

        WallboardProvider.getInstance().save();
    }

    /**
     * This method adds a given {@link DashletSpec}.
     *
     * @param dashletSpec the {@link DashletSpec} to be added
     */
    private void addDashletSpec(DashletSpec dashletSpec) {
        DashletSpecEditor dashletSpecEditor = new DashletSpecEditor(this, m_dashletSelector, dashletSpec);

        m_dashletSpecEditorMap.put(dashletSpec, dashletSpecEditor);

        m_verticalLayout.addComponent(dashletSpecEditor);

        if (!m_wallboard.getDashletSpecs().contains(dashletSpec)) {
            m_wallboard.getDashletSpecs().add(dashletSpec);

            WallboardProvider.getInstance().save();
        }
    }
}
