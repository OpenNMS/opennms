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

package org.opennms.web.alarm.filter;

import org.opennms.web.alarm.AcknowledgeType;
import org.opennms.web.alarm.SortStyle;
import org.opennms.web.filter.Filter;


/**
 * AlarmCritiera
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public class AlarmCriteria {
    
    public static final int NO_LIMIT = -1;
    public static final int NO_OFFSET = -1;
    
    public static interface AlarmCriteriaVisitor<E extends Exception> {
        public void visitAckType(AcknowledgeType ackType) throws E; 
        public void visitFilter(Filter filter) throws E;
        public void visitSortStyle(SortStyle sortStyle) throws E;
        public void visitLimit(int limit, int offset) throws E;
    }
    
    public static class BaseAlarmCriteriaVisitor<E extends Exception> implements AlarmCriteriaVisitor<E> {
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
    SortStyle m_sortStyle = SortStyle.LASTEVENTTIME;
    AcknowledgeType m_ackType = AcknowledgeType.UNACKNOWLEDGED;
    int m_limit = NO_LIMIT;
    int m_offset = NO_OFFSET;
    
    /**
     * <p>Constructor for AlarmCriteria.</p>
     *
     * @param filters a org$opennms$web$filter$Filter object.
     */
    public AlarmCriteria(Filter... filters) {
        this(filters, null, null, NO_LIMIT, NO_OFFSET);
    }
    
    /**
     * <p>Constructor for AlarmCriteria.</p>
     *
     * @param ackType a {@link org.opennms.web.alarm.AcknowledgeType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     */
    public AlarmCriteria(AcknowledgeType ackType, Filter[] filters) {
        this(filters, null, ackType, NO_LIMIT, NO_OFFSET);
    }
    
    /**
     * <p>Constructor for AlarmCriteria.</p>
     *
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @param sortStyle a {@link org.opennms.web.alarm.SortStyle} object.
     * @param ackType a {@link org.opennms.web.alarm.AcknowledgeType} object.
     * @param limit a int.
     * @param offset a int.
     */
    public AlarmCriteria(Filter[] filters, SortStyle sortStyle, AcknowledgeType ackType, int limit, int offset) {
        m_filters = filters;
        m_sortStyle = sortStyle;
        m_ackType = ackType;
        m_limit = limit;
        m_offset = offset;
    }
    
    
    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.web.alarm.filter.AlarmCriteria.AlarmCriteriaVisitor} object.
     * @param <E> a E object.
     * @throws E if any.
     */
    public <E extends Exception> void visit(AlarmCriteriaVisitor<E> visitor) throws E {
        if (m_ackType != null) {
            visitor.visitAckType(m_ackType);
        }
        for(Filter filter : m_filters) {
            visitor.visitFilter(filter);
        }
        if (m_sortStyle != null) {
            visitor.visitSortStyle(m_sortStyle);
        }
        if (m_limit > 0 && m_offset > -1) {
            visitor.visitLimit(m_limit, m_offset);
        }
    }

}
