package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;

import java.util.List;
import java.util.Set;

public class SurveillanceViewAlarmTable extends SurveillanceViewDetailTable {

    private BeanItemContainer<OnmsAlarm> m_beanItemContainer = new BeanItemContainer<OnmsAlarm>(OnmsAlarm.class);

    public SurveillanceViewAlarmTable(SurveillanceViewService surveillanceViewService) {
        super("Alarms", surveillanceViewService);

        setContainerDataSource(m_beanItemContainer);

        addStyleName("surveillance-view");

        addGeneratedColumn("node", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, Object itemId, Object propertyId) {
                OnmsAlarm onmsAlarm = (OnmsAlarm) itemId;
                Link link = new Link(onmsAlarm.getNodeLabel(), new ExternalResource("/opennms/element/node.jsp?node=" + onmsAlarm.getNodeId()));
                link.setPrimaryStyleName("surveillance-view");
                return link;
            }
        });

        setCellStyleGenerator(new CellStyleGenerator() {
            @Override
            public String getStyle(final Table source, final Object itemId, final Object propertyId) {
                OnmsAlarm onmsAlarm = ((OnmsAlarm) itemId);

                String style = onmsAlarm.getSeverity().getLabel().toLowerCase();

                if ("logMsg".equals(propertyId)) {
                    style += "-image";
                }

                return style;
            }
        });

        setColumnHeader("node", "Node");
        setColumnHeader("logMsg", "Log Msg");
        setColumnHeader("counter", "Count");
        setColumnHeader("firstEventTime", "First Time");
        setColumnHeader("lastEventTime", "Last Time");

        setVisibleColumns("node", "logMsg", "counter", "firstEventTime", "lastEventTime");
    }

    @Override
    public void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories) {
        List<OnmsAlarm> alarms = getSurveillanceViewService().getAlarmsForCategories(rowCategories, colCategories);

        m_beanItemContainer.removeAllItems();

        if (alarms != null & !alarms.isEmpty()) {
            for (OnmsAlarm alarm : alarms) {
                m_beanItemContainer.addItem(alarm);
            }
        }
        sort(new Object[]{"firstEventTime"}, new boolean[]{true});

        refreshRowCache();
    }
}
