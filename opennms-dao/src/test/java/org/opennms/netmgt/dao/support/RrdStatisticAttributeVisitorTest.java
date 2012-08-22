/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import static org.easymock.EasyMock.expect;

import java.util.Collections;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.AttributeStatisticVisitor;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class RrdStatisticAttributeVisitorTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private RrdDao m_rrdDao = m_mocks.createMock(RrdDao.class);
    private Long m_startTime = System.currentTimeMillis();
    private Long m_endTime = m_startTime + (24 * 60 * 60 * 1000); // one day
    private AttributeStatisticVisitor m_statisticVisitor = m_mocks.createMock(AttributeStatisticVisitor.class);
    
    public void testAfterPropertiesSet() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        attributeVisitor.setRrdDao(m_rrdDao);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);
        attributeVisitor.afterPropertiesSet();
    }


    public void testAfterPropertiesSetNoStatisticVisitor() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property statisticVisitor must be set to a non-null value"));
        
        attributeVisitor.setRrdDao(m_rrdDao);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(null);

        try {
            attributeVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSetNoConsolidationFunction() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property consolidationFunction must be set to a non-null value"));

        attributeVisitor.setRrdDao(m_rrdDao);
        attributeVisitor.setConsolidationFunction(null);
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);

        try {
            attributeVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSetNoRrdDao() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property rrdDao must be set to a non-null value"));

        attributeVisitor.setRrdDao(null);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);

        try {
            attributeVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSetNoStartTime() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property startTime must be set to a non-null value"));

        attributeVisitor.setRrdDao(m_rrdDao);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(null);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);

        try {
            attributeVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSetNoEndTime() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property endTime must be set to a non-null value"));

        attributeVisitor.setRrdDao(m_rrdDao);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(null);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);

        try {
            attributeVisitor.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testVisitWithRrdAttribute() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        attributeVisitor.setRrdDao(m_rrdDao);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);
        attributeVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("interfaceSnmp");
        OnmsAttribute attribute = new RrdGraphAttribute("ifInOctets", "something", "something else");
        new OnmsResource("1", "Node One", resourceType, Collections.singleton(attribute));
        expect(m_rrdDao.getPrintValue(attribute, attributeVisitor.getConsolidationFunction(), attributeVisitor.getStartTime(), attributeVisitor.getEndTime())).andReturn(1.0);
        m_statisticVisitor.visit(attribute, 1.0);

        m_mocks.replayAll();
        attributeVisitor.visit(attribute);
        m_mocks.verifyAll();
    }
    
    public void testVisitWithNonRrdAttribute() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        attributeVisitor.setRrdDao(m_rrdDao);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);
        attributeVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("something other than interfaceSnmp");
        OnmsAttribute attribute = new StringPropertyAttribute("ifInOctets", "one billion octets!");
        new OnmsResource("1", "Node One", resourceType, Collections.singleton(attribute));

        m_mocks.replayAll();
        attributeVisitor.visit(attribute);
        m_mocks.verifyAll();
    }
    
    public void testVisitWithNotANumberRrdAttribute() throws Exception {
        RrdStatisticAttributeVisitor attributeVisitor = new RrdStatisticAttributeVisitor();
        attributeVisitor.setRrdDao(m_rrdDao);
        attributeVisitor.setConsolidationFunction("AVERAGE");
        attributeVisitor.setStartTime(m_startTime);
        attributeVisitor.setEndTime(m_endTime);
        attributeVisitor.setStatisticVisitor(m_statisticVisitor);
        attributeVisitor.afterPropertiesSet();

        MockResourceType resourceType = new MockResourceType();
        resourceType.setName("something other than interfaceSnmp");
        OnmsAttribute attribute = new RrdGraphAttribute("ifInOctets", "something", "something else");
        new OnmsResource("1", "Node One", resourceType, Collections.singleton(attribute));
        expect(m_rrdDao.getPrintValue(attribute, attributeVisitor.getConsolidationFunction(), attributeVisitor.getStartTime(), attributeVisitor.getEndTime())).andReturn(Double.NaN);

        m_mocks.replayAll();
        attributeVisitor.visit(attribute);
        m_mocks.verifyAll();
    }
}
