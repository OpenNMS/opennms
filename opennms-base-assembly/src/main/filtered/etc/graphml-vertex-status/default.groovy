import org.opennms.features.topology.plugins.topo.graphml.GraphMLVertexStatus

assert alarmSummary != null

return new GraphMLVertexStatus() \
    .severity(alarmSummary.maxSeverity)
    .alarmCount(alarmSummary.alarmCount)
