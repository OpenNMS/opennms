/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.event.filter;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.web.event.AcknowledgeType;
import org.opennms.web.event.EventQueryParms;
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
     * @Deprecated use {@link #EventCriteria(Filter[], AcknowledgeType)} instead.
     */
    @Deprecated
    public EventCriteria(AcknowledgeType ackType, Filter[] filters) {
        this(filters, null, ackType, -1, -1);
    }
    
    public EventCriteria(Filter[] filters, AcknowledgeType ackType) {
    	this(filters, null, ackType, -1, -1);
    }
    
    public EventCriteria(List<Filter> filterList, AcknowledgeType ackType) {
    	this (filterList == null ? new Filter[0] : filterList.toArray(new Filter[filterList.size()]), ackType);
    }
    
    public EventCriteria(List<Filter> filterList, SortStyle sortStyle, AcknowledgeType ackType, int limit, int offset) {
    	this(filterList == null ? new Filter[0] : filterList.toArray(new Filter[filterList.size()]), sortStyle, ackType, limit, offset);
    }
    
    public EventCriteria(EventQueryParms parms) {
    	this(parms.filters, parms.sortStyle, parms.ackType, parms.limit, parms.limit * parms.multiple);
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
