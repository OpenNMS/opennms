package org.opennms.netmgt.provision;

import java.util.List;

public interface ReverseDnsProvisioningAdapterService {
    
    List<ReverseDnsRecord> get(Integer nodeid);
    
    void update(Integer nodeid, ReverseDnsRecord rdr);

}
