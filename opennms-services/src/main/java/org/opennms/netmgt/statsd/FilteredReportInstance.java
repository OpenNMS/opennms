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

package org.opennms.netmgt.statsd;

import java.util.Date;
import java.util.SortedSet;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.RrdDao;
import org.opennms.netmgt.dao.support.AttributeMatchingResourceVisitor;
import org.opennms.netmgt.dao.support.FilterResourceWalker;
import org.opennms.netmgt.dao.support.ResourceAttributeFilteringResourceVisitor;
import org.opennms.netmgt.dao.support.ResourceTypeFilteringResourceVisitor;
import org.opennms.netmgt.dao.support.RrdStatisticAttributeVisitor;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.model.AttributeStatistic;
import org.opennms.netmgt.model.AttributeStatisticVisitorWithResults;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>FilteredReportInstance class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class FilteredReportInstance extends AbstractReportInstance implements ReportInstance, InitializingBean {
    private final AttributeStatisticVisitorWithResults m_attributeStatisticVisitor;
    private final RrdStatisticAttributeVisitor m_rrdVisitor = new RrdStatisticAttributeVisitor();
    private final AttributeMatchingResourceVisitor m_attributeVisitor = new AttributeMatchingResourceVisitor();
    private final ResourceTypeFilteringResourceVisitor m_resourceTypeVisitor = new ResourceTypeFilteringResourceVisitor();
    private final FilterResourceWalker m_walker = new FilterResourceWalker();
    private String m_resourceAttributeKey;
    private String m_resourceAttributeValueMatch;
    private ResourceAttributeFilteringResourceVisitor m_resourceAttributeVisitor;
    
    /**
     * <p>Constructor for FilteredReportInstance.</p>
     *
     * @param visitor a {@link org.opennms.netmgt.model.AttributeStatisticVisitorWithResults} object.
     */
    public FilteredReportInstance(AttributeStatisticVisitorWithResults visitor) {
        m_attributeStatisticVisitor = visitor;
    }
    
    /**
     * <p>setFilterDao</p>
     *
     * @param filterDao a {@link org.opennms.netmgt.filter.FilterDao} object.
     */
    public void setFilterDao(FilterDao filterDao) {
        m_walker.setFilterDao(filterDao);
    }
    
    /**
     * <p>setFilter</p>
     *
     * @param filter a {@link java.lang.String} object.
     */
    public void setFilter(String filter) {
        m_walker.setFilter(filter);
    }
    
    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_walker.setNodeDao(nodeDao);
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_walker.setResourceDao(resourceDao);
    }

    /**
     * <p>setRrdDao</p>
     *
     * @param rrdDao a {@link org.opennms.netmgt.dao.api.RrdDao} object.
     */
    public void setRrdDao(RrdDao rrdDao) {
        m_rrdVisitor.setRrdDao(rrdDao);
    }
    
    /**
     * <p>walk</p>
     */
    @Override
    public void walk() {
        setJobStartedDate(new Date());
        m_walker.walk();
        setJobCompletedDate(new Date());
    }

    /**
     * <p>getResults</p>
     *
     * @return a {@link java.util.SortedSet} object.
     */
    @Override
    public SortedSet<AttributeStatistic> getResults() {
        return m_attributeStatisticVisitor.getResults();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#getResourceTypeMatch()
     */
    /**
     * <p>getResourceTypeMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getResourceTypeMatch() {
        return m_resourceTypeVisitor.getResourceTypeMatch();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#setResourceTypeMatch(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public void setResourceTypeMatch(String resourceType) {
        m_resourceTypeVisitor.setResourceTypeMatch(resourceType);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#getAttributeMatch()
     */
    /**
     * <p>getAttributeMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getAttributeMatch() {
        return m_attributeVisitor.getAttributeMatch();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#setAttributeMatch(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public void setAttributeMatch(String attr) {
        m_attributeVisitor.setAttributeMatch(attr);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#getStartTime()
     */
    /**
     * <p>getStartTime</p>
     *
     * @return a long.
     */
    @Override
    public long getStartTime() {
        return m_rrdVisitor.getStartTime();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#setStartTime(long)
     */
    /** {@inheritDoc} */
    @Override
    public void setStartTime(long start) {
        m_rrdVisitor.setStartTime(start);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#getEndTime()
     */
    /**
     * <p>getEndTime</p>
     *
     * @return a long.
     */
    @Override
    public long getEndTime() {
        return m_rrdVisitor.getEndTime();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#setEndTime(long)
     */
    /** {@inheritDoc} */
    @Override
    public void setEndTime(long end) {
        m_rrdVisitor.setEndTime(end);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#getConsolidationFunction()
     */
    /**
     * <p>getConsolidationFunction</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getConsolidationFunction() {
        return m_rrdVisitor.getConsolidationFunction();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#setConsolidationFunction(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public void setConsolidationFunction(String cf) {
        m_rrdVisitor.setConsolidationFunction(cf);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#getCount()
     */
    /**
     * <p>getCount</p>
     *
     * @return a int.
     */
    @Override
    public int getCount() {
        return m_attributeStatisticVisitor.getCount();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#setCount(int)
     */
    /** {@inheritDoc} */
    @Override
    public void setCount(int count) {
        m_attributeStatisticVisitor.setCount(count);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#afterPropertiesSet()
     */
    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        
        m_rrdVisitor.setStatisticVisitor(m_attributeStatisticVisitor);
        m_attributeVisitor.setAttributeVisitor(m_rrdVisitor);
        if (m_resourceAttributeKey != null && m_resourceAttributeValueMatch != null) {
            m_resourceAttributeVisitor = new ResourceAttributeFilteringResourceVisitor();
            m_resourceAttributeVisitor.setDelegatedVisitor(m_attributeVisitor);
            m_resourceAttributeVisitor.setResourceAttributeKey(m_resourceAttributeKey);
            m_resourceAttributeVisitor.setResourceAttributeValueMatch(m_resourceAttributeValueMatch);
            m_resourceAttributeVisitor.afterPropertiesSet();
            
            m_resourceTypeVisitor.setDelegatedVisitor(m_resourceAttributeVisitor);
        } else {
            m_resourceTypeVisitor.setDelegatedVisitor(m_attributeVisitor);
        }
        m_walker.setVisitor(m_resourceTypeVisitor);

        m_attributeStatisticVisitor.afterPropertiesSet();
        m_rrdVisitor.afterPropertiesSet();
        m_attributeVisitor.afterPropertiesSet();
        m_resourceTypeVisitor.afterPropertiesSet();
        m_walker.afterPropertiesSet();
    }
    

    /** {@inheritDoc} */
    @Override
    public void setResourceAttributeKey(String resourceAttributeKey) {
        m_resourceAttributeKey = resourceAttributeKey;
    }

    /** {@inheritDoc} */
    @Override
    public void setResourceAttributeValueMatch(String resourceAttributeValueMatch) {
        m_resourceAttributeValueMatch = resourceAttributeValueMatch;
    }

    /**
     * <p>getResourceAttributeKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getResourceAttributeKey() {
        return m_resourceAttributeKey;
    }

    /**
     * <p>getResourceAttributeValueMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getResourceAttributeValueMatch() {
        return m_resourceAttributeValueMatch;
    }
}
