import org.opennms.features.topology.plugins.topo.graphml.GraphMLVertexStatus

assert alarmSummary != null

if (vertex.propagateStatus) {
    GraphMLVertexStatus status = new GraphMLVertexStatus()

    for (edge in vertex.edges) {
        status = GraphMLVertexStatus.merge(status, statusService.getStatus(edge.target))
    }

    return status

} else {
    return new GraphMLVertexStatus() \
        .severity(alarmSummary.maxSeverity)
        .alarmCount(alarmSummary.alarmCount)
}
