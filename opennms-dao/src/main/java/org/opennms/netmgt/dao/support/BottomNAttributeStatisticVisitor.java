/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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
 * <p>BottomNAttributeStatisticVisitor class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class BottomNAttributeStatisticVisitor implements AttributeStatisticVisitorWithResults, InitializingBean {

    private Integer m_count;
    private SortedSet<AttributeStatistic> m_results = new TreeSet<AttributeStatistic>(new AttributeStatisticComparator());
    protected Comparator<Double> m_comparator = new DoubleComparator();

    /**
     * <p>Constructor for BottomNAttributeStatisticVisitor.</p>
     */
    public BottomNAttributeStatisticVisitor() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public void visit(OnmsAttribute attribute, double statistic) {
        Assert.notNull(attribute, "attribute argument must not be null");
        
        m_results.add(new AttributeStatistic(attribute, statistic));
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_count != null, "property count must be set to a non-null value");
    }

    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getCount() {
        return m_count;
    }

    /** {@inheritDoc} */
    @Override
    public void setCount(Integer count) {
        m_count = count;
    }

    /**
     * <p>getResults</p>
     *
     * @return top attribute statistics (up to getCount() number)
     */
    @Override
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

    /**
     * <p>getComparator</p>
     *
     * @return a {@link java.util.Comparator} object.
     */
    public Comparator<Double> getComparator() {
        return m_comparator;
    }

    /**
     * <p>setComparator</p>
     *
     * @param comparator a {@link java.util.Comparator} object.
     */
    public void setComparator(Comparator<Double> comparator) {
        m_comparator = comparator;
    }

    public class AttributeStatisticComparator implements Comparator<AttributeStatistic> {
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
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
            
            return Integer.valueOf(o1.getAttribute().hashCode()).compareTo(o2.getAttribute().hashCode());
        }
    }
    
    public class DoubleComparator implements Comparator<Double> {
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Double o1, Double o2) {
            return o1.compareTo(o2);
        }
    }

}
