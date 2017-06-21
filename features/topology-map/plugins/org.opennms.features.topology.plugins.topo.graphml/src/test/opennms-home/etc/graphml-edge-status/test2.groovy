import org.opennms.netmgt.model.OnmsSeverity
import org.opennms.features.topology.plugins.topo.graphml.GraphMLEdgeStatus

return new GraphMLEdgeStatus() \
    .severity(OnmsSeverity.NORMAL) \
    .style(["stroke": "pink"])
