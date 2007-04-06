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

import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.model.AttributeStatisticVisitor;
import org.opennms.netmgt.model.AttributeVisitor;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class RrdStatisticAttributeVisitor implements AttributeVisitor, InitializingBean {
    private RrdDao m_rrdDao;
    private String m_consolidationFunction;
    private Long m_startTime;
    private Long m_endTime;
    private AttributeStatisticVisitor m_statisticVisitor;
    
    /**
     * @see org.opennms.netmgt.model.AttributeVisitor#visit(org.opennms.netmgt.model.OnmsAttribute)
     */
    public void visit(OnmsAttribute attribute) {
        if (!RrdGraphAttribute.class.isAssignableFrom(attribute.getClass())) {
            // Nothing to do if we can't cast to an RrdGraphAttribute
            return;
        }
        
        double statistic = m_rrdDao.getPrintValue(attribute, m_consolidationFunction, m_startTime, m_endTime);
        
        /*
         * We don't want to do anything with NaN data, since
         * it means there is no data. We especially want to
         * stay away from it, because it will be sorted as
         * being higher than any numeric value, which will
         * leave our TopN report with most, if not all NaN
         * values at the top.
         */
        if (Double.isNaN(statistic)) {
            return;
        }
        
        m_statisticVisitor.visit(attribute, statistic);
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() {
        Assert.state(m_rrdDao != null, "property rrdDao must be set to a non-null value");
        Assert.state(m_consolidationFunction != null, "property consolidationFunction must be set to a non-null value");
        Assert.state(m_startTime != null, "property startTime must be set to a non-null value");
        Assert.state(m_endTime != null, "property endTime must be set to a non-null value");
        Assert.state(m_statisticVisitor != null, "property statisticVisitor must be set to a non-null value");
    }

    public RrdDao getRrdDao() {
        return m_rrdDao;
    }

    public void setRrdDao(RrdDao rrdDao) {
        m_rrdDao = rrdDao;
    }

    public AttributeStatisticVisitor getStatisticVisitor() {
        return m_statisticVisitor;
    }

    public void setStatisticVisitor(AttributeStatisticVisitor statisticVisitor) {
        m_statisticVisitor = statisticVisitor;
    }

    public String getConsolidationFunction() {
        return m_consolidationFunction;
    }

    public void setConsolidationFunction(String consolidationFunction) {
        m_consolidationFunction = consolidationFunction;
    }

    public Long getEndTime() {
        return m_endTime;
    }

    public void setEndTime(Long endTime) {
        m_endTime = endTime;
    }

    public Long getStartTime() {
        return m_startTime;
    }

    public void setStartTime(Long startTime) {
        m_startTime = startTime;
    }


}
