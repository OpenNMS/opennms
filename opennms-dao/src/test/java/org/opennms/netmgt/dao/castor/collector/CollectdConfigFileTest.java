/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao.castor.collector;

import java.io.IOException;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.castor.InvocationAnticipator;
import org.springframework.core.io.ClassPathResource;

public class CollectdConfigFileTest extends TestCase {
    
    private InvocationAnticipator m_invocationAnticipator;
    private CollectdConfigVisitor m_visitor;

    protected void setUp() throws Exception {
        super.setUp();
        m_invocationAnticipator = new InvocationAnticipator(CollectdConfigVisitor.class);
        m_visitor = (CollectdConfigVisitor)m_invocationAnticipator.getProxy();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testVisitTop() throws IOException {
        
        ClassPathResource resource = new ClassPathResource("/collectdconfiguration-testdata.xml");
        CollectdConfigFile configFile = new CollectdConfigFile(resource.getFile());
        
        m_invocationAnticipator.anticipateCalls(1, "visitCollectdConfiguration");
        m_invocationAnticipator.anticipateCalls(1, "completeCollectdConfiguration");
        m_invocationAnticipator.anticipateCalls(4, "visitCollectorCollection");
        m_invocationAnticipator.anticipateCalls(4, "completeCollectorCollection");
        
        configFile.visit(m_visitor);
        
        m_invocationAnticipator.verify();
        
    }

}
