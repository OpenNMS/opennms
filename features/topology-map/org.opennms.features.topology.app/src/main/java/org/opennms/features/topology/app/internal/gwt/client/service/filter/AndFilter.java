package org.opennms.features.topology.app.internal.gwt.client.service.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.app.internal.gwt.client.service.Filter;

public class AndFilter extends AbstractFilter {
    
    List<Filter> m_filters;

    public AndFilter(List<Filter> filters) {
        m_filters = filters;
    }
    
    public AndFilter(Filter... filters) {
        this(Arrays.asList(filters));
    }

    @Override
    public boolean match(Map<String, String> properties) {
        for(Filter f : m_filters) {
            if (!f.match(properties)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("(&");
        for(Filter f : m_filters) {
            buf.append(f);
        }
        buf.append(")");
        return buf.toString();
    }

}
