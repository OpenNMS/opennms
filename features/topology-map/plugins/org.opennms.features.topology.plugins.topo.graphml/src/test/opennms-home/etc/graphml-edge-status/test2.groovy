import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLEdgeStatus;

return new GraphMLEdgeStatus() \
    .severity(OnmsSeverity.NORMAL) \
    .style(["stroke": "pink"])
