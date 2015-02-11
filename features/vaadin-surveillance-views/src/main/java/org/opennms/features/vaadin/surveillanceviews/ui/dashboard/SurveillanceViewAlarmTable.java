package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.data.util.BeanItemContainer;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;

import java.util.HashSet;
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
    public void refreshDetails(Set<String> rowCategories, Set<String> colCategories) {
        if (rowCategories == null || colCategories == null) {
            return;
        }

        List<OnmsCategory> onmsCategories = getSurveillanceViewService().getOnmsCategories();

        Set<OnmsCategory> rows = new HashSet<>();
        Set<OnmsCategory> cols = new HashSet<>();

        for (OnmsCategory onmsCategory : onmsCategories) {
            if (rowCategories.contains(onmsCategory.getName())) {
                rows.add(onmsCategory);
            }
            if (colCategories.contains(onmsCategory.getName())) {
                cols.add(onmsCategory);
            }
        }

        List<OnmsNode> nodes = null;

        if (rows.size() == 0 || cols.size() == 0) {
            if (rows.size() == 0 && cols.size() > 0) {
                nodes = getSurveillanceViewService().getNodeDao().findAllByCategoryList(cols);
            }

            if (rows.size() > 0 && cols.size() == 0) {
                nodes = getSurveillanceViewService().getNodeDao().findAllByCategoryList(rows);
            }
        } else {
            nodes = getSurveillanceViewService().getNodeDao().findAllByCategoryLists(rows, cols);
        }

        final CriteriaBuilder alarmCb = new CriteriaBuilder(OnmsAlarm.class);

        alarmCb.alias("node", "node");
        alarmCb.alias("lastEvent", "event");
        alarmCb.ne("node.type", "D");

        //alarmCb.fetch("firstEvent", Fetch.FetchType.EAGER);
        //alarmCb.fetch("lastEvent", Fetch.FetchType.EAGER);

        alarmCb.distinct();

        m_beanItemContainer.removeAllItems();

        if (nodes != null && nodes.size() > 0) {
            alarmCb.in("node", nodes);

            List<OnmsAlarm> alarms = getSurveillanceViewService().getAlarmDao().findMatching(alarmCb.toCriteria());

            for (OnmsAlarm alarm : alarms) {
                m_beanItemContainer.addItem(alarm);
            }
        }

        refreshRowCache();
    }
}
