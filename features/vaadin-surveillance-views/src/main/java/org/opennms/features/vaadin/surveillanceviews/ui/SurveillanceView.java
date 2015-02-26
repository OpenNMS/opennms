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

public class SurveillanceView extends VerticalLayout implements UIEvents.PollListener {
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceView.class);

    private class SurveillanceViewTableHeader extends HorizontalLayout {
        private Label m_label;
        private NativeSelect m_nativeSelect;

        private SurveillanceViewTableHeader() {
            setWidth(100, Unit.PERCENTAGE);
            setSpacing(false);
            setPrimaryStyleName("v-caption-surveillance-view");

            m_label = new Label();
            m_nativeSelect = new NativeSelect();
            m_nativeSelect.setNullSelectionAllowed(false);

            List<View> views = SurveillanceViewProvider.getInstance().getSurveillanceViewConfiguration().getViews();

            for (View view : views) {
                m_nativeSelect.addItem(view.getName());
            }

            m_nativeSelect.addValueChangeListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                    String name = (String) valueChangeEvent.getProperty().getValue();

                    if (!name.equals(m_view.getName())) {
                        setView(name);
                    }
                }
            });

            addComponent(m_label);
            addComponent(m_nativeSelect);

            setComponentAlignment(m_nativeSelect, Alignment.MIDDLE_RIGHT);
        }

        public void setCaptionText(String text) {
            m_label.setCaption(text);
        }

        public void select(String name) {
            m_nativeSelect.select(name);
        }

        public NativeSelect getNativeSelect() {
            return m_nativeSelect;
        }
    }

    private SurveillanceViewService m_surveillanceViewService;
    private SurveillanceViewTable m_surveillanceViewTable;
    private View m_view;
    private SurveillanceViewTableHeader m_surveillanceViewTableHeader;
    private VerticalLayout upperLayout, lowerLayout;
    private boolean m_dashboard = false, m_enabled = true;
    private int m_countdown;
    private boolean m_refreshEnabled = false;

    public SurveillanceView(View selectedView, SurveillanceViewService surveillanceViewService, boolean dashboard, boolean enabled) {
        this.m_surveillanceViewService = surveillanceViewService;
        this.m_view = selectedView;
        this.m_surveillanceViewTableHeader = new SurveillanceViewTableHeader();
        this.m_dashboard = dashboard;
        this.m_enabled = enabled;

        setSpacing(true);

        setView(selectedView);

        addAttachListener(new AttachListener() {
            @Override
            public void attach(AttachEvent attachEvent) {
                getUI().addPollListener(SurveillanceView.this);

            }
        });

        addDetachListener(new DetachListener() {
            @Override
            public void detach(DetachEvent detachEvent) {
                getUI().removePollListener(SurveillanceView.this);
            }
        });
    }

    @Override
    public void poll(UIEvents.PollEvent pollEvent) {
        if (m_refreshEnabled) {
            m_countdown--;
        }

        if (m_countdown < 0) {
            m_countdown = m_view.getRefreshSeconds();

            getUI().access(new Runnable() {
                @Override
                public void run() {
                    m_surveillanceViewTable.refresh();
                }
            });
        }
    }

    public void setView(View view) {
        m_view = view;

        m_refreshEnabled = (m_view.getRefreshSeconds() > 0);
        m_countdown = m_view.getRefreshSeconds();

        m_surveillanceViewTableHeader.setCaptionText("Surveillance view: " + m_view.getName());
        m_surveillanceViewTableHeader.select(m_view.getName());
        m_surveillanceViewTableHeader.getNativeSelect().setEnabled(m_enabled);

        removeAllComponents();

        upperLayout = new VerticalLayout();
        upperLayout.setSpacing(false);

        m_surveillanceViewTable = new SurveillanceViewTable(m_view, m_surveillanceViewService, m_dashboard, m_enabled);

        upperLayout.addComponent(m_surveillanceViewTableHeader);
        upperLayout.addComponent(m_surveillanceViewTable);

        addComponent(upperLayout);

        if (m_dashboard) {
            lowerLayout = new VerticalLayout();
            lowerLayout.setSpacing(true);

            SurveillanceViewAlarmTable surveillanceViewAlarmTable = new SurveillanceViewAlarmTable(m_surveillanceViewService, m_enabled);
            SurveillanceViewNotificationTable surveillanceViewNotificationTable = new SurveillanceViewNotificationTable(m_surveillanceViewService, m_enabled);
            SurveillanceViewNodeRtcTable surveillanceViewNodeRtcTable = new SurveillanceViewNodeRtcTable(m_surveillanceViewService, m_enabled);
            SurveillanceViewGraphComponent surveillanceViewGraphComponent = new SurveillanceViewGraphComponent(m_surveillanceViewService, m_enabled);

            lowerLayout.addComponent(surveillanceViewAlarmTable);
            lowerLayout.addComponent(surveillanceViewNotificationTable);
            lowerLayout.addComponent(surveillanceViewNodeRtcTable);
            lowerLayout.addComponent(surveillanceViewGraphComponent);

            m_surveillanceViewTable.addDetailsTable(surveillanceViewAlarmTable);
            m_surveillanceViewTable.addDetailsTable(surveillanceViewNotificationTable);
            m_surveillanceViewTable.addDetailsTable(surveillanceViewNodeRtcTable);
            m_surveillanceViewTable.addDetailsTable(surveillanceViewGraphComponent);

            addComponent(lowerLayout);
            setExpandRatio(lowerLayout, 1.0f);
        }
    }

    public void setView(String name) {
        setView(SurveillanceViewProvider.getInstance().getView(name));
    }
}
