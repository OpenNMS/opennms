/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.jaxb.collector;

import java.io.IOException;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.jaxb.InvocationAnticipator;
import org.opennms.netmgt.dao.jaxb.collector.CollectdConfigFile;
import org.opennms.netmgt.dao.jaxb.collector.CollectdConfigVisitor;
import org.springframework.core.io.ClassPathResource;

public class CollectdConfigFileTest extends TestCase {
    
    private InvocationAnticipator m_invocationAnticipator;
    private CollectdConfigVisitor m_visitor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m_invocationAnticipator = new InvocationAnticipator(CollectdConfigVisitor.class);
        m_visitor = (CollectdConfigVisitor)m_invocationAnticipator.getProxy();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testVisitTop() throws IOException {
        
        ClassPathResource resource = new ClassPathResource("/collectdconfiguration-testdata.xml");
        CollectdConfigFile configFile = new CollectdConfigFile(resource.getFile());
        
        m_invocationAnticipator.anticipateCalls(1, "visitCollectdConfiguration");
        m_invocationAnticipator.anticipateCalls(1, "completeCollectdConfiguration");
        m_invocationAnticipator.anticipateCalls(2, "visitCollectorCollection");
        m_invocationAnticipator.anticipateCalls(2, "completeCollectorCollection");
        
        configFile.visit(m_visitor);
        
        m_invocationAnticipator.verify();
        
    }

}
