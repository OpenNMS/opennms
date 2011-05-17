package org.opennms.netmgt.model;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OnmsMonitoredLocationAvailabilityDefinitionList extends LinkedList<OnmsMonitoredLocationAvailibilityDefinition> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public OnmsMonitoredLocationAvailabilityDefinitionList() {
        super();
    }
    
    

}
