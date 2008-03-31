/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: April 14, 2007
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

import org.opennms.netmgt.model.AttributeStatistic;
import org.opennms.netmgt.model.AttributeStatisticVisitorWithResults;
import org.opennms.netmgt.model.OnmsAttribute;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class BottomNAttributeStatisticVisitor implements AttributeStatisticVisitorWithResults, InitializingBean {

    private Integer m_count;
    private SortedSet<AttributeStatistic> m_results = new TreeSet<AttributeStatistic>(new AttributeStatisticComparator());
    protected Comparator<Double> m_comparator = new DoubleComparator();

    public BottomNAttributeStatisticVisitor() {
        super();
    }

    /**
     * @see org.opennms.netmgt.model.AttributeStatisticVisitor#visit(org.opennms.netmgt.model.OnmsAttribute, double)
     */
    public void visit(OnmsAttribute attribute, double statistic) {
        Assert.notNull(attribute, "attribute argument must not be null");
        
        m_results.add(new AttributeStatistic(attribute, statistic));
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
    public SortedSet<AttributeStatistic> getResults() {
        SortedSet<AttributeStatistic> top = new TreeSet<AttributeStatistic>(new AttributeStatisticComparator());
        
        for (AttributeStatistic stat : m_results) {
            top.add(stat);
            
            if (top.size() >= m_count) {
                break;
            }
        }
        
        return top;
    }

    public Comparator<Double> getComparator() {
        return m_comparator;
    }

    public void setComparator(Comparator<Double> comparator) {
        m_comparator = comparator;
    }

    public class AttributeStatisticComparator implements Comparator<AttributeStatistic> {
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(AttributeStatistic o1, AttributeStatistic o2) {
            int diff;
            
            diff = getComparator().compare(o1.getStatistic(), o2.getStatistic()); 
            if (diff != 0) {
                return diff;
            }
            
            diff = o1.getAttribute().getResource().getId().compareToIgnoreCase(o2.getAttribute().getResource().getId());
            if (diff != 0) {
                return diff;
            }
            
            return new Integer(o1.getAttribute().hashCode()).compareTo(o2.getAttribute().hashCode());
        }
    }
    
    public class DoubleComparator implements Comparator<Double> {
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Double o1, Double o2) {
            return o1.compareTo(o2);
        }
    }

}