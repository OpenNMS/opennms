/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.notification.filter;

import org.opennms.web.filter.Filter;
import org.opennms.web.notification.AcknowledgeType;
import org.opennms.web.notification.SortStyle;

public class NotificationCriteria {
    
    public static interface NotificationCriteriaVisitor<E extends Exception>{
        public void visitAckType(AcknowledgeType ackType) throws E;
        public void visitFilter(Filter filter) throws E;
        public void visitSortStyle(SortStyle sortStyle) throws E;
        public void visitLimit(int limit, int offset) throws E;
    }
    
    public static class BaseNotificationCriteriaVisitor<E extends Exception> implements NotificationCriteriaVisitor<E>{
        public void visitAckType(AcknowledgeType ackType) throws E { }
        public void visitFilter(Filter filter) throws E { }
        public void visitLimit(int limit, int offset) throws E { }
        public void visitSortStyle(SortStyle sortStyle) throws E { }
    }
    
    Filter[] m_filters = null;
    SortStyle m_sortStyle = SortStyle.ID;
    AcknowledgeType m_ackType = AcknowledgeType.UNACKNOWLEDGED;
    int m_limit = -1;
    int m_offset = -1;
    
    public NotificationCriteria(Filter...filters){
        this(filters, null, null, -1, -1);
    }
    
    public NotificationCriteria(Filter[] filters, SortStyle sortStyle, AcknowledgeType ackType, int limit, int offset){
        m_filters = filters;
        m_sortStyle = sortStyle;
        m_ackType = ackType;
        m_limit = limit;
        m_offset = limit;
    }
    
    public NotificationCriteria(AcknowledgeType ackType, Filter[] filters) {
        this(filters, null, ackType, -1, -1);
    }

    public <E extends Exception> void visit(NotificationCriteriaVisitor<E> visitor) throws E{
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
                                                  
}