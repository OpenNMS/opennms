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
package org.opennms.web.notification.filter;

import org.opennms.web.filter.Filter;
import org.opennms.web.notification.AcknowledgeType;
import org.opennms.web.notification.SortStyle;

/**
 * <p>NotificationCriteria class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NotificationCriteria {
    
    public static interface NotificationCriteriaVisitor<E extends Exception>{
        public void visitAckType(AcknowledgeType ackType) throws E;
        public void visitFilter(Filter filter) throws E;
        public void visitSortStyle(SortStyle sortStyle) throws E;
        public void visitLimit(int limit, int offset) throws E;
    }
    
    public static class BaseNotificationCriteriaVisitor<E extends Exception> implements NotificationCriteriaVisitor<E>{
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
    SortStyle m_sortStyle = SortStyle.ID;
    AcknowledgeType m_ackType = AcknowledgeType.UNACKNOWLEDGED;
    int m_limit = -1;
    int m_offset = -1;
    
    /**
     * <p>Constructor for NotificationCriteria.</p>
     *
     * @param filters a org$opennms$web$filter$Filter object.
     */
    public NotificationCriteria(Filter...filters){
        this(filters, null, null, -1, -1);
    }
    
    /**
     * <p>Constructor for NotificationCriteria.</p>
     *
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @param sortStyle a {@link org.opennms.web.notification.SortStyle} object.
     * @param ackType a {@link org.opennms.web.notification.AcknowledgeType} object.
     * @param limit a int.
     * @param offset a int.
     */
    public NotificationCriteria(Filter[] filters, SortStyle sortStyle, AcknowledgeType ackType, int limit, int offset){
        m_filters = filters;
        m_sortStyle = sortStyle;
        m_ackType = ackType;
        m_limit = limit;
        m_offset = offset;
    }
    
    /**
     * <p>Constructor for NotificationCriteria.</p>
     *
     * @param ackType a {@link org.opennms.web.notification.AcknowledgeType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     */
    public NotificationCriteria(AcknowledgeType ackType, Filter[] filters) {
        this(filters, null, ackType, -1, -1);
    }

    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.web.notification.filter.NotificationCriteria.NotificationCriteriaVisitor} object.
     * @param <E> a E object.
     * @throws E if any.
     */
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
