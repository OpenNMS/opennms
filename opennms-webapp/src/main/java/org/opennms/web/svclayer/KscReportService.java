package org.opennms.web.svclayer;

import java.util.Map;

import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.model.OnmsResource;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface KscReportService {
    public Report buildNodeReport(int nodeId);
    public Report buildDomainReport(String domain);
    public OnmsResource getResourceFromGraph(Graph graph);
    public Map<String, String> getTimeSpans(boolean includeNone);
    public Map<Integer, String> getReportList();
}
