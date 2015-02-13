package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;

import java.util.List;
import java.util.Set;

public class SurveillanceViewNodeRtcTable extends SurveillanceViewDetailTable {
    private BeanItemContainer<SurveillanceViewService.NodeRtc> m_beanItemContainer = new BeanItemContainer<SurveillanceViewService.NodeRtc>(SurveillanceViewService.NodeRtc.class);

    public SurveillanceViewNodeRtcTable(SurveillanceViewService surveillanceViewService) {
        super("Outages", surveillanceViewService);

        setContainerDataSource(m_beanItemContainer);

        addStyleName("surveillance-view");

        addGeneratedColumn("node", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, Object itemId, Object propertyId) {
                SurveillanceViewService.NodeRtc nodeRtc = (SurveillanceViewService.NodeRtc) itemId;
                Link link = new Link(nodeRtc.getNode().getLabel(), new ExternalResource("/opennms/element/node.jsp?node=" + nodeRtc.getNode().getNodeId()));
                link.setPrimaryStyleName("surveillance-view");
                link.addStyleName("white");
                return link;
            }
        });

        addGeneratedColumn("currentOutages", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, final Object itemId, Object columnId) {
                SurveillanceViewService.NodeRtc nodeRtc = (SurveillanceViewService.NodeRtc) itemId;
                return nodeRtc.getDownServiceCount() + " of " + nodeRtc.getDownServiceCount();
            }
        });

        addGeneratedColumn("availability", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, final Object itemId, Object columnId) {
                return ((SurveillanceViewService.NodeRtc) itemId).getAvailabilityAsString();
            }
        });

        setCellStyleGenerator(new CellStyleGenerator() {
            @Override
            public String getStyle(Table table, Object itemId, Object propertyId) {
                String style = null;
                SurveillanceViewService.NodeRtc nodeRtc = (SurveillanceViewService.NodeRtc) itemId;
                if (!"node".equals(propertyId)) {
                    if (nodeRtc.getAvailability() == 1.0) {
                        style = "normal-image";
                    } else {
                        style = "critical-image";
                    }
                }
                return style;
            }
        });

        setColumnHeader("node", "Node");
        setColumnHeader("currentOutages", "Current Outages");
        setColumnHeader("availability", "24 Hour Availability");

        setVisibleColumns(new Object[]{"node", "currentOutages", "availability"});
    }

    @Override
    public void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories) {
        List<SurveillanceViewService.NodeRtc> nodeRtcs = getSurveillanceViewService().getNoteRtcsForCategories(rowCategories, colCategories);

        m_beanItemContainer.removeAllItems();

        if (nodeRtcs != null && !nodeRtcs.isEmpty()) {
            for (SurveillanceViewService.NodeRtc nodeRtc : nodeRtcs) {
                m_beanItemContainer.addItem(nodeRtc);
            }
        }

        sort(new Object[]{"node"}, new boolean[]{true});

        refreshRowCache();
    }
}
