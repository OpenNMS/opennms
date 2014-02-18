package org.opennms.netmgt.config.agents;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="agents")
public class AgentResponseCollection extends ArrayList<AgentResponse> {
    private static final long serialVersionUID = 6911620042375097464L;
    public AgentResponseCollection() {
        super();
    }
    public AgentResponseCollection(final List<AgentResponse> responses) {
        super();
        addAll(responses);
    }
    @XmlElement(name="agent")
    public List<AgentResponse> getAgents() {
        return this;
    }
    public void setAgents(final List<AgentResponse> agents) {
        if (agents == this) {
            return;
        }
        clear();
        addAll(agents);
    }
}