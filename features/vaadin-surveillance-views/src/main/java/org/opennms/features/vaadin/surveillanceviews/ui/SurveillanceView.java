/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.surveillanceviews.ui;

import com.vaadin.data.Property;
import com.vaadin.event.UIEvents;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.surveillanceviews.config.SurveillanceViewProvider;
import org.opennms.features.vaadin.surveillanceviews.model.View;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.features.vaadin.surveillanceviews.ui.dashboard.SurveillanceViewAlarmTable;
import org.opennms.features.vaadin.surveillanceviews.ui.dashboard.SurveillanceViewGraphComponent;
import org.opennms.features.vaadin.surveillanceviews.ui.dashboard.SurveillanceViewNodeRtcTable;
import org.opennms.features.vaadin.surveillanceviews.ui.dashboard.SurveillanceViewNotificationTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class is the base component for displaying a surveillance view and the dashboard.
 *
 * @author Christian Pape
 */
public class SurveillanceView extends VerticalLayout implements UIEvents.PollListener {
    /**
     * the logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceView.class);

    /**
     * This class is used to display a header caption like the one used by tables with a
     * selection box to select a different surveillance view.
     */
    private class SurveillanceViewTableHeader extends HorizontalLayout {
        /**
         * the labe to use
         */
        private Label m_label;
        /**
         * the selection box
         */
        private NativeSelect m_nativeSelect;

