package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;

import java.util.Set;

public class SurveillanceViewGraphComponent extends VerticalLayout implements SurveillanceViewDetail {
    private SurveillanceViewService m_surveillanceViewService;
    protected boolean m_enabled;
    NativeSelect m_nodeSelect, m_resourceSelect, m_graphSelect;

    public SurveillanceViewGraphComponent(SurveillanceViewService surveillanceViewService, boolean enabled) {

        m_surveillanceViewService = surveillanceViewService;
        m_enabled = enabled;

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setWidth(100, Unit.PERCENTAGE);
        horizontalLayout.setSpacing(false);
        horizontalLayout.setPrimaryStyleName("v-caption-surveillance-view");
        horizontalLayout.addComponent(new Label("Resource Graphs"));
        addComponent(horizontalLayout);

        m_nodeSelect = new NativeSelect();
        m_resourceSelect = new NativeSelect();
        m_graphSelect = new NativeSelect();

        addComponent(m_nodeSelect);
        addComponent(m_resourceSelect);
        addComponent(m_graphSelect);
    }

    protected SurveillanceViewService getSurveillanceViewService() {
        return m_surveillanceViewService;
    }

    @Override
    public void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories) {

    }
}
