/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 05: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.support;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opennms.netmgt.model.AttributeStatisticVisitor;
import org.opennms.netmgt.model.OnmsAttribute;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class TopNAttributeStatisticVisitor implements AttributeStatisticVisitor, InitializingBean {
    private Integer m_count;
    private SortedSet<AttributeStatistic> m_topN = new TreeSet<AttributeStatistic>();
    private Comparator<Double> m_comparator = new ReverseDoubleComparator();
    
    /**
     * @see org.opennms.netmgt.model.AttributeStatisticVisitor#visit(org.opennms.netmgt.model.OnmsAttribute, double)
     */
    public void visit(OnmsAttribute attribute, double statistic) {
        Assert.notNull(attribute, "attribute argument must not be null");
        
        m_topN.add(new AttributeStatistic(attribute, statistic));
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() {
        Assert.state(m_count != null, "property count must be set to a non-null value");
    }

    public Integer getCount() {
        return m_count;
    }

    public void setCount(Integer count) {
        m_count = count;
    }

    /**
     * @return top attribute statistics (up to getCount() number)
     */
    public SortedSet<AttributeStatistic> getTopN() {
        SortedSet<AttributeStatistic> top = new TreeSet<AttributeStatistic>();
        
        for (AttributeStatistic stat : m_topN) {
            top.add(stat);
            
            if (top.size() >= m_count) {
                break;
            }
        }
        
        return top;
    }
    
    public class AttributeStatistic implements Comparable<AttributeStatistic> {
        private OnmsAttribute m_attribute;
        private Double m_statistic;
        
        public AttributeStatistic(OnmsAttribute attribute, Double statistic) {
            m_attribute = attribute;
            m_statistic = statistic;
        }
        
        public OnmsAttribute getAttribute() {
            return m_attribute;
        }
        
        public Double getStatistic() {
            return m_statistic;
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(AttributeStatistic o) {
            int diff;
            
            diff = m_comparator.compare(getStatistic(), o.getStatistic()); 
            if (diff != 0) {
                return diff;
            }
            
            diff = getAttribute().getResource().getId().compareToIgnoreCase(o.getAttribute().getResource().getId());
            if (diff != 0) {
                return diff;
            }
            
            return new Integer(getAttribute().hashCode()).compareTo(o.getAttribute().hashCode());
        }
    }
    
    private class ReverseDoubleComparator implements Comparator<Double> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Double o1, Double o2) {
            return o2.compareTo(o1);
        }
    }
}
