package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OnmsLocationAvailDefinitionList extends LinkedList<OnmsLocationAvailDataPoint> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public OnmsLocationAvailDefinitionList() {
        super();
    }
    
    public OnmsLocationAvailDefinitionList(Collection<? extends OnmsLocationAvailDataPoint> c) {
        super(c);
    }
    
    @XmlElement(name="data")
    public List<OnmsLocationAvailDataPoint> getDefinitions(){
        return this;
    }
    
    public void setDefinitions(List<OnmsLocationAvailDataPoint> defs) {
        clear();
        addAll(defs);
    }

}
