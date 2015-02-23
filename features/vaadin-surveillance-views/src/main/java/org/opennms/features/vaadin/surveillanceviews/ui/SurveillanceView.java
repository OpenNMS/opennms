package org.opennms.features.vaadin.surveillanceviews.ui;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.surveillanceviews.config.SurveillanceViewProvider;
import org.opennms.features.vaadin.surveillanceviews.model.View;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.features.vaadin.surveillanceviews.ui.dashboard.SurveillanceViewAlarmTable;
import org.opennms.features.vaadin.surveillanceviews.ui.dashboard.SurveillanceViewNodeRtcTable;
import org.opennms.features.vaadin.surveillanceviews.ui.dashboard.SurveillanceViewNotificationTable;

import java.util.List;

public class SurveillanceView extends VerticalLayout {

    private class SurveillanceViewTableHeader extends HorizontalLayout {
        private Label m_label;
        private NativeSelect m_nativeSelect;

        public SurveillanceViewTableHeader() {
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

    public SurveillanceView(View selectedView, SurveillanceViewService surveillanceViewService, boolean dashboard, boolean enabled) {
        this.m_surveillanceViewService = surveillanceViewService;
        this.m_view = selectedView;
        this.m_surveillanceViewTableHeader = new SurveillanceViewTableHeader();
        this.m_dashboard = dashboard;
        this.m_enabled = enabled;

        setSpacing(true);

        setView(selectedView);
    }

    public void setView(View view) {
        m_view = view;

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

            lowerLayout.addComponent(surveillanceViewAlarmTable);
            lowerLayout.addComponent(surveillanceViewNotificationTable);
            lowerLayout.addComponent(surveillanceViewNodeRtcTable);

            m_surveillanceViewTable.addDetailsTable(surveillanceViewAlarmTable);
            m_surveillanceViewTable.addDetailsTable(surveillanceViewNotificationTable);
            m_surveillanceViewTable.addDetailsTable(surveillanceViewNodeRtcTable);

            addComponent(lowerLayout);
            setExpandRatio(lowerLayout, 1.0f);
        }
    }

    public void setView(String name) {
        setView(SurveillanceViewProvider.getInstance().getView(name));
    }
}
