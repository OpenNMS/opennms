package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.ui.Table;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;

import java.util.Set;

public abstract class SurveillanceViewDetailTable extends Table {
    private SurveillanceViewService m_surveillanceViewService;

    public SurveillanceViewDetailTable(String title, SurveillanceViewService surveillanceViewService) {
        super(title);
        m_surveillanceViewService = surveillanceViewService;
        setSizeFull();
    }

    protected SurveillanceViewService getSurveillanceViewService() {
        return m_surveillanceViewService;
    }

    public abstract void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> columnCategories);
}
