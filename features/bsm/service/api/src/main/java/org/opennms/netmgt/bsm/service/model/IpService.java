package org.opennms.netmgt.bsm.service.model;

import java.util.Set;

import org.opennms.netmgt.model.OnmsSeverity;

public interface IpService {

    int getId();

    String getServiceName();

    String getNodeLabel();

    String getIpAddress();

    Set<String> getReductionKeys();

    OnmsSeverity getOperationalStatus();
}
