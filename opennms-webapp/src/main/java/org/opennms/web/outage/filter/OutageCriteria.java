package org.opennms.web.outage.filter;

import org.opennms.web.outage.OutageType;
import org.opennms.web.outage.SortStyle;

public class OutageCriteria {

    public static interface OutageCriteriaVisitor<E extends Exception> {
        public void visitOutageType(OutageType ackType) throws E; 
        public void visitFilter(Filter filter) throws E;
        public void visitSortStyle(SortStyle sortStyle) throws E;
        public void visitGroupBy() throws E;
        public void visitLimit(int limit, int offset) throws E;
    }

    public static class BaseOutageCriteriaVisitor<E extends Exception> implements OutageCriteriaVisitor<E> {
        public void visitOutageType(OutageType ackType) throws E { }
        public void visitFilter(Filter filter) throws E { }
        public void visitLimit(int limit, int offset) throws E { }
        public void visitGroupBy() throws E { }
        public void visitSortStyle(SortStyle sortStyle) throws E { }
    }
    
    Filter[] m_filters = null;
    SortStyle m_sortStyle = SortStyle.DEFAULT_SORT_STYLE;
    OutageType m_outageType = OutageType.CURRENT;
    String m_groupBy = null;
    int m_limit = -1;
    int m_offset = -1;
    
    public OutageCriteria(Filter... filters) {
        this(filters, null, null, -1, -1);
    }
    
    public OutageCriteria(OutageType outageType, Filter[] filters) {
        this(filters, null, outageType, -1, -1);
    }

    public OutageCriteria(Filter[] filters, SortStyle sortStyle, OutageType outageType, int limit, int offset) {
        m_filters = filters;
        m_sortStyle = sortStyle;
        m_outageType = outageType;
        m_limit = limit;
        m_offset = offset;
    }
    
    
    public <E extends Exception> void visit(OutageCriteriaVisitor<E> visitor) throws E {
        if (m_outageType != null) {
            visitor.visitOutageType(m_outageType);
        }

        for(Filter filter : m_filters) {
            visitor.visitFilter(filter);
        }

        visitor.visitGroupBy();
        
        if (m_sortStyle != null) {
            visitor.visitSortStyle(m_sortStyle);
        }

        if (m_limit > 0 && m_offset > -1) {
            visitor.visitLimit(m_limit, m_offset);
        }
    }
}
