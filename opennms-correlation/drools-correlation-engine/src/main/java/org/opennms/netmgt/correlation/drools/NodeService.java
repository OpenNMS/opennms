package org.opennms.netmgt.correlation.drools;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface NodeService {
    
    public Long getParentNode( Long node );

}