        /**
         * Default constructor.
         */
        private SurveillanceViewTableHeader() {
            /**
             * set width and spacong
             */
            setWidth(100, Unit.PERCENTAGE);
            setSpacing(false);

            /**
             * set base style name
             */
            setPrimaryStyleName("v-caption-surveillance-view");

            /**
             * construct label
             */
            m_label = new Label();
            /**
             * and selection box
             */
            m_nativeSelect = new NativeSelect();
            m_nativeSelect.setNullSelectionAllowed(false);

            /**
             * add the available views
             */
            List<View> views = SurveillanceViewProvider.getInstance().getSurveillanceViewConfiguration().getViews();

            for (View view : views) {
                m_nativeSelect.addItem(view.getName());
            }

            /**
             * add the value change listener to trigger the update
             */
            m_nativeSelect.addValueChangeListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                    String name = (String) valueChangeEvent.getProperty().getValue();

                    if (!name.equals(m_view.getName())) {
                        setView(name);
                    }
                }
            });

            /**
             * add the components...
             */
            addComponent(m_label);
            addComponent(m_nativeSelect);

            /**
             * ...and align the selection box to the right
             */
            setComponentAlignment(m_nativeSelect, Alignment.MIDDLE_RIGHT);
        }

        /**
         * Sets the caption text.
         *
         * @param text the text to be used
         */
        public void setCaptionText(String text) {
            m_label.setCaption(text);
        }

        /**
         * Selects a vie wwith the given name.
         *
         * @param name the name of the view to be selected
         */
        public void select(String name) {
            m_nativeSelect.select(name);
        }

        /**
         * Returns the select box instance used by this component.
         *
         * @return the select box instance
         */
        public NativeSelect getNativeSelect() {
            return m_nativeSelect;
        }
    }

    /**
     * the surveillance view service used
     */
    private SurveillanceViewService m_surveillanceViewService;
    /**
     * the surveillance view table instance
     */
    private SurveillanceViewTable m_surveillanceViewTable;
    /**
     * the view to be displayed
     */
    private View m_view;
    /**
     * the table header instance
     */
    private SurveillanceViewTableHeader m_surveillanceViewTableHeader;
    /**
     * layouts for the surveillance view itself and the dependent detail tables
     */
    private VerticalLayout upperLayout, lowerLayout;
    /**
     * flag whether this component should display a dashboard
     */
    private boolean m_dashboard = false;
    /**
     * flag whether links should be enabled
     */
    private boolean m_enabled = true;
    /**
     * countdown for the refresh timer
     */
    private int m_countdown;
    /**
     * flag whether refreshing is enabled
     */
    private boolean m_refreshEnabled = false;

    /**
     * Constructor for creating new instances of this component.
     *
     * @param selectedView            the view to be used
     * @param surveillanceViewService the surveillance view service
     * @param dashboard               should the dashboard be displayed?
     * @param enabled                 should links be enabled?
     */
    public SurveillanceView(View selectedView, SurveillanceViewService surveillanceViewService, boolean dashboard, boolean enabled) {
        /**
         * set the fields
         */
        this.m_surveillanceViewService = surveillanceViewService;
        this.m_view = selectedView;
        this.m_surveillanceViewTableHeader = new SurveillanceViewTableHeader();
        this.m_dashboard = dashboard;
        this.m_enabled = enabled;

        /**
         * set spacing
         */
        setSpacing(true);

        /**
         * set the view to be displayed
         */
        setView(selectedView);

        /**
         * add poll listener for refresh timer
         */
        addAttachListener(new AttachListener() {
            @Override
            public void attach(AttachEvent attachEvent) {
                getUI().addPollListener(SurveillanceView.this);

            }
        });

        /**
         * remove this listener on detach event
         */
        addDetachListener(new DetachListener() {
            @Override
            public void detach(DetachEvent detachEvent) {
                getUI().removePollListener(SurveillanceView.this);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void poll(UIEvents.PollEvent pollEvent) {
        if (m_refreshEnabled) {
            m_countdown--;
        }

        if (m_countdown < 0) {
            m_countdown = m_view.getRefreshSeconds();
            m_surveillanceViewTable.refresh();
        }
    }

    /**
     * Set the view to be displayed by this component.
     *
     * @param view the view to be displayed
     */

    public void setView(View view) {
        /**
         * set the field
         */
        m_view = view;

        /**
         * check whether refreshing is enabled
         */
        m_refreshEnabled = (m_view.getRefreshSeconds() > 0);
        m_countdown = m_view.getRefreshSeconds();

        /**
         * alter the table header
         */
        m_surveillanceViewTableHeader.setCaptionText("Surveillance view: " + m_view.getName());
        m_surveillanceViewTableHeader.select(m_view.getName());
        m_surveillanceViewTableHeader.getNativeSelect().setEnabled(m_enabled);

        /**
         * remove old components
         */
        removeAllComponents();

        /**
         * create the layout
         */
        upperLayout = new VerticalLayout();
        upperLayout.setId("surveillance-window");
        upperLayout.setSpacing(false);

        /**
         * create surveillance view table...
         */
        m_surveillanceViewTable = new SurveillanceViewTable(m_view, m_surveillanceViewService, m_dashboard, m_enabled);

        /**
         * ...and add the header and the table itself
         */
        upperLayout.addComponent(m_surveillanceViewTableHeader);
        upperLayout.addComponent(m_surveillanceViewTable);

        addComponent(upperLayout);

        /**
         * if dashoard should be displayed add the detail tables and components
         */
        if (m_dashboard) {
            lowerLayout = new VerticalLayout();
            lowerLayout.setSpacing(true);

            /**
             * create the tables and components
             */
            SurveillanceViewAlarmTable surveillanceViewAlarmTable = new SurveillanceViewAlarmTable(m_surveillanceViewService, m_enabled);
            SurveillanceViewNotificationTable surveillanceViewNotificationTable = new SurveillanceViewNotificationTable(m_surveillanceViewService, m_enabled);
            SurveillanceViewNodeRtcTable surveillanceViewNodeRtcTable = new SurveillanceViewNodeRtcTable(m_surveillanceViewService, m_enabled);
            SurveillanceViewGraphComponent surveillanceViewGraphComponent = new SurveillanceViewGraphComponent(m_surveillanceViewService, m_enabled);

            /**
             * add them to the layout
             */
            lowerLayout.addComponent(surveillanceViewAlarmTable);
            lowerLayout.addComponent(surveillanceViewNotificationTable);
            lowerLayout.addComponent(surveillanceViewNodeRtcTable);
            lowerLayout.addComponent(surveillanceViewGraphComponent);

            /**
             * associate the detail tables and components with the surveillance view table
             */
            m_surveillanceViewTable.addDetailsTable(surveillanceViewAlarmTable);
            m_surveillanceViewTable.addDetailsTable(surveillanceViewNotificationTable);
            m_surveillanceViewTable.addDetailsTable(surveillanceViewNodeRtcTable);
            m_surveillanceViewTable.addDetailsTable(surveillanceViewGraphComponent);

            /**
             * add the layout to this component
             */
            addComponent(lowerLayout);
            setExpandRatio(lowerLayout, 1.0f);
        }
    }

    /**
     * Sets the view to be displayed by name.
     *
     * @param name the name of the view to be displayed
     */
    public void setView(String name) {
        setView(SurveillanceViewProvider.getInstance().getView(name));
    }
}
