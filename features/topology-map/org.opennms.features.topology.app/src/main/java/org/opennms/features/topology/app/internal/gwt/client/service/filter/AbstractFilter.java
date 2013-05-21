package org.opennms.features.topology.app.internal.gwt.client.service.filter;

import java.util.Map;

import org.opennms.features.topology.app.internal.gwt.client.service.Filter;

public abstract class AbstractFilter implements Filter {

    @Override
    public abstract boolean match(Map<String, String> properties);

    @Override
    abstract public String toString();
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Filter) {
            Filter f = (Filter)o;
            return toString().equals(f.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
