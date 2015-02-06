package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.data.util.BeanItemContainer;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsAlarm;

import java.util.List;
import java.util.Set;

public class SurveillanceViewAlarmTable extends SurveillanceViewDetailTable {

    private BeanItemContainer<OnmsAlarm> m_beanItemContainer = new BeanItemContainer<OnmsAlarm>(OnmsAlarm.class);

    public SurveillanceViewAlarmTable(SurveillanceViewService surveillanceViewService) {
        super("Alarms", surveillanceViewService);

        setContainerDataSource(m_beanItemContainer);

        setVisibleColumns("nodeLabel", "logMsg", "counter", "firstEventTime", "lastEventTime");

        setColumnHeader("nodeLabel", "Node");
        setColumnHeader("logMsg", "Log Msg");
        setColumnHeader("counter", "Count");
        setColumnHeader("firstEventTime", "First Time");
        setColumnHeader("lastEventTime", "Last Time");
    }

    @Override
    public void refreshDetails(Set<String> rowCategories, Set<String> columnCategories) {

        final CriteriaBuilder alarmCb = new CriteriaBuilder(OnmsAlarm.class);

        alarmCb.alias("node", "node");
        alarmCb.alias("lastEvent", "event");
        alarmCb.ne("node.type", "D");

        if (rowCategories != null || columnCategories != null) {
            alarmCb.alias("node.categories", "category");

            if (rowCategories != null && rowCategories.size() > 0) {
                alarmCb.in("category.name", rowCategories);
            }
            if (columnCategories != null && columnCategories.size() > 0) {
                alarmCb.in("category.name", columnCategories);
            }
        }
        alarmCb.fetch("firstEvent", Fetch.FetchType.EAGER);
        alarmCb.fetch("lastEvent", Fetch.FetchType.EAGER);

        alarmCb.distinct();

        List<OnmsAlarm> alarms = getSurveillanceViewService().getAlarmDao().findMatching(alarmCb.toCriteria());

        m_beanItemContainer.removeAllItems();

        for (OnmsAlarm alarm : alarms) {
            m_beanItemContainer.addItem(alarm);
        }

        refreshRowCache();
    }
}
