package org.opennms.netmgt.snmp.proxy.common;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

public class SNMPSetBuilder extends AbstractSNMPRequestBuilder<SnmpValue> {

    public SNMPSetBuilder(LocationAwareSnmpClientRpcImpl client, SnmpAgentConfig agent, List<SnmpObjId> oids, List<SnmpValue> values) {
        super(client, agent, Collections.emptyList(), Collections.emptyList(), buildGetRequests(oids, values));
    }

    private static List<SnmpSetRequestDTO> buildGetRequests(List<SnmpObjId> oids, List<SnmpValue> values) {
        final SnmpSetRequestDTO setRequest = new SnmpSetRequestDTO();
        setRequest.setOids(oids);
        setRequest.setValues(values);
        return Collections.singletonList(setRequest);
    }

    @Override
    protected SnmpValue processResponse(SnmpMultiResponseDTO response) {
        return response.getResponses().stream()
                .flatMap(res -> res.getResults().stream())
                .findFirst()
                .map(SnmpResult::getValue)
                .orElse(null);
    }
}
