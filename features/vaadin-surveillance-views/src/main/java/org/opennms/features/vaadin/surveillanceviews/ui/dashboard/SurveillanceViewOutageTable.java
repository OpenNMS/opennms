package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import org.opennms.features.vaadin.surveillanceviews.service.NodeRtc;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;

import java.util.List;
import java.util.Set;

public class SurveillanceViewOutageTable extends SurveillanceViewDetailTable {
    private BeanItemContainer<NodeRtc> m_beanItemContainer = new BeanItemContainer<NodeRtc>(NodeRtc.class);

    public SurveillanceViewOutageTable(SurveillanceViewService surveillanceViewService) {
        super("Outages", surveillanceViewService);

        setContainerDataSource(m_beanItemContainer);

        addStyleName("surveillance-view");

/*
        addGeneratedColumn("node", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, final Object itemId, Object columnId) {
                Label label = new Label(((NodeRtc) itemId).getNode().getLabel());
                label.setSizeFull();
                label.setPrimaryStyleName("white");
                return label;
            }
        });
*/

        addGeneratedColumn("node", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, Object itemId, Object propertyId) {
                NodeRtc nodeRtc = (NodeRtc) itemId;
                Link link = new Link(nodeRtc.getNode().getLabel(), new ExternalResource("/opennms/element/node.jsp?node=" + nodeRtc.getNode().getNodeId()));
                link.setPrimaryStyleName("surveillance-view");
                link.addStyleName("white");
                return link;
            }
        });


        addGeneratedColumn("currentOutages", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, final Object itemId, Object columnId) {
                NodeRtc nodeRtc = (NodeRtc) itemId;
                return nodeRtc.getDownServiceCount() + " of " + nodeRtc.getDownServiceCount();
            }
        });

        addGeneratedColumn("availability", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, final Object itemId, Object columnId) {
                return ((NodeRtc) itemId).getAvailabilityAsString();
            }
        });


        setCellStyleGenerator(new CellStyleGenerator() {
            @Override
            public String getStyle(Table table, Object itemId, Object propertyId) {
                String style = null;
                NodeRtc nodeRtc = (NodeRtc) itemId;
                if (!"node".equals(propertyId)) {
                    if (nodeRtc.getAvailability() == 1.0) {
                        style = "normal-image";
                    } else {
                        style = "critical-image";
                    }
                } else {
                    //style = "white";
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

        m_beanItemContainer.removeAllItems();

        if (nodes != null && nodes.size() > 0) {
            List<NodeRtc> nodeRtcList = getSurveillanceViewService().getRtcList(nodes);
            for (NodeRtc nodeRtc : nodeRtcList) {
                m_beanItemContainer.addItem(nodeRtc);
            }
        }

        sort(new Object[]{"node"}, new boolean[]{true});
    }
}
