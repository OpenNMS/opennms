package org.opennms.web.rest.config;

import java.util.List;

import javax.ws.rs.core.GenericEntity;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.agents.AgentResponse;

@XmlRootElement(name="agents")
final class AgentEntity extends GenericEntity<List<AgentResponse>> {
    public AgentEntity() {
        super(null);
    }

    protected AgentEntity(List<AgentResponse> entity) {
        super(entity);
    }
    
}