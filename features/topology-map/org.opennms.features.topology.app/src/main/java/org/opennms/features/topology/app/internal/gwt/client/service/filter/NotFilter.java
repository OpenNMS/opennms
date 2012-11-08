package org.opennms.features.topology.app.internal.gwt.client.service.filter;

import java.util.Map;

import org.opennms.features.topology.app.internal.gwt.client.service.Filter;

public class NotFilter extends AbstractFilter {

    Filter m_filter;

    public NotFilter(Filter filter) {
        m_filter = filter;
    }

    @Override
    public boolean match(Map<String, String> properties) {
        return !m_filter.match(properties);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("(!").append(m_filter).append(")").toString();
    }

}
