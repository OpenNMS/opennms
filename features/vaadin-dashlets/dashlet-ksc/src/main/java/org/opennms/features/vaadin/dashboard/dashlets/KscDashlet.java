package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 09.10.13
 * Time: 22:29
 * To change this template use File | Settings | File Templates.
 */
public class KscDashlet extends VerticalLayout implements Dashlet {
    private NodeDao m_nodeDao;
    private ResourceDao m_resourceDao;
    private TransactionOperations m_transactionOperations;
    /**
     * the dashlet's name
     */
    private String m_name;

    /**
     * The {@link org.opennms.features.vaadin.dashboard.model.DashletSpec} for this instance
     */
    private DashletSpec m_dashletSpec;

    /**
     *
     */
    private GridLayout m_gridLayout;

    /**
     * Constructor for instantiating new objects.
     *
     * @param name        the name of the dashlet
     * @param dashletSpec the {@link DashletSpec} to be used
     */
    public KscDashlet(String name, DashletSpec dashletSpec, NodeDao nodeDao, ResourceDao resourceDao, TransactionOperations transactionOperations) {
        /**
         * Setting the member fields
         */
        m_name = name;
        m_dashletSpec = dashletSpec;
        m_nodeDao = nodeDao;
        m_resourceDao = resourceDao;
        m_transactionOperations = transactionOperations;

        /**
         * Setting up the layout
         */
        setCaption(getName());
        setSizeFull();

        /**
         * creating the grid layout
         */
        m_gridLayout = new GridLayout();
        m_gridLayout.setSizeFull();
        m_gridLayout.setColumns(1);
        m_gridLayout.setRows(1);

        addComponent(m_gridLayout);

        /**
         * initial update call
         */
        update();
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public boolean isBoosted() {
        return false;
    }

    /**
     * Updates the dashlet contents and computes new boosted state
     */
    @Override
    public void update() {
        /**
         * removing old components
         */
        m_gridLayout.removeAllComponents();

        /**
         * iniatizing the parameters
         */
        int columns = 0;
        int rows = 0;

        String kscReportName = m_dashletSpec.getParameters().get("kscReport");

        if (kscReportName == null || "".equals(kscReportName)) {
            return;
        }

        KSC_PerformanceReportFactory kscPerformanceReportFactory = KSC_PerformanceReportFactory.getInstance();

        Map<Integer, String> reportsMap = kscPerformanceReportFactory.getReportList();

        int kscReportId = -1;

        for (Map.Entry<Integer, String> entry : reportsMap.entrySet()) {

            if (kscReportName.equals(entry.getValue())) {
                kscReportId = entry.getKey();
                break;
            }
        }

        if (kscReportId == -1) {
            return;
        }

        Report kscReport = kscPerformanceReportFactory.getReportByIndex(kscReportId);

        columns = kscReport.getGraphs_per_line();

        if (columns == 0) {
            columns = 1;
        }

        rows = kscReport.getGraphCount() / columns;

        if (rows == 0) {
            rows = 1;
        }

        if (kscReport.getGraphCount() % columns > 0) {
            rows++;
        }

        int width = 0;
        int height = 0;

        /*

        try {
            width = Integer.parseInt(m_dashletSpec.getParameters().get("width"));
        } catch (NumberFormatException numberFormatException) {
            width = 400;
        }

        try {
            height = Integer.parseInt(m_dashletSpec.getParameters().get("height"));
        } catch (NumberFormatException numberFormatException) {
            height = 100;
        }
         */

        /**
         * setting new columns/rows
         */
        m_gridLayout.setColumns(columns);
        m_gridLayout.setRows(rows);

        int i = 0;

        /**
         * adding the components
         */

        for (int y = 0; y < m_gridLayout.getRows(); y++) {
            for (int x = 0; x < m_gridLayout.getColumns(); x++) {

                if (i < kscReport.getGraphCount()) {
                    Graph graph = kscReport.getGraph(i);

                    Map<String, String> data = getDataForResourceId(graph.getNodeId(), graph.getResourceId());

                    Calendar beginTime = Calendar.getInstance();
                    Calendar endTime = Calendar.getInstance();

                    KSC_PerformanceReportFactory.getBeginEndTime(graph.getTimespan(), beginTime, endTime);

                    String urlString = "/opennms/graph/graph.png?resourceId=" + graph.getResourceId() + "&report=" + graph.getGraphtype() + "&start=" + beginTime.getTimeInMillis() + "&end=" + endTime.getTimeInMillis() + (width > 0 ? "&width=" + width : "") + (height > 0 ? "&height=" + height : "");

                    Image image = new Image(null, new ExternalResource(urlString));
                    VerticalLayout verticalLayout = new VerticalLayout();
                    verticalLayout.addComponent(new Label(data.get("nodeLabel")));
                    verticalLayout.addComponent(new Label(data.get("resourceTypeLabel") + ": " + data.get("resourceLabel")));
                    verticalLayout.addComponent(image);
                    m_gridLayout.addComponent(verticalLayout, x, y);
                    m_gridLayout.setComponentAlignment(verticalLayout, Alignment.MIDDLE_CENTER);
                }
                i++;
            }
        }
    }

    public Map<String, String> getDataForResourceId(final String nodeId, final String resourceId) {
        return (Map<String, String>) m_transactionOperations.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                Map<String, String> data = new HashMap<String, String>();

                if (nodeId == null) {
                    String arr0[] = resourceId.split("\\.");
                    String arr1[] = arr0[0].split("[\\[\\]]");
                    data.put("nodeId", arr1[1]);
                } else {
                    data.put("nodeId", nodeId);
                }

                data.put("nodeLabel", m_nodeDao.getLabelForId(Integer.valueOf(data.get("nodeId"))));

                List<OnmsResource> resourceList = m_resourceDao.getResourceById("node[" + data.get("nodeId") + "]").getChildResources();

                for (OnmsResource onmsResource : resourceList) {
                    if (resourceId.equals(onmsResource.getId())) {
                        data.put("resourceLabel", onmsResource.getLabel());
                        data.put("resourceTypeLabel", onmsResource.getResourceType().getLabel());

                        break;
                    }
                }
                return data;
            }
        });
    }
}
