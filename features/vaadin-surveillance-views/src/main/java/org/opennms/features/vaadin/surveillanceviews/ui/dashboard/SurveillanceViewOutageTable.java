package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.data.util.BeanItemContainer;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsOutage;

import java.util.Set;

public class SurveillanceViewOutageTable extends SurveillanceViewDetailTable {
    private BeanItemContainer<OnmsOutage> m_beanItemContainer = new BeanItemContainer<OnmsOutage>(OnmsOutage.class);

    public SurveillanceViewOutageTable(SurveillanceViewService surveillanceViewService) {
        super("Outages", surveillanceViewService);

        setContainerDataSource(m_beanItemContainer);
    }

    @Override
    public void refreshDetails(Set<String> rowCategories, Set<String> columnCategories) {

    }
}
