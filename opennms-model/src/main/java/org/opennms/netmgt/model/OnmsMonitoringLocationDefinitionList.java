package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OnmsMonitoringLocationDefinitionList extends LinkedList<OnmsMonitoringLocationDefinition> {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public OnmsMonitoringLocationDefinitionList() {
        super();
    }
    
    public OnmsMonitoringLocationDefinitionList(Collection<? extends OnmsMonitoringLocationDefinition> c) {
        super(c);
    }
    
    @XmlElement(name="locations")
    public List<OnmsMonitoringLocationDefinition> getDefinitions(){
        return this;
    }
    
    public void setDefinitions(List<OnmsMonitoringLocationDefinition> defs) {
        clear();
        addAll(defs);
    }
}
