import org.opennms.netmgt.model.OnmsSeverity
import org.opennms.features.topology.plugins.topo.graphml.status.GraphMLEdgeStatus

assert edge != null

assert sourceNode != null
assert sourceNode.label == "node1"

assert targetNode != null
assert targetNode.label == "node2"

assert measurements != null;

assert nodeDao != null;
assert snmpInterfaceDao != null;


return new GraphMLEdgeStatus() \
    .severity(OnmsSeverity.WARNING) \
    .style(["stroke-width": "3em"])
