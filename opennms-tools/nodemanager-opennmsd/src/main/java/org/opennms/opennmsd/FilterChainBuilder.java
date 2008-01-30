package org.opennms.opennmsd;

public class FilterChainBuilder {
    
    private FilterChain m_chain;
    private Filter m_currentFilter;
    
    public FilterChainBuilder() {
        m_chain = new FilterChain();
    }
    
    public FilterChain getChain() {
        return m_chain;
    }
    
    public FilterChainBuilder newFilter() {
        m_currentFilter = new Filter();
        m_chain.addFilter(m_currentFilter);
        return this;
    }
    
    public FilterChainBuilder setCategoryMatchPattern(String regexp) {
        m_currentFilter.setCategoryMatcher(regexp);
        return this;
    }
    
    public FilterChainBuilder setSeverityMatchPattern(String regexp) {
        m_currentFilter.setSeverityMatcher(regexp);
        return this;
    }
    
    public FilterChainBuilder setEventNameMatchPattern(String regexp) {
        m_currentFilter.setEventNameMatcher(regexp);
        return this;
    }
    
    public FilterChainBuilder setAddressMatchPattern(String iplikeStyleAddressPattern) {
        m_currentFilter.setAddressMatchSpec(iplikeStyleAddressPattern);
        return this;
    }
    
    public FilterChainBuilder setAction(String action) {
        m_currentFilter.setAction(action);
        return this;
    }

}
