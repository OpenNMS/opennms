package org.opennms.netmgt.bsm.service.model;

import java.util.Set;

public interface IpService {

    int getId();

    String getServiceName();

    String getNodeLabel();

    String getIpAddress();

    Set<String> getReductionKeys();
}
