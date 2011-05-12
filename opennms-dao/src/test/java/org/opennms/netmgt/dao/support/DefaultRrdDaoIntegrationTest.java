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

import java.io.File;
import java.util.Collections;
import java.util.HashSet;

import junit.framework.TestCase;

import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.FileAnticipator;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultRrdDaoIntegrationTest extends TestCase {
    private FileAnticipator m_fileAnticipator;

    private RrdStrategy<Object,Object> m_rrdStrategy;
    
    private DefaultRrdDao m_dao;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        RrdTestUtils.initialize();
        m_rrdStrategy = RrdUtils.getStrategy();
        
        m_fileAnticipator = new FileAnticipator();
        
        m_dao = new DefaultRrdDao();
        m_dao.setRrdStrategy(m_rrdStrategy);
        m_dao.setRrdBaseDirectory(m_fileAnticipator.getTempDir());
        m_dao.setRrdBinaryPath("/bin/true");
        m_dao.afterPropertiesSet();
    }
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        
        if (m_fileAnticipator.isInitialized()) {
            m_fileAnticipator.deleteExpected();
        }
    }

    
    @Override
    protected void tearDown() throws Exception {
        m_fileAnticipator.tearDown();
        
        super.tearDown();
    }

    
    public void testInit() {
        // Don't do anything... test that the setUp method works
    }

    public void testPrintValue() throws Exception {
        long start = System.currentTimeMillis();
        long end = start + (24 * 60 * 60 * 1000);
        
        OnmsResource topResource = new OnmsResource("1", "Node One", new MockResourceType(), new HashSet<OnmsAttribute>(0));

        OnmsAttribute attribute = new RrdGraphAttribute("ifInOctets", "snmp/1/eth0", "ifInOctets.jrb");
        HashSet<OnmsAttribute> attributeSet = new HashSet<OnmsAttribute>(1);
        attributeSet.add(attribute);
        
        MockResourceType childResourceType = new MockResourceType();
        OnmsResource childResource = new OnmsResource("eth0", "Interface One: eth0", childResourceType, attributeSet);
        childResource.setParent(topResource);
        
        File snmp = m_fileAnticipator.tempDir(DefaultResourceDao.SNMP_DIRECTORY);
        File node = m_fileAnticipator.tempDir(snmp, topResource.getName());
        File intf = m_fileAnticipator.tempDir(node, childResource.getName());
        
        RrdDataSource rrdDataSource = new RrdDataSource(attribute.getName(), "GAUGE", 600, "U", "U");
        Object def = m_rrdStrategy.createDefinition("test", intf.getAbsolutePath(), attribute.getName(), 600, Collections.singletonList(rrdDataSource), Collections.singletonList("RRA:AVERAGE:0.5:1:100"));
        m_rrdStrategy.createFile(def);
        File rrdFile = m_fileAnticipator.expecting(intf, attribute.getName() + RrdUtils.getExtension());
        
        Object rrdFileObject = m_rrdStrategy.openFile(rrdFile.getAbsolutePath());
        for (int i = 0; i < 10; i++) {
            m_rrdStrategy.updateFile(rrdFileObject, "test", (start/1000 + 300*i) + ":1");
        }
        m_rrdStrategy.closeFile(rrdFileObject);
        
        Double value = m_dao.getPrintValue(childResource.getAttributes().iterator().next(), "AVERAGE", start, end);
        
        assertNotNull("value should not be null", value);
        assertEquals("value", 1.0, value);
    }
}
