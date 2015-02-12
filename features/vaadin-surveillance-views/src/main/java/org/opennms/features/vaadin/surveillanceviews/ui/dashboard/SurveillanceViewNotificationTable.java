package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SurveillanceViewNotificationTable extends SurveillanceViewDetailTable {
    private BeanItemContainer<OnmsNotification> m_beanItemContainer = new BeanItemContainer<OnmsNotification>(OnmsNotification.class);
    private HashMap<OnmsNotification, String> m_customSeverity = new HashMap<>();

    public SurveillanceViewNotificationTable(SurveillanceViewService surveillanceViewService) {
        super("Notifications", surveillanceViewService);

        setContainerDataSource(m_beanItemContainer);

        setVisibleColumns("nodeLabel", "serviceType", "textMsg", "pageTime", "answeredBy", "respondTime");

        setColumnHeader("nodeLabel", "Node");
        setColumnHeader("serviceType", "Service");
        setColumnHeader("textMsg", "Message");
        setColumnHeader("pageTime", "Sent Time");
        setColumnHeader("answeredBy", "Responder");
        setColumnHeader("respondTime", "Respond Time");

        addStyleName("surveillance-view");

        setCellStyleGenerator(new CellStyleGenerator() {
            @Override
            public String getStyle(final Table source, final Object itemId, final Object propertyId) {
                String style = null;

                OnmsNotification onmsNotification = ((OnmsNotification) itemId);

                style = m_customSeverity.get(onmsNotification).toLowerCase();

                return style;
            }
        });
    }

    private List<OnmsNotification> getNotificationsWithCriterion(List<OnmsNode> nodes, String severity, Restriction... criterias) {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsNotification.class);

        criteriaBuilder.alias("node", "node");
        criteriaBuilder.in("node", nodes);
        criteriaBuilder.ne("node.type", "D");
        criteriaBuilder.orderBy("pageTime", false);

        Criteria myCriteria = criteriaBuilder.toCriteria();

        for (Restriction criteria : criterias) {
            myCriteria.addRestriction(criteria);
        }

        List<OnmsNotification> notifications = getSurveillanceViewService().getNotificationDao().findMatching(myCriteria);

        for (OnmsNotification onmsNotification : notifications) {
            m_customSeverity.put(onmsNotification, severity);
        }

        return notifications;
    }

    @Override
    public void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories) {
        if (rowCategories == null || colCategories == null) {
            return;
        }

        List<OnmsNode> nodes = null;

        if (rowCategories.size() == 0 || colCategories.size() == 0) {
            if (rowCategories.size() == 0 && colCategories.size() > 0) {
                nodes = getSurveillanceViewService().getNodeDao().findAllByCategoryList(colCategories);
            }

            if (rowCategories.size() > 0 && colCategories.size() == 0) {
                nodes = getSurveillanceViewService().getNodeDao().findAllByCategoryList(rowCategories);
            }
        } else {
            nodes = getSurveillanceViewService().getNodeDao().findAllByCategoryLists(rowCategories, colCategories);
        }

        List<OnmsNotification> notifications = new ArrayList<OnmsNotification>();

        Date fifteenMinutesAgo = new Date(System.currentTimeMillis() - (15 * 60 * 1000));
        Date oneWeekAgo = new Date(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000));

        m_customSeverity.clear();
        m_beanItemContainer.removeAllItems();

        if (nodes != null && nodes.size() > 0) {
            notifications.addAll(getNotificationsWithCriterion(nodes, "Critical", Restrictions.isNull("respondTime"), Restrictions.le("pageTime", fifteenMinutesAgo)));
            notifications.addAll(getNotificationsWithCriterion(nodes, "Minor", Restrictions.isNull("respondTime"), Restrictions.gt("pageTime", fifteenMinutesAgo)));
            notifications.addAll(getNotificationsWithCriterion(nodes, "Normal", Restrictions.isNotNull("respondTime"), Restrictions.gt("pageTime", oneWeekAgo)));

            for (OnmsNotification onmsNotification : notifications) {
                m_beanItemContainer.addItem(onmsNotification);
            }
        }

        sort(new Object[]{"pageTime"}, new boolean[]{false});

        refreshRowCache();
    }
}
