/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.event.filter;

import java.util.Arrays;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.web.event.AcknowledgeType;
import org.opennms.web.event.SortStyle;
import org.opennms.web.filter.Filter;

/**
 * <p>EventCriteria class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class EventCriteria {
    
    public static interface EventCriteriaVisitor<E extends Exception>{
        public void visitAckType(AcknowledgeType ackType) throws E;
        public void visitFilter(Filter filter) throws E;
        public void visitSortStyle(SortStyle sortStyle) throws E;
        public void visitLimit(int limit, int offset) throws E;
    }
    
    public static class BaseEventCriteriaVisitor<E extends Exception> implements EventCriteriaVisitor<E>{
        @Override
        public void visitAckType(AcknowledgeType ackType) throws E { }
        @Override
        public void visitFilter(Filter filter) throws E { }
        @Override
        public void visitLimit(int limit, int offset) throws E { }
        @Override
        public void visitSortStyle(SortStyle sortStyle) throws E { }
        
    }
    
    Filter[] m_filters = null;
    SortStyle m_sortStyle = SortStyle.TIME;
    AcknowledgeType m_ackType = AcknowledgeType.UNACKNOWLEDGED;
    int m_offset = -1;
    int m_limit = -1;
    
    /**
     * <p>Constructor for EventCriteria.</p>
     *
     * @param filters a org$opennms$web$filter$Filter object.
     */
    public EventCriteria(Filter... filters){
        this(filters, null, null, -1, -1);
    }
    
    /**
     * <p>Constructor for EventCriteria.</p>
     *
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     */
    public EventCriteria(AcknowledgeType ackType, Filter[] filters) {
        this(filters, null, ackType, -1, -1);
    }

    /**
     * <p>Constructor for EventCriteria.</p>
     *
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @param limit a int.
     * @param offset a int.
     */
    public EventCriteria(Filter[] filters, SortStyle sortStyle, AcknowledgeType ackType, int limit, int offset){
        m_filters = filters;
        m_sortStyle = sortStyle;
        m_ackType = ackType;
        m_limit = limit;
        m_offset = offset;
    }
    
    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.web.event.filter.EventCriteria.EventCriteriaVisitor} object.
     * @param <E> a E object.
     * @throws E if any.
     */
    public <E extends Exception> void visit(EventCriteriaVisitor<E> visitor) throws E{
        if(m_ackType != null){
            visitor.visitAckType(m_ackType);
        }
        for(Filter filter : m_filters){
            visitor.visitFilter(filter);
        }
        if(m_sortStyle != null){
            visitor.visitSortStyle(m_sortStyle);
        }
        if(m_limit > 0 && m_offset > -1){
            visitor.visitLimit(m_limit, m_offset);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("filters", Arrays.asList(m_filters))
            .append("sortStyle", m_sortStyle)
            .append("ackType", m_ackType)
            .append("limit", m_limit)
            .append("offset", m_offset)
            .toString();
    }
}
