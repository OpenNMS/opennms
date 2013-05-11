package org.opennms.features.topology.app.internal.gwt.client.service;

import java.util.Map;

public interface Filter {
    
    public boolean match(Map<String, String> properties);

    @Override
    public boolean equals(Object obj);
    
    @Override
    public int hashCode();
    
    @Override
    public String toString();

}
