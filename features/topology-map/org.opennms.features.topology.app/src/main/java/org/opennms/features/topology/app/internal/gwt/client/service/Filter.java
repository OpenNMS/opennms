package org.opennms.features.topology.app.internal.gwt.client.service;

import java.util.Map;

public interface Filter {
    
    public boolean match(Map<String, String> properties);

    public boolean equals(Object obj);
    
    public int hashCode();
    
    public String toString();

}
