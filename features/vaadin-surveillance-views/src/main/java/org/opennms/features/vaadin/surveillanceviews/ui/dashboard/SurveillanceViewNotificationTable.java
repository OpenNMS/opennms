package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.data.util.BeanItemContainer;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNotification;

import java.util.Set;

public class SurveillanceViewNotificationTable extends SurveillanceViewDetailTable {
    private BeanItemContainer<OnmsNotification> m_beanItemContainer = new BeanItemContainer<OnmsNotification>(OnmsNotification.class);

    public SurveillanceViewNotificationTable(SurveillanceViewService surveillanceViewService) {
        super("Notifications", surveillanceViewService);

        setContainerDataSource(m_beanItemContainer);

        setVisibleColumns("nodeLabel", "serviceType", "textMsg", "pageTime", "answeredBy", "respondTime");
    }

    @Override
    public void refreshDetails(Set<String> rowCategories, Set<String> columnCategories) {

    }
}
