import org.opennms.features.topology.plugins.topo.graphml.status.GraphMLVertexStatus
import org.opennms.netmgt.model.OnmsSeverity


if (vertex.label == 'East') {
    return new GraphMLVertexStatus().severity(OnmsSeverity.CRITICAL).alarmCount(23)
}

if (vertex.label == 'South') {
    return new GraphMLVertexStatus().severity(OnmsSeverity.WARNING).alarmCount(42)
}
