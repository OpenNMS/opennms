package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNotification;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SurveillanceViewNotificationTable extends SurveillanceViewDetailTable {
    private BeanItemContainer<OnmsNotification> m_beanItemContainer = new BeanItemContainer<OnmsNotification>(OnmsNotification.class);
    private HashMap<OnmsNotification, String> m_customSeverity = new HashMap<>();

    public SurveillanceViewNotificationTable(SurveillanceViewService surveillanceViewService) {
        super("Notifications", surveillanceViewService);

        setContainerDataSource(m_beanItemContainer);

        addStyleName("surveillance-view");

        addGeneratedColumn("node", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, Object itemId, Object propertyId) {
                OnmsNotification onmsNotification = (OnmsNotification) itemId;
                Link link = new Link(onmsNotification.getNodeLabel(), new ExternalResource("/opennms/element/node.jsp?node=" + onmsNotification.getNodeId()));
                link.setPrimaryStyleName("surveillance-view");
                return link;
            }
        });

        setCellStyleGenerator(new CellStyleGenerator() {
            @Override
            public String getStyle(final Table source, final Object itemId, final Object propertyId) {
                OnmsNotification onmsNotification = ((OnmsNotification) itemId);
                return m_customSeverity.get(onmsNotification).toLowerCase();
            }
        });

        setColumnHeader("node", "Node");
        setColumnHeader("serviceType", "Service");
        setColumnHeader("textMsg", "Message");
        setColumnHeader("pageTime", "Sent Time");
        setColumnHeader("answeredBy", "Responder");
        setColumnHeader("respondTime", "Respond Time");

        setVisibleColumns("node", "serviceType", "textMsg", "pageTime", "answeredBy", "respondTime");
    }

    @Override
    public void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories) {
        List<OnmsNotification> notifications = getSurveillanceViewService().getNotificationsForCategories(rowCategories, colCategories, m_customSeverity);

        m_beanItemContainer.removeAllItems();

        if (notifications != null && !notifications.isEmpty()) {
            for (OnmsNotification onmsNotification : notifications) {
                m_beanItemContainer.addItem(onmsNotification);
            }
        }

        sort(new Object[]{"pageTime"}, new boolean[]{false});

        refreshRowCache();
    }
}
