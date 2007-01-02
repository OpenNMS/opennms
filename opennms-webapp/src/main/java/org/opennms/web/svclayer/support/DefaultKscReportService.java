package org.opennms.web.svclayer.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.config.kscReports.ReportsList;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.svclayer.KscReportService;
import org.opennms.web.svclayer.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;

public class DefaultKscReportService implements KscReportService, InitializingBean {
    
    private ResourceService m_resourceService;
    private KSC_PerformanceReportFactory m_kscReportFactory;

    private static final LinkedHashMap<String, String> s_timeSpans = new LinkedHashMap<String, String>();
    private static final LinkedHashMap<String, String> s_timeSpansWithNone = new LinkedHashMap<String, String>();

    public Report buildDomainReport(String domain) {
        String resourceId = OnmsResource.createResourceId("domain", domain);
        OnmsResource node = getResourceService().getResourceById(resourceId);
        return buildResourceReport(node, "Domain Report for Domain " + domain);
    }

    public Report buildNodeReport(int node_id) {
        String resourceId = OnmsResource.createResourceId("node", Integer.toString(node_id));
        OnmsResource node = getResourceService().getResourceById(resourceId);
        return buildResourceReport(node, "Node Report for Node Number " + node_id);
    }
    
    private Report buildResourceReport(OnmsResource parentResource, String title) {
        Report report = new Report();
        report.setTitle(title);
        report.setShow_timespan_button(true);
        report.setShow_graphtype_button(true);

        List<OnmsResource> resources = getResourceService().findChildResources(parentResource, "interfaceSnmp");
        for (OnmsResource resource : resources) {
            PrefabGraph[] graphs = getResourceService().findPrefabGraphsForResource(resource);
            if (graphs.length == 0) {
                continue;
            }
            
            Graph graph = new Graph();
            graph.setTitle("");
            graph.setResourceId(resource.getId());
            graph.setTimespan("7_day");
            graph.setGraphtype(graphs[0].getName());
            
            report.addGraph(graph);
        }
        return report;
    }


    public OnmsResource getResourceFromGraph(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph argument cannot be null");
        }
        
        String resourceId;
        if (graph.getResourceId() != null) {
            resourceId = graph.getResourceId();
        } else {
            String parentResourceTypeName;
            String parentResourceName;
            String resourceTypeName;
            String resourceName;
            
            if (graph.getNodeId() != null && !graph.getNodeId().equals("null")) {
                parentResourceTypeName = "node";
                parentResourceName = graph.getNodeId();
            } else {
                parentResourceTypeName = "domain";
                parentResourceName = graph.getDomain();
            }
            
            String intf = graph.getInterfaceId();
            if (intf == null || "".equals(intf)) {
                resourceTypeName = "nodeSnmp";
                resourceName = "";
            } else {
                resourceTypeName = "interfaceSnmp";
                resourceName = intf;
            }
    
            resourceId = OnmsResource.createResourceId(parentResourceTypeName, parentResourceName, resourceTypeName, resourceName);
        }
        
        return getResourceService().getResourceById(resourceId);
    }
    

    private void initTimeSpans() {
        for (String timeSpan : getKscReportFactory().timespan_options) {
            s_timeSpans.put(timeSpan, timeSpan);
        }
        
        s_timeSpansWithNone.put("none", "none");
        s_timeSpansWithNone.putAll(s_timeSpans);
    }

    public Map<String, String> getTimeSpans(boolean includeNone) {
        if (includeNone) {
            return s_timeSpansWithNone;
        } else {
            return s_timeSpans;
        }
    }
    
    public Map<Integer, String> getReportList() {
        ReportsList report_configuration = KSC_PerformanceReportFactory.getConfiguration();  
        if (report_configuration == null) {
            throw new DataAccessResourceFailureException("Couldn't retrieve KSC Report File configuration");
        }
        
        Report[] report_array = report_configuration.getReport();
        
        LinkedHashMap<Integer, String> reports = new LinkedHashMap<Integer, String>();
        for (int i = 0; i < report_configuration.getReportCount(); i++ ) {
            reports.put(i, report_array[i].getTitle());
        }
        
        return reports;
    }

    public ResourceService getResourceService() {
        return m_resourceService;
    }

    public void setResourceService(ResourceService resourceService) {
        m_resourceService = resourceService;
    }

    public KSC_PerformanceReportFactory getKscReportFactory() {
        return m_kscReportFactory;
    }

    public void setKscReportFactory(KSC_PerformanceReportFactory kscReportFactory) {
        m_kscReportFactory = kscReportFactory;
    }

    public void afterPropertiesSet() throws Exception {
        if (m_resourceService == null) {
            throw new IllegalStateException("resourceService property has not been set");
        }
        if (m_kscReportFactory == null) {
            throw new IllegalStateException("kscReportFactory property has not been set");
        }
        
        initTimeSpans();
    }

}
