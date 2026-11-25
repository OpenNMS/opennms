import org.opennms.netmgt.collection.api.AttributeType
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource
import org.opennms.netmgt.collection.support.builder.NodeLevelResource
import org.opennms.netmgt.telemetry.protocols.nxos.adapter.NxosGpbParserUtil
import org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis

// Used by SentinelResourceTypesIT: stores the idle value of every CPU row as attribute @METRIC@ of resource type
// smokeTestCpu. As long as Sentinel doesn't know smokeTestCpu, the resource keeps its name but uses the strategies of
// the shipped type nxosCpu.

TelemetryBis.Telemetry telemetryMsg = msg

if (telemetryMsg.getEncodingPath() == "show system resources") {
    def nodeLevelResource = new NodeLevelResource(agent.getNodeId())
    NxosGpbParserUtil.getRowsFromTable(telemetryMsg, "cpu_usage").each { row ->
        def cpuId = NxosGpbParserUtil.getValueFromRowAsString(row, "cpuid")
        def resource = new DeferredGenericTypeResource(nodeLevelResource, "smokeTestCpu", "nxosCpu", cpuId)
        builder.withNumericAttribute(resource, "smoke-test-cpu", "@METRIC@",
                NxosGpbParserUtil.getValueFromRowAsDouble(row, "idle"), AttributeType.GAUGE)
    }
}
